package de.dhbw.music;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.MidiBatchMessage;
import de.dhbw.statics;

import static de.dhbw.statics.DEFAULT_VELOCITY;

public class MidiAdapter {
    int lastInterval = -1;
    int velocity = DEFAULT_VELOCITY;
    boolean playing = true;
    boolean metronomeActive = false;
    MidiBatchMessage midiBatchMessage = new MidiBatchMessage();

    public void setVelocity(int velocity){
        this.velocity = velocity;
    }
    public void setMetronomeActive(boolean metronomeActive){
        this.metronomeActive = metronomeActive;
    }

    public void setMute(boolean muted){
        this.playing = !muted;
    }
    public void tickMidi(int posInBeat, boolean[][] soundMatrix){
        if(posInBeat != lastInterval && playing){
            midiBatchMessage.clearMessages();
            lastInterval = posInBeat;
            if(metronomeActive && posInBeat % 2 == 0){
                midiBatchMessage.addMidiMessage(statics.METRONOME_SOUND, velocity, -1);
            }
            for(int note = 0; note < soundMatrix[posInBeat].length; note++){
                if (soundMatrix[posInBeat][note]){
                    midiBatchMessage.addMidiMessage(int2Note(note), velocity, -1);
                }
            }
            EventQueues.toMidi.offer(midiBatchMessage);
        }
    }

    public int int2Note(int i){
        return switch (i) {
            case 0 -> 42;
            case 1 -> 36;
            case 2 -> 38;
            case 3 -> 49;
            case 4 -> 51;
            default -> 56;
        };
    }
}
