package com.example.pingpong.controller.logic;

import com.example.pingpong.model.GameState;
import javafx.application.Platform;

import java.util.function.BooleanSupplier;

/**
 * Moves the ball on a background thread and requests UI updates on the JavaFX thread.
 */
public class BallMover implements Runnable {

    private final GameState state;
    private final Runnable onUpdate;
    private final BooleanSupplier canMove;
    private volatile boolean running = true;

    public BallMover(GameState state, Runnable onUpdate, BooleanSupplier canMove) {
        this.state = state;
        this.onUpdate = onUpdate;
        this.canMove = canMove;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                running = false;
                break;
            }

            if (canMove.getAsBoolean()) {
                state.getBall().getPosition().set(
                        state.getBall().getPosition().getX() + state.getBall().getVelocity().getX(),
                        state.getBall().getPosition().getY() + state.getBall().getVelocity().getY()
                );
            }

            Platform.runLater(onUpdate);
        }
    }

    public void stop() {
        running = false;
    }
}