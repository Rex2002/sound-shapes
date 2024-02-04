package de.dhbw;

public class Clock {
    long time_zero, diff;
    int currentBeat, currentBar, tempo;
    double secondsPerBar, nextRelPos, relPos;
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
        // 60 seconds per minute, 4 beats per bar
        secondsPerBar = (double) (60 * 4)/tempo;
    }

    public void setPlaying(boolean p){
        if(p){
            playing = true;
            time_zero = System.currentTimeMillis();
            relPos = 0;
            currentBar = 0;
        }
        else{
            playing = false;
        }
    }

    public void tick(long time){
        if(!playing) return;
        diff = time - time_zero;
        nextRelPos = (diff % (secondsPerBar * 1000f)) / (secondsPerBar * 1000f);
        if(nextRelPos < relPos){
            currentBar = ++currentBar % Statics.NO_BARS;
        }
        relPos = nextRelPos;
        currentBeat = (int) (relPos * Statics.NO_BEATS/ Statics.NO_BARS) + Statics.NO_BEATS/ Statics.NO_BARS * currentBar;
    }
}
