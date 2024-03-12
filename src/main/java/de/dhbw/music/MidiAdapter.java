package de.dhbw.music;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.MidiBatchMessage;
import de.dhbw.Statics;
import lombok.Setter;

import static de.dhbw.Statics.*;

public class MidiAdapter {
    int lastInterval = -1;
    @Setter
    int velocity = DEFAULT_VELOCITY;
    boolean playing = true;
    @Setter
    boolean metronomeActive = false;
    final MidiBatchMessage midiBatchMessage = new MidiBatchMessage();
    @Setter
    private int beatsPerBar = DEFAULT_TIME_ENUMERATOR * 2;

    public void setMute(boolean muted) {
        this.playing = !muted;
    }
    public void tickMidi(int posInBeat, boolean[][] soundMatrix) {
        if(posInBeat != lastInterval && playing){
            midiBatchMessage.clearMessages();
            lastInterval = posInBeat;
            if(metronomeActive && posInBeat % NO_BARS == 0) {
                if(posInBeat == 0 || posInBeat == beatsPerBar) {
                    midiBatchMessage.addMidiMessage(Statics.METRONOME_UP_SOUND, velocity, -1);
                }
                else {
                    midiBatchMessage.addMidiMessage(Statics.METRONOME_SOUND, velocity, -1);
                }
            }
            if(soundMatrix != null) {
                for (int note = 0; note < soundMatrix[posInBeat].length; note++) {
                    if (soundMatrix[posInBeat][note]) {
                        midiBatchMessage.addMidiMessage(int2Note(note), velocity, -1);
                    }
                }
            }
            EventQueues.toMidi.offer(midiBatchMessage);
        }
    }

    // TODO rework sound numbers, since it just repeats thrice at the moment
    public int int2Note(int i){
        return switch (i) {
            case 0 -> 42;
            case 1 -> 36;
            case 2 -> 38;
            case 3 -> 49;
            case 4 -> 51;
            case 5 -> 42;
            case 6 -> 36;
            case 7 -> 38;
            case 8 -> 49;
            case 9 -> 51;
            case 10 -> 42;
            case 11 -> 36;
            case 12 -> 38;
            case 13 -> 49;
            case 14 -> 51;
            default -> 56;
        };
    }
}
