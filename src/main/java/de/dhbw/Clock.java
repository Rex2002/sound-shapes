package de.dhbw;

import static de.dhbw.Statics.DEFAULT_TIME_ENUMERATOR;
import static de.dhbw.Statics.NO_BARS;

public class Clock {
    long time_zero, diff;
    int currentBeat, currentBar, tempo;
    double secondsPerBar, nextRelPos, relPos;
    private int beatsPerBar = DEFAULT_TIME_ENUMERATOR * 2;
    boolean playing = false;
    public Clock(long time_zero){
        this.time_zero = time_zero;
        currentBeat = -1;
    }

    /**
     * @param newTempo in BPM
     */
    public void setTempo(int newTempo){
        tempo = newTempo;
        // 60 seconds per minute, time enumerator sets beats per bar
        secondsPerBar = (double) (60 * beatsPerBar)/tempo;
    }

    public void setBeatsPerBar(int beatsPerBar) {
        this.beatsPerBar = beatsPerBar;
        secondsPerBar = (double) (60 * beatsPerBar)/tempo;
    }

    public void setPlaying(boolean p){
        if(p) {
            playing = true;
            time_zero = System.currentTimeMillis();
            relPos = 0;
            currentBar = 0;
        }
        else {
            playing = false;
        }
    }

    public void tick(long time) {
        if(!playing) return;
        diff = time - time_zero;
        nextRelPos = (diff % (secondsPerBar * 1000f)) / (secondsPerBar * 1000f);
        if(nextRelPos < relPos){
            currentBar = ++currentBar % NO_BARS;
        }
        relPos = nextRelPos;
        currentBeat = (int) (relPos * beatsPerBar) + beatsPerBar * currentBar;
    }
}
