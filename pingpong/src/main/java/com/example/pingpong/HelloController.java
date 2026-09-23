package com.example.pingpong;

import com.example.pingpong.controller.input.Player1Listener;
import com.example.pingpong.controller.input.Player2Listener;
import com.example.pingpong.controller.logic.BallMover;
import com.example.pingpong.controller.logic.CollisionService;
import com.example.pingpong.model.GameSettings;
import com.example.pingpong.model.GameState;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Main JavaFX controller for the ping pong game.
 * Handles drawing, keyboard input, scoring, pause, restart,
 * winner detection and ball speed increases.
 */
public class HelloController implements Initializable {

    private static final double PADDLE_STEP = 6;
    private static final long GOAL_MESSAGE_DURATION_MS = 2000;

    @FXML private Pane playfield;
    @FXML private Label leftScoreLabel;
    @FXML private Label rightScoreLabel;

    private final GameSettings settings = new GameSettings();
    private final GameState state = GameState.createDefault(settings);

    private final Rectangle leftPaddle = new Rectangle();
    private final Rectangle rightPaddle = new Rectangle();
    private final Circle ball = new Circle();
    private final Text overlay = new Text();

    private final CollisionService collisionService = new CollisionService();

    private boolean firstLayoutDone = false;

    private boolean p1Up;
    private boolean p1Down;
    private boolean p2Up;
    private boolean p2Down;

    private boolean paused = false;
    private boolean gameOver = false;
    private boolean countdownRunning = false;

    private int bounceCount = 0;

    private String goalMessage = "";
    private long goalMessageEndTime = 0;

    private String countdownMessage = "";

    private double baseBallSpeed;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        playfield.setStyle("-fx-background-color: black;");

        leftPaddle.setStyle("-fx-fill: white;");
        rightPaddle.setStyle("-fx-fill: white;");
        ball.setStyle("-fx-fill: white;");
        overlay.setStyle("-fx-fill: white; -fx-font-size: 24px;");

        playfield.getChildren().addAll(leftPaddle, rightPaddle, ball, overlay);

        baseBallSpeed = settings.getBallSpeed();
        updateScoreLabels();

        playfield.widthProperty().addListener((obs, o, n) -> refreshLayout());
        playfield.heightProperty().addListener((obs, o, n) -> refreshLayout());

        setupKeyboard();

        playfield.setOnMouseClicked(e -> playfield.requestFocus());
        playfield.requestFocus();

        Thread ballThread = new Thread(
                new BallMover(state, this::updateGame, () -> !paused && !gameOver)
        );
        ballThread.setDaemon(true);
        ballThread.start();
    }

    private void setupKeyboard() {
        playfield.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene == null) return;

            Player1Listener player1Listener = new Player1Listener(
                    () -> p1Up = true,
                    () -> p1Down = true,
                    () -> p1Up = false,
                    () -> p1Down = false
            );

            Player2Listener player2Listener = new Player2Listener(
                    () -> p2Up = true,
                    () -> p2Down = true,
                    () -> p2Up = false,
                    () -> p2Down = false
            );

            scene.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, player1Listener);
            scene.addEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, player1Listener);

            scene.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, player2Listener);
            scene.addEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, player2Listener);

            scene.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.P && !gameOver && !countdownRunning) {
                    paused = !paused;
                    if (!paused) {
                        clearTemporaryMessage();
                    }
                    refreshLayout();
                }

                if (e.getCode() == KeyCode.R) {
                    restartGame();
                }
            });
        });
    }

    private void updateGame() {
        if (playfield.getWidth() <= 0 || playfield.getHeight() <= 0) return;

        double width = playfield.getWidth();
        double height = playfield.getHeight();

        if (!firstLayoutDone) {
            state.getLeftPaddle().setCenterY(height / 2.0);
            state.getRightPaddle().setCenterY(height / 2.0);
            state.getBall().getPosition().set(width / 2.0, height / 2.0);
            firstLayoutDone = true;
        }

        if (paused || gameOver) {
            refreshLayout();
            return;
        }

        movePaddles();
        clampPaddles();

        double ballX = state.getBall().getPosition().getX();
        double ballY = state.getBall().getPosition().getY();
        double radius = state.getBall().getRadius();

        if (ballY - radius <= 0) {
            state.getBall().getPosition().set(ballX, radius);
            collisionService.bounceY(state.getBall());
            registerBounce();
        }

        if (ballY + radius >= height) {
            state.getBall().getPosition().set(ballX, height - radius);
            collisionService.bounceY(state.getBall());
            registerBounce();
        }

        double margin = Math.max(18, width * 0.03);

        if (collisionService.hitsPaddle(
                state.getBall(),
                state.getLeftPaddle(),
                margin,
                settings.getPaddleThickness(),
                settings.getPaddleHeight())) {

            if (state.getBall().getVelocity().getX() < 0) {
                collisionService.bounceX(state.getBall());
                registerBounce();

                state.getBall().getPosition().set(
                        state.getBall().getPosition().getX() + state.getBall().getVelocity().getX(),
                        state.getBall().getPosition().getY()
                );
            }
        }

        if (collisionService.hitsPaddle(
                state.getBall(),
                state.getRightPaddle(),
                width - margin - settings.getPaddleThickness(),
                settings.getPaddleThickness(),
                settings.getPaddleHeight())) {

            if (state.getBall().getVelocity().getX() > 0) {
                collisionService.bounceX(state.getBall());
                registerBounce();

                state.getBall().getPosition().set(
                        state.getBall().getPosition().getX() + state.getBall().getVelocity().getX(),
                        state.getBall().getPosition().getY()
                );
            }
        }

        checkGoal(width, height);
        checkWinner();

        updateScoreLabels();
        refreshLayout();
    }

    private void movePaddles() {
        if (p1Up && !p1Down) {
            state.getLeftPaddle().setCenterY(state.getLeftPaddle().getCenterY() - PADDLE_STEP);
        }
        if (p1Down && !p1Up) {
            state.getLeftPaddle().setCenterY(state.getLeftPaddle().getCenterY() + PADDLE_STEP);
        }
        if (p2Up && !p2Down) {
            state.getRightPaddle().setCenterY(state.getRightPaddle().getCenterY() - PADDLE_STEP);
        }
        if (p2Down && !p2Up) {
            state.getRightPaddle().setCenterY(state.getRightPaddle().getCenterY() + PADDLE_STEP);
        }
    }

    private void clampPaddles() {
        double height = playfield.getHeight();
        double half = settings.getPaddleHeight() / 2.0;

        state.getLeftPaddle().clamp(half, height - half);
        state.getRightPaddle().clamp(half, height - half);
    }

    private void checkGoal(double width, double height) {
        double x = state.getBall().getPosition().getX();

        if (x <= 0) {
            state.getPlayer2().setScore(state.getPlayer2().getScore() + 1);
            showGoalAndReset(width, height, "GOAL " + state.getPlayer2().getName(), 1);
        } else if (x >= width) {
            state.getPlayer1().setScore(state.getPlayer1().getScore() + 1);
            showGoalAndReset(width, height, "GOAL " + state.getPlayer1().getName(), -1);
        }
    }

    private void showGoalAndReset(double width, double height, String message, int directionX) {
        paused = true;
        bounceCount = 0;

        goalMessage = message;
        goalMessageEndTime = System.currentTimeMillis() + GOAL_MESSAGE_DURATION_MS;

        state.getBall().getPosition().set(width / 2.0, height / 2.0);
        state.getBall().getVelocity().set(0, 0);

        new Thread(() -> {
            try {
                Thread.sleep(GOAL_MESSAGE_DURATION_MS);
            } catch (InterruptedException ignored) {
            }

            if (gameOver) return;

            double speed = baseBallSpeed;
            double directionY = Math.random() < 0.5 ? -1 : 1;

            state.getBall().getPosition().set(width / 2.0, height / 2.0);
            state.getBall().getVelocity().set(directionX * speed, directionY * speed);

            paused = false;
            clearTemporaryMessage();
        }).start();
    }

    private void checkWinner() {
        if (state.getPlayer1().getScore() >= settings.getWinningScore()) {
            gameOver = true;
            paused = true;
        } else if (state.getPlayer2().getScore() >= settings.getWinningScore()) {
            gameOver = true;
            paused = true;
        }
    }

    private void registerBounce() {
        bounceCount++;

        int every = settings.getSpeedIncreaseEvery();
        if (every <= 0) return;

        if (bounceCount % every == 0) {
            double vx = state.getBall().getVelocity().getX();
            double vy = state.getBall().getVelocity().getY();

            double dirX = vx < 0 ? -1 : 1;
            double dirY = vy < 0 ? -1 : 1;

            double currentSpeed = Math.abs(vx);
            double newSpeed = currentSpeed + 1;

            state.getBall().getVelocity().set(dirX * newSpeed, dirY * newSpeed);
        }
    }

    private void restartGame() {
        double width = playfield.getWidth();
        double height = playfield.getHeight();

        state.getPlayer1().setScore(0);
        state.getPlayer2().setScore(0);

        state.getLeftPaddle().setCenterY(height / 2.0);
        state.getRightPaddle().setCenterY(height / 2.0);

        bounceCount = 0;
        paused = true;
        gameOver = false;
        clearTemporaryMessage();

        state.getBall().getPosition().set(width / 2.0, height / 2.0);
        state.getBall().getVelocity().set(0, 0);

        updateScoreLabels();
        refreshLayout();

        startRestartCountdown(width, height);
    }

    private void startRestartCountdown(double width, double height) {
        countdownRunning = true;

        new Thread(() -> {
            try {
                countdownMessage = "3";
                Thread.sleep(1000);

                countdownMessage = "2";
                Thread.sleep(1000);

                countdownMessage = "1";
                Thread.sleep(1000);

                countdownMessage = "GO!";
                Thread.sleep(700);
            } catch (InterruptedException ignored) {
            }

            double speed = baseBallSpeed;
            double directionY = Math.random() < 0.5 ? -1 : 1;

            state.getBall().getPosition().set(width / 2.0, height / 2.0);
            state.getBall().getVelocity().set(speed, directionY * speed);

            countdownMessage = "";
            countdownRunning = false;
            paused = false;
        }).start();
    }

    private void clearTemporaryMessage() {
        goalMessage = "";
        goalMessageEndTime = 0;
    }

    private void refreshLayout() {
        double width = playfield.getWidth();
        double height = playfield.getHeight();

        if (width <= 0 || height <= 0) return;

        if (!firstLayoutDone) {
            state.getLeftPaddle().setCenterY(height / 2.0);
            state.getRightPaddle().setCenterY(height / 2.0);
            state.getBall().getPosition().set(width / 2.0, height / 2.0);
            firstLayoutDone = true;
        }

        clampPaddles();

        leftPaddle.setWidth(settings.getPaddleThickness());
        leftPaddle.setHeight(settings.getPaddleHeight());

        rightPaddle.setWidth(settings.getPaddleThickness());
        rightPaddle.setHeight(settings.getPaddleHeight());

        ball.setRadius(state.getBall().getRadius());

        double margin = Math.max(18, width * 0.03);

        leftPaddle.setX(margin);
        rightPaddle.setX(width - margin - settings.getPaddleThickness());

        leftPaddle.setY(state.getLeftPaddle().getCenterY() - settings.getPaddleHeight() / 2.0);
        rightPaddle.setY(state.getRightPaddle().getCenterY() - settings.getPaddleHeight() / 2.0);

        ball.setCenterX(state.getBall().getPosition().getX());
        ball.setCenterY(state.getBall().getPosition().getY());

        if (gameOver) {
            String winner = state.getPlayer1().getScore() >= settings.getWinningScore()
                    ? state.getPlayer1().getName()
                    : state.getPlayer2().getName();
            overlay.setText("WINNER: " + winner);
        } else if (!countdownMessage.isEmpty()) {
            overlay.setText(countdownMessage);
        } else if (paused && goalMessage.isEmpty()) {
            overlay.setText("PAUSED");
        } else if (!goalMessage.isEmpty() && System.currentTimeMillis() <= goalMessageEndTime) {
            overlay.setText(goalMessage);
        } else {
            overlay.setText("");
        }

        overlay.setX(width * 0.35);
        overlay.setY(height * 0.10);
    }

    private void updateScoreLabels() {
        leftScoreLabel.setText(state.getPlayer1().getName() + " : " + state.getPlayer1().getScore());
        rightScoreLabel.setText(state.getPlayer2().getScore() + " : " + state.getPlayer2().getName());
    }

    @FXML
    private void onExit() {
        System.exit(0);
    }

    @FXML
    private void onSetPlayerNames() {
        TextInputDialog d1 = new TextInputDialog(state.getPlayer1().getName());
        d1.setHeaderText("Player 1 Name");
        Optional<String> r1 = d1.showAndWait();
        r1.map(String::trim).filter(s -> !s.isEmpty()).ifPresent(state.getPlayer1()::setName);

        TextInputDialog d2 = new TextInputDialog(state.getPlayer2().getName());
        d2.setHeaderText("Player 2 Name");
        Optional<String> r2 = d2.showAndWait();
        r2.map(String::trim).filter(s -> !s.isEmpty()).ifPresent(state.getPlayer2()::setName);

        updateScoreLabels();
    }

    @FXML
    private void onSetBallSpeed() {
        TextInputDialog d = new TextInputDialog("" + settings.getBallSpeed());
        d.setHeaderText("Ball Speed");
        d.setContentText("Enter speed (recommended 2 to 8):");

        d.showAndWait().ifPresent(v -> {
            double newSpeed = Double.parseDouble(v);
            settings.setBallSpeed(newSpeed);
            baseBallSpeed = newSpeed;

            double currentX = state.getBall().getVelocity().getX();
            double currentY = state.getBall().getVelocity().getY();

            double dirX = currentX < 0 ? -1 : 1;
            double dirY = currentY < 0 ? -1 : 1;

            state.getBall().getVelocity().set(dirX * newSpeed, dirY * newSpeed);
        });
    }

    @FXML
    private void onSetWinningScore() {
        TextInputDialog d = new TextInputDialog("" + settings.getWinningScore());
        d.setHeaderText("Winning Score");

        d.showAndWait().ifPresent(v -> settings.setWinningScore(Integer.parseInt(v)));
    }

    @FXML
    private void onSetSpeedIncreaseEvery() {
        TextInputDialog d = new TextInputDialog("" + settings.getSpeedIncreaseEvery());
        d.setHeaderText("Increase Speed Every X Bounces");

        d.showAndWait().ifPresent(v -> settings.setSpeedIncreaseEvery(Integer.parseInt(v)));
    }

    @FXML
    private void onSetRacketDimensions() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Racket Dimensions");

        TextField heightField = new TextField("" + settings.getPaddleHeight());
        TextField thickField = new TextField("" + settings.getPaddleThickness());

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Height:"), heightField);
        grid.addRow(1, new Label("Thickness:"), thickField);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;

            double h = Double.parseDouble(heightField.getText().trim());
            double t = Double.parseDouble(thickField.getText().trim());

            settings.setPaddleHeight(h);
            settings.setPaddleThickness(t);

            refreshLayout();
        });
    }
}