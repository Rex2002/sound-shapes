package de.dhbw;

public class Clock {
    long time_zero, diff;
    int currentBeat, currentBar, tempo;
    double secondsPerBar, nextRelPos, relPos;
    boolean playing = true;
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

    public void setPlaying(boolean p){
        if(p){
            time_zero = System.currentTimeMillis();
            playing = true;
        }
        else{
            playing = false;
        }
    }

    public int tick(long time){
        if(!playing) return 0;
        diff = time - time_zero;
        nextRelPos = (diff % (secondsPerBar * 1000f)) / (secondsPerBar * 1000f);
        if(nextRelPos < relPos){
            currentBar = ++currentBar % Statics.NO_BARS;
        }
        relPos = nextRelPos;
        currentBeat = (int) (relPos * Statics.NO_BEATS/ Statics.NO_BARS) + Statics.NO_BEATS/ Statics.NO_BARS * currentBar;
        return currentBeat;
    }
}
