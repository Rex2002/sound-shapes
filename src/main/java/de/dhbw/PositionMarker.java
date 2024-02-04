package de.dhbw;

import lombok.Getter;
import lombok.Setter;

import static de.dhbw.Statics.DEFAULT_TIME_ENUMERATOR;

public class PositionMarker {

    //contains x,y, width, height
    @Getter
    private int[] posAsRect;
    int height, width, correctedBeat, prevPos0 = 0;
    @Setter
    private int beatsPerBar = DEFAULT_TIME_ENUMERATOR;

    public PositionMarker(){
        height = 0;
        width = 0;
        posAsRect = new int[4];
    }

    public void updatePositionMarker(int[] playFieldInformation, int currentBeat){
        // height is always half the playField's height (2 lines)
        height = playFieldInformation[3]/2;
        width = playFieldInformation[2]/ beatsPerBar;
        // map to 0...3 (i.e. quarter in case of 4/4)
        correctedBeat = (currentBeat % beatsPerBar)/2;
        double relPos = ((double) correctedBeat / beatsPerBar) * 2;

        posAsRect[0] = playFieldInformation[0] + (int) ( (double) playFieldInformation[2] * relPos);
        posAsRect[1] = currentBeat >= beatsPerBar ? playFieldInformation[1] + height : playFieldInformation[1];
        posAsRect[2] = width;
        posAsRect[3] = height;
        prevPos0 = posAsRect[0];
    }


}
