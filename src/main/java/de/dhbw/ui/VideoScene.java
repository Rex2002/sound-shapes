package de.dhbw.ui;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.Setting;
import de.dhbw.communication.SettingType;
import de.dhbw.communication.UIMessage;
import de.dhbw.statics;
import de.dhbw.video.shape.Shape;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

public class VideoScene {
    @FXML
    private StackPane root;
    @FXML
    private ImageView currentFrame;
    @FXML
    private Button play_btn;
    @FXML
    private Button metronome_btn;
    @FXML
    private Button mute_btn;
    @FXML
    public GridPane menu_pane;

    private CheckQueueService checkQueueService;
    private List<Shape> shapesToDraw;
    private int[] playFieldInformation;
    private boolean playing = true;
    private boolean metronome = false;
    private boolean mute = false;

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
                shapesToDraw = message.getShapes();
            }
            if (message.getPlayFieldInformation() != null){
                this.playFieldInformation = message.getPlayFieldInformation();
            }
        }
    }

    private void updateFrame(Mat frame) {
        if (shapesToDraw != null) {
            List<MatOfPoint> contours = shapesToDraw.stream().map(Shape::getContour).collect(Collectors.toList());
            Imgproc.drawContours(frame, contours, -1,  statics.SHAPE_HL_COLOR);
        }
        else {
            //System.out.println("VideoScene: Can't draw shapes because none are present.");
        }
        if (playFieldInformation != null) {
            Imgproc.rectangle(
                    frame,
                    new Point(playFieldInformation[0], playFieldInformation[1]),
                    new Point(playFieldInformation[0] + playFieldInformation[2], playFieldInformation[1] + playFieldInformation[3]),
                    statics.PLAYFIELD_HL_COLOR
            );
        }
        else {
            //System.out.println("VideoScene: Can't draw PlayField because none is present.");
        }

        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        Image image = new Image( new ByteArrayInputStream( buffer.toArray() ) );

        currentFrame.setImage( image );
    }

    @FXML
    private void togglePlayPause() {
        playing = !playing;
        double playValue = playing ? 1.0 : 0.0;
        Setting setting = new Setting(SettingType.PLAY, playValue);
        EventQueues.toController.add(setting);
        play_btn.setText(playing ? "Pause" : "Play");
    }

    @FXML
    private void toggleMetronome() {
        metronome = !metronome;
        double metronomeValue = metronome ? 1.0 : 0.0;
        Setting setting = new Setting(SettingType.METRONOME, metronomeValue);
        EventQueues.toController.add(setting);
        metronome_btn.setText(metronome ? "Click off" : "Click on");
    }

    @FXML
    private void toggleMute() {
        mute = !mute;
        double muteValue = mute ? 0.0 : 1.0;
        Setting setting = new Setting(SettingType.MUTE, muteValue);
        EventQueues.toController.add(setting);
        mute_btn.setText(mute ? "Unmute" : "Mute");
    }

}
