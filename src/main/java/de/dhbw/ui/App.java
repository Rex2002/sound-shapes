package de.dhbw.ui;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
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
        //fixAspectRatio(primaryStage, 16/9.0);
        primaryStage.setScene(videoScene);
        primaryStage.show();
        primaryStage.requestFocus();
    }

    private ChangeListener<? super Number> widthChangeListener;
    private ChangeListener<? super Number> heightChangeListener;

    private void fixAspectRatio(Stage stage, double ratio) {
        widthChangeListener = (observable, oldValue, newValue) -> {
            stage.heightProperty().removeListener(heightChangeListener);
            stage.setHeight(newValue.doubleValue() / ratio );
            stage.heightProperty().addListener(heightChangeListener);
        };
        heightChangeListener = (observable, oldValue, newValue) -> {
            stage.widthProperty().removeListener(widthChangeListener);
            stage.setWidth(newValue.doubleValue() * ratio );
            stage.widthProperty().addListener(widthChangeListener);
        };

        stage.widthProperty().addListener(widthChangeListener);
        stage.heightProperty().addListener(heightChangeListener);
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
