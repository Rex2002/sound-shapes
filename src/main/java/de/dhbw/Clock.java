package de.dhbw;

public class Clock {
    long time_zero;
    long diff;
    int currentBeat, currentBar;
    int tempo;
    double secondsPerBar;
    double nextRelPos, relPos;
    public Clock(long time_zero){
        this.time_zero = time_zero;
        currentBeat = 0;
    }

    /**
     * @param newTempo in BPM
     */
    public void setTempo(int newTempo){
        tempo = newTempo;
        // 60 seconds per minute, 4 beats per bar
        secondsPerBar = (double) (60 * 4)/tempo;
    }

    public int tick(long time){
        diff = time - time_zero;
        nextRelPos = (diff % (secondsPerBar * 1000f)) / (secondsPerBar * 1000f);
        if(nextRelPos < relPos){
            currentBar = ++currentBar % statics.NO_BARS;
        }
        relPos = nextRelPos;
        currentBeat = (int) (relPos * statics.NO_BEATS/statics.NO_BARS) + statics.NO_BEATS/statics.NO_BARS * currentBar;
        return currentBeat;
    }
}
