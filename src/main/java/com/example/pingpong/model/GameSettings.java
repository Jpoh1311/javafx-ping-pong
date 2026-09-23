package com.example.pingpong.model;

public class GameSettings {

    private double ballSpeed = 3;
    private double paddleHeight = 120;
    private double paddleThickness = 15;
    private int winningScore = 5;
    private int speedIncreaseEvery = 2;

    public double getBallSpeed(){ return ballSpeed; }
    public void setBallSpeed(double v){ ballSpeed = v; }

    public double getPaddleHeight(){ return paddleHeight; }
    public void setPaddleHeight(double v){ paddleHeight = v; }

    public double getPaddleThickness(){ return paddleThickness; }

    public int getWinningScore(){ return winningScore; }
    public void setWinningScore(int v){ winningScore = v; }

    public int getSpeedIncreaseEvery(){ return speedIncreaseEvery; }
    public void setSpeedIncreaseEvery(int v){ speedIncreaseEvery = v; }
    public void setPaddleThickness(double v){ paddleThickness = v; }

}

