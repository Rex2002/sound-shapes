package de.dhbw;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.Setting;
import de.dhbw.communication.SettingType;
import de.dhbw.communication.UIMessage;
import de.dhbw.music.MidiAdapter;
import de.dhbw.music.MidiOutputDevice;
import de.dhbw.ui.App;
import de.dhbw.video.MarkerRecognizer;
import de.dhbw.video.ShapeProcessor;
import de.dhbw.video.VideoInput;
import javafx.application.Application;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;

import static de.dhbw.Statics.*;

public class Main {
    static boolean running = true;
    static boolean stopped = false;
    public static void main(String[] args) {
        OpenCV.loadLocally();
        Thread uiThread = new Thread( () -> Application.launch( App.class, args) );
        uiThread.start();
        runController();
    }

    public static void runController(){
        // time_zero is not set to zero to optionally allow sync wit ext. clocks later on by simply manipulating time_zero
        long time_zero = System.currentTimeMillis();
        long time = time_zero;

        VideoInput videoIn = new VideoInput(DEFAULT_CAMERA_DEVICE);
        MarkerRecognizer markerRecognizer = new MarkerRecognizer();
        ShapeProcessor shapeProcessor = new ShapeProcessor();
        PositionMarker positionMarker = new PositionMarker();
        MidiAdapter midiAdapter = new MidiAdapter();
        MidiOutputDevice midiOutputDevice = new MidiOutputDevice();
        midiOutputDevice.setMidiDevice(DEFAULT_MIDI_DEVICE);
        midiOutputDevice.initialize();
        midiOutputDevice.updateSettings(null);
        midiOutputDevice.start();
        midiAdapter.setChannel(9);

        Clock clock = new Clock(time_zero);
        clock.setTempo(DEFAULT_TEMPO);

        Mat frame = new Mat();
        Setting<?> setting;
        int counter = 0;
        while (running) {
            // message dependent / message sending code:
            if(!EventQueues.toController.isEmpty()) {
                setting = EventQueues.toController.poll();
                if(setting != null) {
                    switch (setting.getType()) {
                        case CM_VELOCITY:
                            midiAdapter.setVelocity((int) ((double) setting.getValue() * MAX_VELOCITY));
                            UIMessage veloMsg = new UIMessage( new Setting<>( SettingType.CM_VELOCITY, (double) setting.getValue() ) );
                            EventQueues.toUI.offer(veloMsg);
                            break;
                        case GUI_VELOCITY:
                            midiAdapter.setVelocity((int) ((double) setting.getValue() * MAX_VELOCITY));
                            break;
                        case MUTE:
                            midiAdapter.setMute((Boolean) setting.getValue());
                            if(!(Boolean) setting.getValue()){
                                shapeProcessor.setLastVelocity(0.5);
                                midiAdapter.setVelocity((int) (0.5 * MAX_VELOCITY));
                            }
                            break;
                        case METRONOME:
                            midiAdapter.setMetronomeActive((Boolean) setting.getValue());
                            break;
                        case CM_TEMPO:
                            clock.setTempo((int) Math.round((double) setting.getValue() * MAX_TEMPO + MIN_TEMPO));
                            UIMessage tempoMsg = new UIMessage( new Setting<>( SettingType.CM_TEMPO, (double) setting.getValue() ) );
                            EventQueues.toUI.offer(tempoMsg);
                            break;
                        case GUI_TEMPO:
                            clock.setTempo((int) Math.round((double) setting.getValue() * MAX_TEMPO + MIN_TEMPO));
                            break;
                        case PLAY:
                            clock.setPlaying((Boolean) setting.getValue());
                            break;
                        case MIDI_DEVICE:
                            midiOutputDevice.setMidiDevice((String) setting.getValue());
                            break;
                        case CAMERA:
                            videoIn.releaseCap();
                            videoIn.setInputDevice((int) setting.getValue());
                            EventQueues.toUI.add( new UIMessage( new Setting<>(SettingType.CAMERA, true) ) );
                            break;
                        case STOP_LOOP:
                            stopped = (boolean) setting.getValue();
                            if (stopped) {
                                videoIn.releaseCap();
                                EventQueues.toUI.add( new UIMessage( new Setting<>(SettingType.STOP_LOOP, false) ) );
                            }
                            else{
                                // TODO find out how to reset to the prev value
                                videoIn.setInputDevice(0);
                            }
                            break;
                        case TOGGLE_CM:
                            shapeProcessor.setEnableControlMarker((boolean) setting.getValue());
                            break;
                        case CHANNEL_CHG:
                            int v = ((boolean) setting.getValue()) ? 9 : 10;
                            midiAdapter.setChannel(v);
                            break;
                        case TIME_SIGNATURE:
                            Integer[] timeSignature = (Integer[]) setting.getValue();
                            int resolution = getTimeResolution(timeSignature);
                            positionMarker.setTimeInfo(resolution, resolution != timeSignature[0]);
                            clock.setBeatsPerBar(resolution);
                            shapeProcessor.setBeatsPerBar(resolution);
                            midiAdapter.setTimeInfo(resolution, resolution != timeSignature[0]);
                            break;
                        case QUIT:
                            running = false;
                        case null, default:
                            break;
                    }
                }
            }
            if (stopped) continue;

            // message independent code:
            clock.tick(System.currentTimeMillis());
            videoIn.grabImage(frame);
            markerRecognizer.setFrame(frame);
            markerRecognizer.detectShapes();
            shapeProcessor.processShapes(markerRecognizer.getShapes(), frame);
            positionMarker.updatePositionMarker(shapeProcessor.getPlayFieldInfo(), clock.currentBeat);

            midiAdapter.tickMidi(clock.currentBeat, shapeProcessor.getSoundMatrix());

            if (shapeProcessor.getFrame() == null) continue;

            UIMessage uiMessage = new UIMessage();
            uiMessage.setFrame(shapeProcessor.getFrame());
            uiMessage.setPlayFieldInformation(shapeProcessor.getPlayFieldInfo());
            uiMessage.setPositionMarker(positionMarker.getPosAsRect());
            uiMessage.setShapes(markerRecognizer.getShapes());
            if(EventQueues.toUI.size() < 19) {
                EventQueues.toUI.add(uiMessage);
            }

            if (counter % 100 == 0) {
                printStats(time);
                counter = 0;
                time = System.currentTimeMillis();
            }
            counter++;
        }
        videoIn.releaseCap();
        midiOutputDevice.stopDevice();
    }

    private static int getTimeResolution(Integer[] timeSignature) {
        return timeSignature[1] == 4 ? timeSignature[0] * 2 : timeSignature[0];
    }

    private static void printStats(long time) {
        System.out.println("MB used=" + (Runtime.getRuntime().totalMemory() -
                Runtime.getRuntime().freeMemory()) / (1000 * 1000) + "M");
        System.out.println("fps: " + 100f / (System.currentTimeMillis() - time) * 1000);
    }
}