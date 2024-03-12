package de.dhbw.ui;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.Setting;
import de.dhbw.communication.SettingType;
import de.dhbw.communication.UIMessage;
import de.dhbw.video.shape.Shape;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
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
import org.opencv.videoio.VideoCapture;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import java.io.ByteArrayInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static de.dhbw.Statics.*;

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
    private ChoiceBox<String> camera_choicebox;
    @FXML
    private Button cm_button;

    @FXML
    private FlowPane music_tab;
    @FXML
    private ImageView music_btn;
    @FXML
    private FlowPane music_pane;
    @FXML
    private TextField tempo_field;
    @FXML
    private TextField velocity_field;
    @FXML
    private Slider velocity_slider;
    @FXML
    private ChoiceBox<Integer> time_field_enumerator;
    @FXML
    private ChoiceBox<Integer> time_field_denominator;

    private CheckQueueService checkQueueService;
    private final ResourceProvider resourceProvider = new ResourceProvider();
    private boolean playing = false;
    private boolean metronomeRunning = false;
    private boolean mute = false;
    private boolean settingsVisible = false;
    private boolean controlMarkersEnabled = true;
    private boolean musicPaneVisible = false;
    private double aspectRatioFrame;
    private double frameWidth = -1;
    private double scaleRatio;
    ChangeListener<? super Number> sizeChangeListener;
    private final Integer[] timeSignature = {DEFAULT_TIME_ENUMERATOR, DEFAULT_TIME_DENOMINATOR};
    private final Integer[][] timeEnumerators = {new Integer[5], new Integer[12]};

    @FXML
    private void initialize() {
        //bind ImageView dimensions to parent
        currentFrame.fitWidthProperty().bind(stack.widthProperty());
        currentFrame.fitHeightProperty().bind(stack.heightProperty());

        sizeChangeListener = (observable, oldValue, newValue) -> setUIDimensions();
        stack.widthProperty().addListener(sizeChangeListener);

        camera_choicebox.getItems().add("0");

        tempo_field.setTextFormatter( new TextFormatter<>( new IntegerStringConverter() ) );
        tempo_field.setText(String.valueOf(DEFAULT_TEMPO));
        velocity_field.setTextFormatter( new TextFormatter<>( new IntegerStringConverter() ) );
        velocity_field.setText(String.valueOf(DEFAULT_VELOCITY));
        velocity_slider.setMin(MIN_VELOCITY);
        velocity_slider.setMax(MAX_VELOCITY);
        velocity_slider.setValue(DEFAULT_VELOCITY);
        velocity_field.textProperty().bindBidirectional( velocity_slider.valueProperty(), NumberFormat.getIntegerInstance() );

        for (int i = 0; i < 12; i++) {
            timeEnumerators[1][i] = i + 1;
            if (i < 5) timeEnumerators[0][i] = i +1;
        }
        time_field_enumerator.getItems().addAll( timeEnumerators[0] );
        time_field_enumerator.setValue(DEFAULT_TIME_ENUMERATOR);
        time_field_denominator.getItems().addAll(4,8);
        time_field_denominator.setValue(DEFAULT_TIME_DENOMINATOR);

        checkQueueService = new CheckQueueService();
        checkQueueService.setPeriod(Duration.millis(33));
        checkQueueService.setOnSucceeded((event) -> handleQueue());
        checkQueueService.start();
        EventQueues.toUI.clear();

        root.setOnKeyPressed(this::handleKeyStroke);
    }

    private void handleKeyStroke(KeyEvent event) {
        if (event.isShortcutDown()) {
            switch (event.getCode()) {
                case COMMA -> toggleSettingsPane();
                case M -> toggleMusicPane();
                case Q -> {
                    Platform.exit();
                    EventQueues.toController.add(new Setting<>(SettingType.QUIT, 0));
                }
            }
        } else {
            switch (event.getCode()) {
                case SPACE -> togglePlayPause();
                case M -> toggleMute();
                case K -> toggleMetronome();
            }
        }
    }
    private void handleQueue() {
        List<UIMessage> messages = checkQueueService.getValue();
        for (UIMessage message : messages) {
            if (message.getFrame() != null && !message.getFrame().empty()) {
                updateFrame( message.getFrame() );
            }
            if (message.getSetting() != null) {
                switch (message.getSetting().getType()) {
                    case CM_VELOCITY:
                        setVelocityIcon( (double) message.getSetting().getValue() );
                        int value = (int) Math.round((double) message.getSetting().getValue() * MAX_VELOCITY);
                        velocity_field.setText(String.valueOf(value));
                        break;
                    case CM_TEMPO:
                        int tempo = (int) Math.round((double) message.getSetting().getValue() * MAX_TEMPO + MIN_TEMPO);
                        tempo_field.setText(String.valueOf(tempo));
                        break;
                    case STOP_LOOP:
                        if (! (boolean) message.getSetting().getValue() ) {
                            getCameraIndices();
                        }
                        break;
                    case CAMERA:
                        fieldPane.getChildren().clear();
                        break;
                    case null, default:
                        break;
                }
                //do something
            }
            if (message.getShapes() != null) {
                processShapes( message.getShapes() );
            }
            if (message.getPlayFieldInformation() != null && message.getPlayFieldInformation()[4] == 1) {
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
        Rectangle playField = new Rectangle(playFieldInfo[0], playFieldInfo[1], playFieldInfo[2], playFieldInfo[3]);

        List<Line> lines = new ArrayList<>();
        Line l = new Line(playFieldInfo[0], playFieldInfo[1] + playFieldInfo[3]/2, playFieldInfo[0] + playFieldInfo[2], playFieldInfo[1]+playFieldInfo[3]/2 );
        l.setStroke(Color.YELLOW);
        lines.add(l);
        double x;
        for (int i = 1; i < timeSignature[0]; i++) {
            x = playFieldInfo[0] + playFieldInfo[2] * i/timeSignature[0];
            l = new Line(x,playFieldInfo[1], x, playFieldInfo[1] + playFieldInfo[3]);
            l.setStroke(Color.YELLOW);
            lines.add(l);
        }
        //add eighths subdivisions
        if (timeSignature[1] == 4) {
            for (int i = 1; i < timeSignature[0] * 2; i = i + 2) {
                x = playFieldInfo[0] + playFieldInfo[2] * i/(timeSignature[0]*2);
                l = new Line(x,playFieldInfo[1], x, playFieldInfo[1] + playFieldInfo[3] * 0.1);
                l.setStroke(Color.YELLOW);
                lines.add(l);
                l = new Line(x,playFieldInfo[1] + playFieldInfo[3] * 0.4, x, playFieldInfo[1] + playFieldInfo[3] * 0.6);
                l.setStroke(Color.YELLOW);
                lines.add(l);
                l = new Line(x,playFieldInfo[1] + playFieldInfo[3] * 0.9, x, playFieldInfo[1] + playFieldInfo[3]);
                l.setStroke(Color.YELLOW);
                lines.add(l);
            }
        }
        playField.setStroke(Color.YELLOW);
        playField.setFill(Color.TRANSPARENT);
        fieldPane.getChildren().addAll(lines);
        fieldPane.getChildren().add(playField);
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

    private void setVelocityIcon(double value) {
        String iconPath = "src/main/resources/icons/";
        if (value == 0.0) {
            iconPath += "volume_mute_blue.png";
        } else if (value < 0.4) {
            iconPath += "volume_low_blue.png";
        } else if (value < 0.8) {
            iconPath += "volume_mid_blue.png";
        } else {
            iconPath += "volume_high_blue.png";
        }
        mute_btn.setImage( new Image( resourceProvider.getResource(iconPath).toURI().toString() ) );
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
        String iconPath = "src/main/resources/icons/" + (mute ? "volume_mute_blue.png" : "volume_high_blue.png");
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
        } else if (!musicPaneVisible) {
            root.requestFocus();
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
        String currentDevice = midi_choicebox.getValue();
        midi_choicebox.getItems().clear();
        midi_choicebox.getItems().addAll(devices);
        midi_choicebox.setValue( devices.contains(currentDevice) ? currentDevice : DEFAULT_MIDI_DEVICE );
    }

    @FXML
    private void stopVideoProcessing() {
        Setting<Boolean> stop = new Setting<>(SettingType.STOP_LOOP, true);
        EventQueues.toController.add(stop);
    }

    /**
     * Gets list of working cameras' indices by trying to connect to indices 0 through 9.
     */
    private void getCameraIndices() {
        List<String> cameras = new ArrayList<>(10);
        VideoCapture cap;
        for (int i = 0; i < 10; i++) {
            try {
                cap = new VideoCapture(i);
            } catch (Exception e) {
                continue;
            }
            if (cap.isOpened()) {
                cameras.add( String.valueOf(i) );
            }
            cap.release();
        }

        String currentCam = camera_choicebox.getValue();
        camera_choicebox.getItems().clear();
        camera_choicebox.getItems().addAll(cameras);
        camera_choicebox.setValue( cameras.contains(currentCam) ? currentCam : "0" );
        Setting<Boolean> restart = new Setting<>(SettingType.STOP_LOOP, false);
        EventQueues.toController.add(restart);
    }

    @FXML
    private void toggleControlMarkers() {
        controlMarkersEnabled = !controlMarkersEnabled;
        Setting<Boolean> setting = new Setting<>(SettingType.TOGGLE_CM, controlMarkersEnabled);
        EventQueues.toController.add(setting);

        cm_button.setText(controlMarkersEnabled ? "Disable" : "Enable");
    }

    @FXML
    private void toggleMusicPane() {
        if (settingsVisible) {
            toggleSettingsPane();
        }
        music_pane.setVisible(!musicPaneVisible);
        musicPaneVisible = !musicPaneVisible;
        if (!settingsVisible && !musicPaneVisible) {
            root.requestFocus();
        }
    }

    @FXML
    private void sendMidiSetting() {
        if (midi_choicebox.getValue() == null) return;
        Setting<String> setting = new Setting<>(SettingType.MIDI_DEVICE, midi_choicebox.getValue());
        EventQueues.toController.add(setting);
    }

    @FXML
    private void sendCameraSetting() {
        if (camera_choicebox.getValue() == null) return;
        Setting<Integer> setting = new Setting<>(SettingType.CAMERA, Integer.parseInt(camera_choicebox.getValue()) );
        EventQueues.toController.add(setting);
    }

    @FXML
    private void decreaseTempoTen() {
        changeTempo(-10);
    }

    @FXML
    private void decreaseTempoFive() {
        changeTempo(-5);
    }

    @FXML
    private void increaseTempoFive() {
        changeTempo(5);
    }

    @FXML
    private void increaseTempoTen() {
        changeTempo(10);
    }

    private void changeTempo(int value) {
        value = enforceValueLimits( Integer.parseInt(tempo_field.getText()) + value, MIN_TEMPO, MAX_TEMPO );
        tempo_field.setText(String.valueOf(value));
        sendTempoSetting();
    }

    @FXML
    private void sendTempoSetting() {
        int value = enforceValueLimits( Integer.parseInt(tempo_field.getText()), MIN_TEMPO, MAX_TEMPO );
        tempo_field.setText(String.valueOf(value));

        double normalizedTempo = (value - MIN_TEMPO) / (double) MAX_TEMPO;
        Setting<Double> setting = new Setting<>( SettingType.GUI_TEMPO, normalizedTempo );
        EventQueues.toController.add(setting);
    }

    @FXML
    private void sendVelocitySetting() {
        int value = enforceValueLimits( Integer.parseInt(velocity_field.getText()), MIN_VELOCITY, MAX_VELOCITY );
        velocity_field.setText(String.valueOf(value));
        double normalizedValue = value / (double) MAX_VELOCITY;
        setVelocityIcon( normalizedValue );

        Setting<Double> setting = new Setting<>( SettingType.GUI_VELOCITY, normalizedValue );
        EventQueues.toController.add(setting);
    }

    @FXML
    private void handleTimeDenominator() {
        int value = time_field_enumerator.getValue();
        time_field_enumerator.getItems().clear();
        if (time_field_denominator.getValue() == 4) {
            time_field_enumerator.getItems().addAll(timeEnumerators[0]);
            time_field_enumerator.setValue(Math.min(value, 5));
        } else {
            time_field_enumerator.getItems().addAll(timeEnumerators[1]);
            time_field_enumerator.setValue(value);
        }
        sendTimeSignatureSetting();
    }
    @FXML
    private void sendTimeSignatureSetting() {
        if (time_field_enumerator.getValue() == null || time_field_denominator.getValue() == null) return;
        timeSignature[0] = time_field_enumerator.getValue();
        timeSignature[1] = time_field_denominator.getValue();
        Setting<Integer[]> setting = new Setting<>(SettingType.TIME_SIGNATURE, timeSignature);
        EventQueues.toController.add(setting);
    }

    private int enforceValueLimits(int input, int min, int max) {
        if (input > max) input = max;
        if (input < min) input = min;
        return input;
    }
}
