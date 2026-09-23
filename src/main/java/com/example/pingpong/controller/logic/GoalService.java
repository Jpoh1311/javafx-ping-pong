package com.example.pingpong.controller.logic;

import com.example.pingpong.model.GameSettings;
import com.example.pingpong.model.GameState;

public class GoalService {

    private final GameSettings settings;

    private String goalMessage = "";
    private long messageEndTime = 0;
    private boolean paused = false;

    public GoalService(GameSettings settings) {
        this.settings = settings;
    }

    public void checkGoal(GameState state, double fieldWidth, double fieldHeight) {
        double x = state.getBall().getPosition().getX();

        if (paused) {
            return;
        }

        if (x <= 0) {
            state.getPlayer2().setScore(state.getPlayer2().getScore() + 1);
            showGoalAndReset(state, fieldWidth, fieldHeight, "GOAL " + state.getPlayer2().getName(), 1);
        } else if (x >= fieldWidth) {
            state.getPlayer1().setScore(state.getPlayer1().getScore() + 1);
            showGoalAndReset(state, fieldWidth, fieldHeight, "GOAL " + state.getPlayer1().getName(), -1);
        }
    }

    private void showGoalAndReset(GameState state, double fieldWidth, double fieldHeight, String message, int directionX) {
        paused = true;
        goalMessage = message;
        messageEndTime = System.currentTimeMillis() + 2000;

        state.getBall().getPosition().set(fieldWidth / 2.0, fieldHeight / 2.0);
        state.getBall().getVelocity().set(0, 0);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }

            double speed = settings.getBallSpeed();
            state.getBall().getPosition().set(fieldWidth / 2.0, fieldHeight / 2.0);
            state.getBall().getVelocity().set(directionX * speed, speed);

            goalMessage = "";
            paused = false;
        }).start();
    }

    public String getGoalMessage() {
        if (System.currentTimeMillis() <= messageEndTime) {
            return goalMessage;
        }
        return "";
    }

    public boolean isPaused() {
        return paused;
    }
}