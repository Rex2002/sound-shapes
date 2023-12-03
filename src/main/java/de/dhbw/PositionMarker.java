package de.dhbw;

import lombok.Getter;

public class PositionMarker {

    // contains (x1, y1), (x2, y2)
    @Getter
    private int[][] posAsLine;
    // TODO better name needed #magicNumbers
    private final int beatCorrectionFactor = statics.POSITION_MARKER_RES * 2;
    int height;

    public PositionMarker(){
        height = 0;
        posAsLine = new int[2][2];
    }

    public void updatePositionMarker(int[] playFieldInformation, int currentBeat){
        // height is as of now always half the playfields height (2 lines)
        height = playFieldInformation[3]/2;
        //System.out.println("Updating position marker to beat: " + currentBeat);
        currentBeat = currentBeat/statics.POSITION_MARKER_RES;
        posAsLine[0][0] = playFieldInformation[0] + (int) (playFieldInformation[2] * ( (double) currentBeat%(statics.NO_BEATS/beatCorrectionFactor) / (double) (statics.NO_BEATS/beatCorrectionFactor)));
        posAsLine[0][1] = currentBeat > statics.NO_BEATS/beatCorrectionFactor ? playFieldInformation[1] + height : playFieldInformation[1];
        posAsLine[1][0] = posAsLine[0][0];
        posAsLine[1][1] = posAsLine[0][1] + height;
    }
}
