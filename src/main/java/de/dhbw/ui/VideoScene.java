package de.dhbw.ui;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.Setting;
import de.dhbw.communication.SettingType;
import de.dhbw.communication.UIMessage;
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
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.util.List;

public class VideoScene {
    public AnchorPane shape_pane;
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
                processShapes( message.getShapes() );
            }
            if (message.getLines() != null){
                drawConnectedLines(message.getLines());
            }
        }
    }

    private void updateFrame(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        Image image = new Image( new ByteArrayInputStream(buffer.toArray() ) );

        currentFrame.setImage( image );
    }

    private void processShapes(List<Shape> shapes) {

        //operates on the assumption that input always contains exactly 4 field markers
        Shape[] fieldMarkers = new Shape[4];
        int fieldMarkerCounter = 0;
        for (Shape shape : shapes) {
            drawShape( shape );
            //if (shape.getType() == ShapeType.FIELD_MARKER) {
            //    fieldMarkers[fieldMarkerCounter] = shape;
            //    fieldMarkerCounter++;
            //}
        }
        //if (fieldMarkerCounter != 4) {
        //    System.out.println("Invalid number of field markers passed");
        //    //throw new RuntimeException("Invalid number of FieldMarkers");
        //}
        //else {
            //drawPlayField(fieldMarkers);
        //}
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

    private void drawConnectedLines(int[][] lines){
        shape_pane.getChildren().clear();
        Path linesPath = new Path();
        MoveTo moveTo = new MoveTo(lines[0][0], lines[0][1]);
        linesPath.getElements().add(moveTo);
        for(int[] line : lines){
            LineTo lineTo = new LineTo(line[2], line[3]);
            linesPath.getElements().add(lineTo);
        }
        shape_pane.getChildren().add(linesPath);

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
