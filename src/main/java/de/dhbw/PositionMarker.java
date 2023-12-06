package de.dhbw;

import lombok.Getter;

public class PositionMarker {

    private double relPos;
    //contains x,y, width, height
    @Getter
    private int[] posAsRect;
    int height, width, correctedBeat, prevPos0 = 0;
    public PositionMarker(){
        height = 0;
        width = 0;
        posAsRect = new int[4];
    }

    public void updatePositionMarker(int[] playFieldInformation, int currentBeat){
        // height is always half the playField's height (2 lines)
        height = playFieldInformation[3]/2;
        width = playFieldInformation[2]/8;
        // map to 0...3 (i.e. quarter)
        correctedBeat = currentBeat % (statics.NO_BEATS/2);
        relPos = correctedBeat / (statics.NO_BEATS/2f);

        posAsRect[0] = playFieldInformation[0] + (int) ( (double) playFieldInformation[2] * relPos);
        posAsRect[1] = currentBeat >= statics.NO_BEATS/2 ? playFieldInformation[1] + height : playFieldInformation[1];
        posAsRect[2] = width;
        posAsRect[3] = height;
        prevPos0 = posAsRect[0];
    }


}
