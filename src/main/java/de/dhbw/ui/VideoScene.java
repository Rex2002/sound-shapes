package de.dhbw.ui;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.Setting;
import de.dhbw.communication.SettingType;
import de.dhbw.communication.UIMessage;
import de.dhbw.video.shape.Shape;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
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
    private GridPane menu_grid;

    @FXML
    private ImageView mute_btn;
    @FXML
    private ImageView play_btn;
    @FXML
    private ImageView metronome_btn;

    @FXML
    private FlowPane settings_tab;
    @FXML
    private ImageView settings_btn;
    @FXML
    private FlowPane settings_pane;
    @FXML
    private ChoiceBox<String> midi_choicebox;

    @FXML
    private FlowPane music_tab;
    @FXML
    private ImageView music_btn;
    @FXML
    private FlowPane music_pane;
    @FXML
    private TextField tempo_field;
    @FXML
    private TextField time_field_enumerator;
    @FXML
    private TextField time_field_denominator;

    private CheckQueueService checkQueueService;
    private ResourceProvider resourceProvider;
    private boolean playing = true;
    private boolean metronomeRunning = false;
    private boolean mute = false;
    private boolean settingsVisible = false;
    private boolean musicPaneVisible = false;
    private double aspectRatioFrame;
    private double frameWidth = -1;
    private double scaleRatio;
    ChangeListener<? super Number> sizeChangeListener;
    private int tempo = 120;
    private int[] timeSignature = {4, 4};

    @FXML
    private void initialize() {
        //bind ImageView dimensions to parent
        currentFrame.fitWidthProperty().bind(stack.widthProperty());
        currentFrame.fitHeightProperty().bind(stack.heightProperty());

        sizeChangeListener = (observable, oldValue, newValue) -> setUIDimensions();
        stack.widthProperty().addListener(sizeChangeListener);

        tempo_field.setTextFormatter( new TextFormatter<>( new IntegerStringConverter() ) );
        time_field_enumerator.setTextFormatter( new TextFormatter<>( new IntegerStringConverter() ) );
        time_field_denominator.setTextFormatter( new TextFormatter<>( new IntegerStringConverter() ) );

        resourceProvider = new ResourceProvider();
        checkQueueService = new CheckQueueService();
        checkQueueService.setPeriod(Duration.millis(33));
        checkQueueService.setOnSucceeded((event) -> handleQueue());
        checkQueueService.start();
        EventQueues.toUI.clear();
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
            if (message.getPlayFieldInformation() != null && message.getPlayFieldInformation()[4] == 1){
                fieldPane.getChildren().clear();
                drawPlayField( message.getPlayFieldInformation() );
                if (message.getPositionMarker() != null){
                    drawPositionMarker(message.getPositionMarker());
                }
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

        List<Line> lines = new ArrayList<>();
        Line l = new Line(playFieldInfo[0], playFieldInfo[1] + playFieldInfo[3]/2, playFieldInfo[0] + playFieldInfo[2], playFieldInfo[1]+playFieldInfo[3]/2 );
        l.setStroke(Color.YELLOW);
        lines.add(l);
        double x;
        // TODO maybe remove magic numbers here
        for(int i = 1; i < 4; i++){
            x = playFieldInfo[0] + playFieldInfo[2] * i/4;
            l = new Line(x,playFieldInfo[1], x, playFieldInfo[1] + playFieldInfo[3]);
            l.setStroke(Color.YELLOW);
            lines.add(l);
        }
        playfield.setStroke(Color.YELLOW);
        playfield.setFill(Color.TRANSPARENT);
        fieldPane.getChildren().addAll(lines);
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
        Setting<Boolean> setting = new Setting<>(SettingType.PLAY, playing);
        EventQueues.toController.add(setting);
        String iconPath = "src/main/resources/icons/" + (playing ? "stop_blue.png" : "play_blue.png");
        play_btn.setImage( new Image( resourceProvider.getResource(iconPath).toURI().toString() ) );
    }

    @FXML
    private void toggleMetronome() {
        metronomeRunning = !metronomeRunning;
        Setting<Boolean> setting = new Setting<>(SettingType.METRONOME, metronomeRunning);
        EventQueues.toController.add(setting);
        String iconPath = "src/main/resources/icons/" + (metronomeRunning ? "metronome_on_blue.png" : "metronome_off_blue.png");
        metronome_btn.setImage( new Image( resourceProvider.getResource(iconPath).toURI().toString() ) );
    }

    @FXML
    private void toggleMute() {
        mute = !mute;
        Setting<Boolean> setting = new Setting<>(SettingType.MUTE, mute);
        EventQueues.toController.add(setting);
        String iconPath = "src/main/resources/icons/" + (mute ? "volume_mute_blue.png" : "volume_max_blue.png");
        mute_btn.setImage( new Image( resourceProvider.getResource(iconPath).toURI().toString() ) );
    }

    @FXML
    private void toggleSettingsPane() {
        if (musicPaneVisible) {
            toggleMusicPane();
        }
        settings_pane.setVisible(!settingsVisible);
        settingsVisible = !settingsVisible;
        if (settingsVisible) {
            refreshSettingsPane();
        }
    }

    private void refreshSettingsPane() {
        MidiDevice.Info[] deviceInfos = MidiSystem.getMidiDeviceInfo();
        List<String> devices = new ArrayList<>();
        for (MidiDevice.Info deviceInfo : deviceInfos) {
            if (deviceInfo.getName().contains("Sequencer")) {
                continue;
            }
            devices.add(deviceInfo.getName());
        }
        midi_choicebox.getItems().clear();
        midi_choicebox.getItems().addAll(devices);
    }

    @FXML
    private void toggleMusicPane() {
        if (settingsVisible) {
            toggleSettingsPane();
        }
        music_pane.setVisible(!musicPaneVisible);
        musicPaneVisible = !musicPaneVisible;
        if (musicPaneVisible) {
            refreshMusicPane();
        }
    }

    private void refreshMusicPane() {
        tempo_field.setText(String.valueOf(tempo));
        time_field_enumerator.setText(String.valueOf(timeSignature[0]));
        time_field_denominator.setText(String.valueOf(timeSignature[1]));
    }

    @FXML
    private void sendMidiSetting() {
        Setting<String> setting = new Setting<>(SettingType.MIDI_DEVICE, midi_choicebox.getValue());
        EventQueues.toController.add(setting);
    }

    @FXML
    private void sendTempoSetting() {
        enforceTempoLimits( Integer.parseInt(tempo_field.getText()) );
        Setting<Integer> setting = new Setting<>( SettingType.TEMPO, tempo );
        EventQueues.toController.add(setting);
    }

    @FXML
    private void sendTimeSignatureSetting() {
        enforceTimeSignatureLimits(
                Integer.parseInt(time_field_enumerator.getText()),
                Integer.parseInt(time_field_denominator.getText())
        );
        Setting<int[]> setting = new Setting<>(SettingType.TIME_SIGNATURE, timeSignature);
        EventQueues.toController.add(setting);
    }

    private void enforceTimeSignatureLimits(int enumerator, int denominator) {
        if (enumerator > 12) enumerator = 12;
        if (enumerator < 1) enumerator = 1;

        if (denominator > 12) denominator = 12;
        if (denominator < 2) denominator = 2;

        time_field_enumerator.setText(String.valueOf(enumerator));
        time_field_denominator.setText(String.valueOf(denominator));
        timeSignature[0] = enumerator;
        timeSignature[1] = denominator;
    }

    private void enforceTempoLimits(int input) {
        if (input > 250) input = 250;
        if (input < 40) input = 40;
        tempo_field.setText(String.valueOf(input));
        tempo = input;
    }

}
