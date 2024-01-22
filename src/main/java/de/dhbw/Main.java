package de.dhbw;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.Setting;
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

import static de.dhbw.statics.*;

public class Main {
    static boolean running = true;
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

        VideoInput videoIn = new VideoInput(2);
        MarkerRecognizer markerRecognizer = new MarkerRecognizer();
        ShapeProcessor shapeProcessor = new ShapeProcessor();
        PositionMarker positionMarker = new PositionMarker();
        MidiAdapter midiAdapter = new MidiAdapter();
        MidiOutputDevice midiOutputDevice = new MidiOutputDevice();
        midiOutputDevice.setMidiDevice("Gervill");
        midiOutputDevice.updateSettings(null);
        midiOutputDevice.start();

        Clock clock = new Clock(time_zero);
        clock.setTempo(120);

        Mat frame = new Mat();
        Setting setting;
        int counter = 0;
        while (running) {
            // message independent code:
            clock.tick(System.currentTimeMillis());
            videoIn.grabImage(frame);

            markerRecognizer.setFrame(frame);
            markerRecognizer.detectShapes();
            shapeProcessor.processShapes(markerRecognizer.getShapes(), frame.width(), frame.height(), frame);
            positionMarker.updatePositionMarker(shapeProcessor.getPlayfieldInfo(), clock.currentBeat);
            // message dependent / message sending code:
            if(!EventQueues.toController.isEmpty()){
                setting = EventQueues.toController.poll();
                if(setting != null) {
                    switch (setting.getType()) {
                        case VELOCITY:
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
                            // TODO find out where the information that should be displayed should be stored
                            midiAdapter.setMetronomeActive((Boolean) setting.getValue());
                            break;
                        case TEMPO:
                            clock.setTempo((int) setting.getValue() * MAX_TEMPO_SPAN + MIN_TEMPO);
                            break;
                        case PLAY:
                            clock.setPlaying((Boolean) setting.getValue());
                            break;
                        case MIDI_DEVICE:
                            midiOutputDevice.setMidiDevice((String) setting.getValue());
                            break;
                        case null, default:
                            break;
                    }
                }
            }
            midiAdapter.tickMidi(clock.currentBeat, shapeProcessor.getSoundMatrix());
            UIMessage uiMessage = new UIMessage();
            uiMessage.setFrame(shapeProcessor.getFrame());
            uiMessage.setPlayFieldInformation(shapeProcessor.getPlayfieldInfo());
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

    private static void printStats(long time) {
        System.out.println("MB used=" + (Runtime.getRuntime().totalMemory() -
                Runtime.getRuntime().freeMemory()) / (1000 * 1000) + "M");
        System.out.println("fps: " + 100f / (System.currentTimeMillis() - time) * 1000);
        System.gc();
    }
}