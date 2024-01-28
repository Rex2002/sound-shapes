package de.dhbw;

import org.opencv.core.Scalar;

public class Statics {

    // describes how many beats should be fitted into the FULL number of bars
    // TODO maybe enhance this to only describe the number of beats per bar, but at the moment this complicates things unnecessarily
    public final static int NO_BEATS = 16;
    public final static int NO_NOTES = 5 * 3;

    // describes how many bars there are on a full screen
    public final static int NO_BARS = 2;

    //video
    public final static Scalar SHAPE_HL_COLOR = new Scalar(100,255,35);
    public final static Scalar PLAYFIELD_HL_COLOR = new Scalar(100, 100, 100);
    public final static int MAX_TEMPO = 250;

    //tempo
    public final static int MAX_TEMPO_SPAN = 120;
    public final static int DEFAULT_TEMPO = 120;
    public final static int MIN_TEMPO = 40;
    public final static int DEFAULT_VELOCITY = 80;
    public final static int MAX_VELOCITY = 127;

    //MIDI
    public final static String DEFAULT_MIDI_DEVICE = "Gervill";
    // TODO find clicking sound
    public final static int METRONOME_SOUND = 45;
}
