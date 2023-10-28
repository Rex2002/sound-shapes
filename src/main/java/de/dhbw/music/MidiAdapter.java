package de.dhbw.music;

import de.dhbw.Settings;
import de.dhbw.communication.EventQueues;
import de.dhbw.communication.MidiMessage;

public class MidiAdapter {
    int lastInterval = -1;

    public void tickMidi(int posInBeat, boolean[][] soundMatrix, Settings settings){
        if(posInBeat != lastInterval){
            lastInterval = posInBeat;
            for(int instrNo = 0; instrNo < soundMatrix[posInBeat].length; instrNo++){
                if (soundMatrix[posInBeat][instrNo]){
                    // TODO think about handling speed / giving an option to input speed somehow via the playfield
                    EventQueues.toMidi.offer(new MidiMessage(int2Instr(instrNo), 80, -1));
                }
            }
        }
    }

    public int int2Instr(int i){
        return switch (i) {
            case 0 -> 42;
            case 1 -> 54;
            case 2 -> 37;
            case 3 -> 39;
            case 4 -> 64;
            default -> 32;
        };
    }
}
