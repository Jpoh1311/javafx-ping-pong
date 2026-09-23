package com.example.pingpong.model;

public class GameState {

    private Player player1;
    private Player player2;

    private Paddle leftPaddle;
    private Paddle rightPaddle;

    private Ball ball;

    public GameState(Player p1, Player p2, Paddle left, Paddle right, Ball ball){

        this.player1 = p1;
        this.player2 = p2;
        this.leftPaddle = left;
        this.rightPaddle = right;
        this.ball = ball;
    }

    public static GameState createDefault(GameSettings settings){

        Player p1 = new Player("Player 1");
        Player p2 = new Player("Player 2");

        Paddle left = new Paddle(300);
        Paddle right = new Paddle(300);

        Ball ball = new Ball(new Vector2D(450, 300), new Vector2D(3, 3), 10);

        return new GameState(p1,p2,left,right,ball);
    }

    public Player getPlayer1(){ return player1; }
    public Player getPlayer2(){ return player2; }

    public Paddle getLeftPaddle(){ return leftPaddle; }
    public Paddle getRightPaddle(){ return rightPaddle; }

    public Ball getBall(){ return ball; }
}
