package de.dhbw.music;

import de.dhbw.Settings;
import de.dhbw.communication.EventQueues;
import de.dhbw.communication.MidiMessage;

public class MidiAdapter {
    int lastInterval = -1;

    public void tickMidi(int posInBeat, boolean[][] soundMatrix, Settings settings){
        if(posInBeat != lastInterval){
            lastInterval = posInBeat;
            for(int note = 0; note < soundMatrix[posInBeat].length; note++){
                if (soundMatrix[posInBeat][note]){
                    // TODO think about handling velocity / giving an option to input velocity somehow via the playfield
                    EventQueues.toMidi.offer(new MidiMessage(int2Note(note), 80, -1));
                }
            }
        }
    }

    public int int2Note(int i){
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
