package com.example.pingpong.controller.input;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Player2Listener implements EventHandler<KeyEvent> {

    private final Runnable onUpPressed;
    private final Runnable onDownPressed;
    private final Runnable onUpReleased;
    private final Runnable onDownReleased;

    public Player2Listener(Runnable onUpPressed, Runnable onDownPressed,
                           Runnable onUpReleased, Runnable onDownReleased) {
        this.onUpPressed = onUpPressed;
        this.onDownPressed = onDownPressed;
        this.onUpReleased = onUpReleased;
        this.onDownReleased = onDownReleased;
    }

    @Override
    public void handle(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
            if (event.getEventType() == KeyEvent.KEY_PRESSED) onUpPressed.run();
            if (event.getEventType() == KeyEvent.KEY_RELEASED) onUpReleased.run();
        }

        if (event.getCode() == KeyCode.DOWN) {
            if (event.getEventType() == KeyEvent.KEY_PRESSED) onDownPressed.run();
            if (event.getEventType() == KeyEvent.KEY_RELEASED) onDownReleased.run();
        }
    }
}