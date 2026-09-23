package com.example.pingpong.model;

public final class Ball {
    private final Vector2D position;
    private final Vector2D velocity;
    private double radius;

    public Ball(Vector2D position, Vector2D velocity, double radius) {
        this.position = position;
        this.velocity = velocity;
        this.radius = radius;
    }

    public Vector2D getPosition() {
        return position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void resetToCenter(double fieldWidth, double fieldHeight) {
        position.set(fieldWidth / 2.0, fieldHeight / 2.0);
    }
}