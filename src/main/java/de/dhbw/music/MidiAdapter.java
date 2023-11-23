package de.dhbw.music;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.MidiMessage;

import static de.dhbw.statics.DEFAULT_VELOCITY;

public class MidiAdapter {
    int lastInterval = -1;
    int velocity = DEFAULT_VELOCITY;
    boolean playing = true;

    public void setVelocity(int velocity){
        this.velocity = velocity;
    }

    public void setMute(boolean muted){
        this.playing = !muted;
    }
    public void tickMidi(int posInBeat, boolean[][] soundMatrix){
        if(posInBeat != lastInterval && playing){
            lastInterval = posInBeat;
            for(int note = 0; note < soundMatrix[posInBeat].length; note++){
                if (soundMatrix[posInBeat][note]){
                    // TODO think about handling velocity / giving an option to input velocity somehow via the playfield
                    EventQueues.toMidi.offer(new MidiMessage(int2Note(note), velocity, -1));
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
