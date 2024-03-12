package de.dhbw;

public class Statics {
    public final static int NO_NOTES = 5 * 3;
    //time signature
    public final static int MIN_TIME_ENUMERATOR = 1;
    public final static int MAX_TIME_ENUMERATOR = 12;
    public final static int DEFAULT_TIME_ENUMERATOR = 4;

    public final static int MIN_TIME_DENOMINATOR = 2;
    public final static int MAX_TIME_DENOMINATOR = 12;
    public final static int DEFAULT_TIME_DENOMINATOR = 4;

    //describes how many bars there are on a full screen
    public final static int NO_BARS = 2;

    //tempo
    public final static int MAX_TEMPO = 250;
    public final static int DEFAULT_TEMPO = 120;
    public final static int MIN_TEMPO = 40;
    public final static int DEFAULT_VELOCITY = 80;
    public final static int MAX_VELOCITY = 127;
    public final static int MIN_VELOCITY = 0;

    //MIDI
    public final static String DEFAULT_MIDI_DEVICE = "Gervill";
    // TODO find clicking sound
    public final static int METRONOME_SOUND = 45;
    public final static int METRONOME_UP_SOUND = 47;
}
