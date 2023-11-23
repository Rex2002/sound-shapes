package de.dhbw.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/VideoScene.fxml"));
        StackPane root = loader.load();
        Scene videoScene = new Scene(root);

        primaryStage.setTitle("SoundShapes");
        primaryStage.setMinHeight(340);
        primaryStage.setMinWidth(600);
        primaryStage.setScene(videoScene);
        primaryStage.show();
        primaryStage.requestFocus();
    }

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0);
    }
}
