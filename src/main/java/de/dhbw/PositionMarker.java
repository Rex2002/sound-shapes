package de.dhbw;

import lombok.Getter;

public class PositionMarker {

    // contains (x1, y1), (x2, y2)
    @Getter
    private int[][] posAsLine;
    int height;
    public PositionMarker(){
        height = 0;
        posAsLine = new int[2][2];
    }

    public void updatePositionMarker(int[] playFieldInformation, int currentBeat){
        // height is as of now always half the playfields height (2 lines)
        height = playFieldInformation[3]/2;
        System.out.println("Updating position marker to beat: " + currentBeat);
        posAsLine[0][0] = playFieldInformation[0] + (int) (playFieldInformation[2] * ( (double) currentBeat%(statics.NO_BEATS/2) / (double) (statics.NO_BEATS/2)));
        posAsLine[0][1] = currentBeat > statics.NO_BEATS/2 ? playFieldInformation[1] : playFieldInformation[1] + height;
        posAsLine[1][0] = posAsLine[0][0];
        posAsLine[1][1] = posAsLine[0][1] + height;
    }
}
