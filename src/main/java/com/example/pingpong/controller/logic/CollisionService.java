package com.example.pingpong.controller.logic;

import com.example.pingpong.model.Ball;
import com.example.pingpong.model.Paddle;
/**
 * Handles collision detection and ball bouncing logic.
 */
public class CollisionService {

    public boolean hitsPaddle(Ball ball, Paddle paddle, double paddleX,
                              double paddleWidth, double paddleHeight) {

        double ballLeft = ball.getPosition().getX() - ball.getRadius();
        double ballRight = ball.getPosition().getX() + ball.getRadius();
        double ballTop = ball.getPosition().getY() - ball.getRadius();
        double ballBottom = ball.getPosition().getY() + ball.getRadius();

        double paddleTop = paddle.getCenterY() - paddleHeight / 2.0;
        double paddleBottom = paddle.getCenterY() + paddleHeight / 2.0;
        double paddleLeft = paddleX;
        double paddleRight = paddleX + paddleWidth;

        return ballRight >= paddleLeft &&
                ballLeft <= paddleRight &&
                ballBottom >= paddleTop &&
                ballTop <= paddleBottom;
    }

    public void bounceX(Ball ball) {
        ball.getVelocity().set(
                -ball.getVelocity().getX(),
                ball.getVelocity().getY()
        );
    }

    public void bounceY(Ball ball) {
        ball.getVelocity().set(
                ball.getVelocity().getX(),
                -ball.getVelocity().getY()
        );
    }
}