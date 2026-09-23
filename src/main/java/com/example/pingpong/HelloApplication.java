package com.example.pingpong;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("hello-view.fxml"));

        Scene scene = new Scene(loader.load(), 900, 600);

        stage.setTitle("Ping Pong - Stage 1");
        stage.setScene(scene);

        stage.setMinWidth(700);
        stage.setMinHeight(450);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
