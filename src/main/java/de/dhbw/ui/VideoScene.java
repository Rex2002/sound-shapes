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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
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
    public AnchorPane shape_pane;
    @FXML
    private StackPane root;
    @FXML
    private ImageView currentFrame;
    @FXML
    private Button play_btn;

    private CheckQueueService checkQueueService;
    private boolean playing;
    private List<Shape> shapes2Draw;
    private int[] playFieldInformation;

    @FXML
    private void initialize() {
        //bind ImageView dimensions to parent
        currentFrame.fitWidthProperty().bind( root.widthProperty() );
        currentFrame.fitHeightProperty().bind( root.heightProperty() );

        checkQueueService = new CheckQueueService();
        checkQueueService.setPeriod( Duration.millis(300) );
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
                addShapes( message.getShapes() );
            }
            if (message.getPlayFieldInformation() != null){
                addPlayFieldInformation(message.getPlayFieldInformation());
            }
        }
    }

    private void updateFrame(Mat frame) {
        if(shapes2Draw != null){
            List<MatOfPoint> contours = shapes2Draw.stream().map(Shape::getContour).collect(Collectors.toList());
            Imgproc.drawContours(frame, contours, -1,  statics.SHAPE_HL_COLOR);
        }
        else{
            System.out.println("having no shapes");
        }
        if(playFieldInformation != null){
            Imgproc.rectangle(frame, new Point(playFieldInformation[0], playFieldInformation[1]), new Point(playFieldInformation[0] + playFieldInformation[2], playFieldInformation[1] + playFieldInformation[3]), statics.PLAYFIELD_HL_COLOR);
        }
        else{
            System.out.println("Having no playfield");
        }

        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        Image image = new Image( new ByteArrayInputStream(buffer.toArray() ) );

        //image.heightProperty()
        currentFrame.setImage(null);
        currentFrame.setImage( image );
    }

    private void addShapes(List<Shape> shapes) {
        shapes2Draw = shapes;
    }

    private void drawPlayField(Shape[] corners) {
        Path border = new Path();
        MoveTo moveTo = new MoveTo( corners[corners.length-1].getPos()[0], corners[corners.length-1].getPos()[1] );
        border.getElements().add(moveTo);
        for (Shape corner : corners) {
            LineTo lineTo = new LineTo( corner.getPos()[0], corner.getPos()[1] );
            border.getElements().add(lineTo);
        }

        shape_pane.getChildren().add( border );
    }

    private void addPlayFieldInformation(int[] playFieldInformation){
        this.playFieldInformation = playFieldInformation;
    }

    private void drawShape(Shape shape) {
        Point[] points = shape.getContour().toArray();

        Path path = new Path();
        MoveTo moveTo = new MoveTo( points[ points.length - 1 ].x, points[ points.length - 1 ].y );
        path.getElements().add(moveTo);
        for (Point point : points) {
            LineTo lineTo = new LineTo(point.x, point.y);
            path.getElements().add(lineTo);
        }


        shape_pane.getChildren().add( path );
    }

    @FXML
    private void pressPlayPause() {
        playing = !playing;
        Setting setting = new Setting(SettingType.PLAY, 1.0);
        EventQueues.toController.add(setting);
        play_btn.setText(playing ? "Pause" : "Play");
    }

}
