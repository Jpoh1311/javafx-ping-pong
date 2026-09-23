package com.example.pingpong.model;

public class Paddle {

    private double centerY;

    public Paddle(double centerY){
        this.centerY = centerY;
    }

    public double getCenterY(){ return centerY; }

    public void clamp(double min, double max){

        if(centerY < min) centerY = min;
        if(centerY > max) centerY = max;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

}

