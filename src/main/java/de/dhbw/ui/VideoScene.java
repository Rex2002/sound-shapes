package de.dhbw.ui;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.Setting;
import de.dhbw.communication.SettingType;
import de.dhbw.communication.UIMessage;
import de.dhbw.video.shape.Shape;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.util.List;

public class VideoScene {
    @FXML
    private AnchorPane root;
    @FXML
    private StackPane stack;
    @FXML
    private ImageView currentFrame;
    @FXML
    private AnchorPane fieldPane;
    @FXML
    private AnchorPane shapePane;
    @FXML
    private GridPane menu_pane;
    @FXML
    private ImageView mute_btn;
    @FXML
    private ImageView play_btn;
    @FXML
    private ImageView metronome_btn;

    private CheckQueueService checkQueueService;
    private ResourceProvider resourceProvider;
    private boolean playing = true;
    private boolean metronome = false;
    private boolean mute = false;
    private double aspectRatioFrame;
    private double frameWidth = -1;
    private double scaleRatio;
    ChangeListener<? super Number> sizeChangeListener;
    private boolean fieldPaneCleared = false;
    @FXML
    private void initialize() {
        //bind ImageView dimensions to parent
        currentFrame.fitWidthProperty().bind(stack.widthProperty());
        currentFrame.fitHeightProperty().bind(stack.heightProperty());

        sizeChangeListener = (observable, oldValue, newValue) -> setUIDimensions();
        stack.widthProperty().addListener(sizeChangeListener);

        resourceProvider = new ResourceProvider();
        checkQueueService = new CheckQueueService();
        checkQueueService.setPeriod(Duration.millis(33));
        checkQueueService.setOnSucceeded((event) -> handleQueue());
        checkQueueService.start();
        EventQueues.toUI.clear();
    }
    private void handleQueue() {
        fieldPaneCleared = false;
        List<UIMessage> messages = checkQueueService.getValue();
        for (UIMessage message : messages) {
            if (message.getFrame() != null) {
                updateFrame( message.getFrame() );
            }
            if (message.getSetting() != null) {
                //do something
            }
            if (message.getShapes() != null) {
                processShapes( message.getShapes() );
            }
            if (message.getPlayFieldInformation() != null && message.getPlayFieldInformation()[4] == 1){
                fieldPane.getChildren().clear();
                fieldPaneCleared = true;
                drawPlayField( message.getPlayFieldInformation() );
            }
            if (message.getPositionMarker() != null){
                if(!fieldPaneCleared) fieldPane.getChildren().clear();
                drawPositionMarker(message.getPositionMarker());
            }
        }
    }

    private void updateFrame(Mat frame) {
        if (frameWidth == -1) {
            frameWidth = frame.width();
            aspectRatioFrame = frameWidth / frame.height();
            setUIDimensions();
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
        for (Point point : points) {
            point.x = scaleCoordinate(point.x);
            point.y = scaleCoordinate(point.y);
        }

        Path path = new Path();
        MoveTo moveTo = new MoveTo( points[ points.length - 1 ].x, points[ points.length - 1 ].y );
        path.getElements().add(moveTo);
        for (Point point : points) {
            LineTo lineTo = new LineTo(point.x, point.y);
            path.getElements().add(lineTo);
        }

        shapePane.getChildren().add( path );
    }

    private void drawPlayField(int[] input) {
        double[] playFieldInfo = new double[4];
        for (int i = 0; i < playFieldInfo.length; i++) {
            playFieldInfo[i] = scaleCoordinate(input[i]);
        }
        Rectangle playfield = new Rectangle(playFieldInfo[0], playFieldInfo[1], playFieldInfo[2], playFieldInfo[3]);
        playfield.setStroke(Color.YELLOW);
        playfield.setFill(Color.TRANSPARENT);

        fieldPane.getChildren().add(playfield);
    }

    private void drawPositionMarker(int[] input){
        double[] positionMarkerInfo = new double[4];
        for (int i = 0; i < positionMarkerInfo.length; i++) {
            positionMarkerInfo[i] = scaleCoordinate(input[i]);
        }

        Rectangle positionMarker = new Rectangle(positionMarkerInfo[0], positionMarkerInfo[1], positionMarkerInfo[2], positionMarkerInfo[3]);
        positionMarker.setFill(Color.GREEN);
        positionMarker.opacityProperty().setValue(0.4);

        fieldPane.getChildren().add(positionMarker);

    }

    private void setUIDimensions() {
        double aspectRatioRoot = root.getWidth() / root.getHeight();
        if (aspectRatioRoot < aspectRatioFrame) {
            stack.setMaxHeight( root.getWidth() / aspectRatioFrame );
            scaleRatio = frameWidth / root.getWidth();
            double actualHeightImgView = scaleCoordinate(frameWidth / aspectRatioFrame);
            fieldPane.setMaxSize( stack.getWidth(), actualHeightImgView );
            shapePane.setMaxSize( stack.getWidth(), actualHeightImgView );
        } else {
            stack.setMaxWidth( root.getHeight() * aspectRatioFrame );
            scaleRatio = (frameWidth / aspectRatioFrame) / root.getHeight();
            double actualWidthImgView = scaleCoordinate(frameWidth);
            fieldPane.setMaxSize( actualWidthImgView, stack.getHeight() );
            shapePane.setMaxSize( actualWidthImgView, stack.getHeight() );
        }
    }

    private double scaleCoordinate(double coordinate) {
        return coordinate / scaleRatio;
    }

    @FXML
    private void togglePlayPause() {
        playing = !playing;
        double playValue = playing ? 1.0 : 0.0;
        Setting setting = new Setting(SettingType.PLAY, playValue);
        EventQueues.toController.add(setting);
        if(playing){
            play_btn.setImage( new Image( resourceProvider.getResource("src/main/resources/icons/stop_blue.png").toURI().toString() ) );
        }
        else{
            play_btn.setImage( new Image( resourceProvider.getResource("src/main/resources/icons/play_blue.png").toURI().toString() ) );
        }

        //TODO: maybe load icons in initialize() to make swapping them snappier
    }

    @FXML
    private void toggleMetronome() {
        metronome = !metronome;
        double metronomeValue = metronome ? 1.0 : 0.0;
        Setting setting = new Setting(SettingType.METRONOME, metronomeValue);
        EventQueues.toController.add(setting);
        if(metronome){
            metronome_btn.setImage( new Image( resourceProvider.getResource("src/main/resources/icons/metronome_on_blue.png").toURI().toString() ) );
        }
        else{
            metronome_btn.setImage( new Image( resourceProvider.getResource("src/main/resources/icons/metronome_off_blue.png").toURI().toString() ) );
        }

        //TODO: maybe load icons in initialize() to make swapping them snappier
    }

    @FXML
    private void toggleMute() {
        mute = !mute;
        double muteValue = mute ? 0.0 : 1.0;
        Setting setting = new Setting(SettingType.MUTE, muteValue);
        EventQueues.toController.add(setting);
        if(mute){
            mute_btn.setImage( new Image( resourceProvider.getResource("src/main/resources/icons/volume_mute_blue.png").toURI().toString() ) );
        }
        else{
            mute_btn.setImage( new Image( resourceProvider.getResource("src/main/resources/icons/volume_max_blue.png").toURI().toString() ) );
        }

        //TODO: maybe load icons in initialize() to make swapping them snappier
    }
}
