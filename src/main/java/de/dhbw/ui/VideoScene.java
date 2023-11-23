package de.dhbw.ui;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.Setting;
import de.dhbw.communication.SettingType;
import de.dhbw.communication.UIMessage;
import de.dhbw.statics;
import de.dhbw.video.shape.Shape;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.util.List;

public class VideoScene {
    @FXML
    private StackPane root;
    @FXML
    private ImageView currentFrame;
    @FXML
    private AnchorPane fieldPane;
    @FXML
    private AnchorPane shapePane;
    @FXML
    private GridPane menu_pane;
    @FXML
    private Button mute_btn;
    @FXML
    private Button play_btn;
    @FXML
    private Button metronome_btn;

    private CheckQueueService checkQueueService;
    private List<Shape> shapesToDraw;
    private int[] playFieldInformation;
    private boolean playing = true;
    private boolean metronome = false;
    private boolean mute = false;
    private double aspectRatio = 0;
    private double frameWidth = 0;
    private double scaleRatio;
    ChangeListener<? super Number> sizeChangeListener;

    @FXML
    private void initialize() {
        //bind ImageView dimensions to parent
        currentFrame.fitWidthProperty().bind( root.widthProperty() );
        currentFrame.fitHeightProperty().bind( root.heightProperty() );

        sizeChangeListener = (observable, oldValue, newValue) -> setUIDimensions();
        root.widthProperty().addListener(sizeChangeListener);

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
        if (frameWidth == 0) {
            frameWidth = frame.width();
            aspectRatio = frameWidth / frame.height();
            setUIDimensions();
        }

        if (shapesToDraw != null) {
            processShapes( shapesToDraw );
        }
        else {
            System.out.println("VideoScene: Can't draw shapes because none are present.");
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
            System.out.println("VideoScene: Can't draw PlayField because none is present.");
        }

        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        Image image = new Image( new ByteArrayInputStream( buffer.toArray() ) );

        currentFrame.setImage( image );
    }

    private void processShapes(List<Shape> shapes) {
        shapePane.getChildren().clear();
        for (Shape shape : shapes) {
            drawShape( shape );
        }
    }

    private void drawShape(Shape shape) {
        Point[] points = shape.getContour().toArray();
        scaleCoordinates(points);

        Path path = new Path();
        MoveTo moveTo = new MoveTo( points[ points.length - 1 ].x, points[ points.length - 1 ].y );
        path.getElements().add(moveTo);
        for (Point point : points) {
            LineTo lineTo = new LineTo(point.x, point.y);
            path.getElements().add(lineTo);
        }

        shapePane.getChildren().add( path );
    }

    private void setUIDimensions() {
        root.setPrefHeight( root.getWidth() / aspectRatio);
        scaleRatio = frameWidth / root.getWidth();
        fieldPane.setPrefSize( root.getWidth(), root.getHeight() );
        shapePane.setPrefSize( root.getWidth(), root.getHeight() );
    }

    private void scaleCoordinates(Point[] points) {
        for (Point point : points) {
            point.x /= scaleRatio;
            point.y /= scaleRatio;
        }
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
