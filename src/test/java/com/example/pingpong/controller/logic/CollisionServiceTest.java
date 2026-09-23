package com.example.pingpong.controller.logic;


import com.example.pingpong.model.Ball;
import com.example.pingpong.model.Paddle;
import com.example.pingpong.model.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CollisionServiceTest {

    @Test
    void testBallHitsPaddle() {
        CollisionService service = new CollisionService();

        Ball ball = new Ball(new Vector2D(30, 100), new Vector2D(3, 3), 10);
        Paddle paddle = new Paddle(100);

        boolean hit = service.hitsPaddle(ball, paddle, 20, 15, 120);

        assertTrue(hit);
    }

    @Test
    void testBallMissesPaddle() {
        CollisionService service = new CollisionService();

        Ball ball = new Ball(new Vector2D(200, 300), new Vector2D(3, 3), 10);
        Paddle paddle = new Paddle(100);

        boolean hit = service.hitsPaddle(ball, paddle, 20, 15, 120);

        assertFalse(hit);
    }

    @Test
    void testBounceXReversesHorizontalVelocity() {
        CollisionService service = new CollisionService();
        Ball ball = new Ball(new Vector2D(50, 50), new Vector2D(4, 2), 10);

        service.bounceX(ball);

        assertEquals(-4, ball.getVelocity().getX());
        assertEquals(2, ball.getVelocity().getY());
    }

    @Test
    void testBounceYReversesVerticalVelocity() {
        CollisionService service = new CollisionService();
        Ball ball = new Ball(new Vector2D(50, 50), new Vector2D(4, 2), 10);

        service.bounceY(ball);

        assertEquals(4, ball.getVelocity().getX());
        assertEquals(-2, ball.getVelocity().getY());
    }
}