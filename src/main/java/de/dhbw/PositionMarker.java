package de.dhbw;

import lombok.Getter;

public class PositionMarker {

    // contains (x1, y1), (x2, y2)

    private int[][] posAsLine;
    private double relPos;
    //contains x,y, width, height
    @Getter
    private int[] posAsRect;
    // TODO better name needed #magicNumbers
    private final int beatCorrectionFactor = statics.POSITION_MARKER_RES * 2;
    int height, width, correctedBeat;
    public PositionMarker(){
        height = 0;
        width = 0;
        posAsRect = new int[4];
    }

    public void updatePositionMarker(int[] playFieldInformation, int currentBeat){
        // height is as of now always half the playfields height (2 lines)
        height = playFieldInformation[3]/2;
        width = playFieldInformation[2]/4;
        //System.out.println("Updating position marker to beat: " + currentBeat);
        //currentBeat = currentBeat/2;
        correctedBeat = currentBeat%(statics.NO_BEATS/2);
        relPos = correctedBeat /(statics.NO_BEATS/2f);
        //posAsLine[0][0] = playFieldInformation[0] + (int) (playFieldInformation[2] * ( (double) currentBeat%(statics.NO_BEATS/beatCorrectionFactor) / (double) (statics.NO_BEATS/beatCorrectionFactor)));
        //posAsLine[0][1] = currentBeat > statics.NO_BEATS/beatCorrectionFactor ? playFieldInformation[1] + height : playFieldInformation[1];
        //posAsLine[1][0] = posAsLine[0][0];
        //posAsLine[1][1] = posAsLine[0][1] + height;

        posAsRect[0] = playFieldInformation[0] + (int) ( (double) playFieldInformation[2] * relPos);
        posAsRect[1] = currentBeat >= statics.NO_BEATS/2 ? playFieldInformation[1] + height : playFieldInformation[1];
        posAsRect[2] = width;
        posAsRect[3] = height;
    }


}
