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
        Settings settings = new Settings(120);
        Setting setting;

        VideoInput videoIn = new VideoInput(2);
        MarkerRecognizer markerRecognizer = new MarkerRecognizer();
        ShapeProcessor shapeProcessor = new ShapeProcessor();
        MidiAdapter midiAdapter = new MidiAdapter();
        MidiOutputDevice midiOutputDevice = new MidiOutputDevice();
        midiOutputDevice.setMidiDevice("MPK");
        midiOutputDevice.updateSettings(null);
        midiOutputDevice.start();
        Clock clock = new Clock(time_zero);
        clock.setTempo(settings.tempo);
        Mat frame = new Mat();
        int counter = 0;
        while (running){
            // message independent code:
            clock.tick(System.currentTimeMillis());
            videoIn.grabImage(frame);

            markerRecognizer.setFrame(frame);
            markerRecognizer.detectShapes();
            shapeProcessor.processShapes(markerRecognizer.getShapes(), frame.width(), frame.height());
            // message dependent / message sending code:
            if(!EventQueues.toController.isEmpty()){
                setting = EventQueues.toController.poll();
                if(setting != null) {
                    switch (setting.getType()) {
                        case VELOCITY:
                            midiAdapter.setVelocity((int) (setting.getValue() * MAX_VELOCITY));
                            break;
                        case MUTE:
                            midiAdapter.setMute(!(setting.getValue() > 0.5));
                            break;
                        case METRONOME:
                            // TODO find out where the information that should be displayed should be stored
                            clock.setTempo((int) (setting.getValue() * MAX_TEMPO_SPAN + MIN_TEMPO));
                            break;
                        case PLAY:
                            clock.setPlaying(setting.getValue() > 0.5);
                            //midiAdapter.setMute(!(setting.getValue() > 0.5));
                            break;
                        case null, default:
                            break;
                    }
                }
            }
            midiAdapter.tickMidi(clock.currentBeat, shapeProcessor.getSoundMatrix());


            EventQueues.toUI.offer(new UIMessage(shapeProcessor.getPlayfieldInfo()));
            EventQueues.toUI.offer(new UIMessage(markerRecognizer.getShapes()));

            // TODO does it make a difference if the frame-offering is at the end
            EventQueues.toUI.offer(new UIMessage(markerRecognizer.getFrame()));

            if (counter % 100 == 0) {
                printStats(time);
                counter = 0;
                time = System.currentTimeMillis();
            }
            counter++;
        }
        videoIn.releaseCap();
        midiOutputDevice.release();

    }

    private static void printStats(long time) {
        System.out.println("MB used=" + (Runtime.getRuntime().totalMemory() -
                Runtime.getRuntime().freeMemory()) / (1000 * 1000) + "M");
        System.out.println("fps: " + 100f / (System.currentTimeMillis() - time) * 1000);
        System.gc();
    }
}