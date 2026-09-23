package com.example.pingpong.controller.input;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Player1Listener implements EventHandler<KeyEvent> {

    private final Runnable onUpPressed;
    private final Runnable onDownPressed;
    private final Runnable onUpReleased;
    private final Runnable onDownReleased;

    public Player1Listener(Runnable onUpPressed, Runnable onDownPressed,
                           Runnable onUpReleased, Runnable onDownReleased) {
        this.onUpPressed = onUpPressed;
        this.onDownPressed = onDownPressed;
        this.onUpReleased = onUpReleased;
        this.onDownReleased = onDownReleased;
    }

    @Override
    public void handle(KeyEvent event) {
        if (event.getCode() == KeyCode.W) {
            if (event.getEventType() == KeyEvent.KEY_PRESSED) onUpPressed.run();
            if (event.getEventType() == KeyEvent.KEY_RELEASED) onUpReleased.run();
        }

        if (event.getCode() == KeyCode.S) {
            if (event.getEventType() == KeyEvent.KEY_PRESSED) onDownPressed.run();
            if (event.getEventType() == KeyEvent.KEY_RELEASED) onDownReleased.run();
        }
    }
}