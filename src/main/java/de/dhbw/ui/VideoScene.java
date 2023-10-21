package de.dhbw.ui;

import de.dhbw.communication.Message;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.util.List;

public class VideoScene {
    @FXML
    private StackPane root;
    @FXML
    private ImageView currentFrame;

    private CheckQueueService checkQueueService;

    @FXML
    private void initialize() {
        //bind ImageView dimensions to parent
        currentFrame.fitWidthProperty().bind( root.widthProperty() );
        currentFrame.fitHeightProperty().bind( root.heightProperty() );

        checkQueueService = new CheckQueueService();
        checkQueueService.setPeriod( Duration.millis(33) );
        checkQueueService.setOnSucceeded( (event) -> handleQueue() );
    }

    private void handleQueue() {
        List<Message> messages = checkQueueService.getValue();
        for (Message message : messages) {
            switch (message.type) {
                case FRAME -> {
                    updateFrame( message.data );
                }
            }
        }
    }

    private void updateFrame(Object data) {
        if ( !(data instanceof Mat) ) {
            return;
        }

        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", (Mat) data, buffer);
        Image frame = new Image( new ByteArrayInputStream(buffer.toArray() ) );

        currentFrame.setImage( frame );
    }

}
