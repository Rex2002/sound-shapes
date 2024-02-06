package de.dhbw.communication;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MidiBatchMessage {
    // TODO think about making this normal arrays
    List<Integer> notes = new ArrayList<>();
    List<Integer> velocities = new ArrayList<>();
    List<Integer> offsets = new ArrayList<>();
    List<Integer> channels = new ArrayList<>();
    int[] ret = new int[4];
    public void clearMessages(){
        notes.clear(); velocities.clear(); offsets.clear(); channels.clear();
    }
    public void addMidiMessage(int note, int velocity, int offset, int channel){
        notes.add(note); velocities.add(velocity); offsets.add(offset); channels.add(channel);
    }
    public int[] getMidiMessage(int no){
        ret[0] = notes.get(no);
        ret[1] = velocities.get(no);
        ret[2] = offsets.get(no);
        ret[3] = channels.get(no);
        return ret;
    }

    public int getSize(){
        return notes.size();
    }
}
