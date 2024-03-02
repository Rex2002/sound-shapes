package de.dhbw;

import lombok.Getter;

import static de.dhbw.Statics.DEFAULT_TIME_ENUMERATOR;

public class PositionMarker {

    //contains x,y, width, height
    @Getter
    private int[] posAsRect;
    int height, width, correctedBeat;
    private int beatsPerBar = DEFAULT_TIME_ENUMERATOR;
    private boolean doubled;

    public PositionMarker() {
        height = 0;
        width = 0;
        posAsRect = new int[4];
    }

    public void setTimeInfo(int beatsPerBar, boolean doubled) {
        this.beatsPerBar = beatsPerBar;
        this.doubled = doubled;
    }

    public void updatePositionMarker(int[] playFieldInformation, int currentBeat) {
        int factor = doubled ? 2 : 1;
        // height is always half the playField's height (2 lines)
        height = playFieldInformation[3]/2;
        width = playFieldInformation[2]/ beatsPerBar * factor;
        // map to 0...3 (i.e. quarter in case of 4/4)
        correctedBeat = (currentBeat % beatsPerBar)/2;
        double relPos = ((double) correctedBeat / beatsPerBar) * 2;

        posAsRect[0] = playFieldInformation[0] + (int) ( (double) playFieldInformation[2] * relPos);
        posAsRect[1] = currentBeat >= beatsPerBar ? playFieldInformation[1] + height : playFieldInformation[1];
        posAsRect[2] = width;
        posAsRect[3] = height;
    }


}
