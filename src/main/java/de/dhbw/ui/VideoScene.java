package de.dhbw.ui;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.Setting;
import de.dhbw.communication.SettingType;
import de.dhbw.communication.UIMessage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public class VideoScene {
    @FXML
    private StackPane root;
    @FXML
    private ImageView currentFrame;
    @FXML
    private Button play_btn;

    private CheckQueueService checkQueueService;
    private boolean playing;

    @FXML
    private void initialize() {
        //bind ImageView dimensions to parent
        currentFrame.fitWidthProperty().bind( root.widthProperty() );
        currentFrame.fitHeightProperty().bind( root.heightProperty() );

        checkQueueService = new CheckQueueService();
        checkQueueService.setPeriod( Duration.millis(33) );
        checkQueueService.setOnSucceeded( (event) -> handleQueue() );
        checkQueueService.start();
    }

    private void handleQueue() {
        List<UIMessage> messages = checkQueueService.getValue();
        for (UIMessage message : messages) {
            if (message.getFrame() != null) {
                updateFrame( message.getFrame() );
            }
            if (message.getSetting() != null) {
                //do something
            }
            if (message.getShapes() != null) {
                //do something
            }
        }
    }

    private void updateFrame(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        Image image = new Image( new ByteArrayInputStream(buffer.toArray() ) );

        currentFrame.setImage( image );
    }

    @FXML
    private void pressPlayPause() {
        playing = !playing;
        Setting setting = new Setting(SettingType.PLAY, 1.0);
        EventQueues.toController.add(setting);
        play_btn.setText(playing ? "Pause" : "Play");

        System.out.println(Arrays.toString(EventQueues.toController.toArray()));
    }

}
