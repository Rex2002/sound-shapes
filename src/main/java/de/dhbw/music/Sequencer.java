package de.dhbw.music;

import de.dhbw.video.shape.Shape;

import static de.dhbw.statics.*;

public class Sequencer {
    public Sequencer(){
    }
    /**
     * This method must not be called when the playfield is not valid!!!
     * @param shapes the shapes to be processed. Should only contain sound-marker shapes
     * @param playFieldInfo the info about the size and position of the playfield
     * @return a sound matrix that can be forwarded to the midi-Adapter
     */
    public boolean[][] process(Shape[] shapes, int[] playFieldInfo){
        boolean[][] soundMatrix = new boolean[NO_BEATS][NO_INSTR];
        int barOffset, beatNo;
        for(Shape s : shapes){
            barOffset = s.pos[1] - playFieldInfo[1] > playFieldInfo[3]/2 ? NO_BARS/2 : 0;
            beatNo = (int) (((s.pos[0] - playFieldInfo[0])/ (double) playFieldInfo[2]) * NO_BEATS);
            soundMatrix[barOffset * NO_BEATS/NO_BARS + beatNo][s.getForm().toInt()] = true;
        }
        return soundMatrix;
    }
}
