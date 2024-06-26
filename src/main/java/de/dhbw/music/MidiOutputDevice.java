package de.dhbw.music;

import de.dhbw.communication.EventQueues;
import de.dhbw.communication.MidiBatchMessage;

import javax.sound.midi.*;

public class MidiOutputDevice extends Thread{

    MidiDevice md;
    Receiver recv;
    boolean running = true, suspended = false;


    public void setMidiDevice(String deviceName){
        release();
        deviceName = deviceName.toLowerCase();
        MidiDevice.Info[] mdInfo = MidiSystem.getMidiDeviceInfo();
        try {
            foundDevice:{
                for (MidiDevice.Info info : mdInfo) {
                    if (info.getName().toLowerCase().contains(deviceName) || info.getDescription().toLowerCase().contains(deviceName) || info.getVendor().toLowerCase().contains(deviceName)) {
                        md = MidiSystem.getMidiDevice(info);
                        if (!md.isOpen()) {
                            md.open();
                        }
                        recv = md.getReceiver();
                        suspended = false;
                        break foundDevice;
                    }
                }
                throw new MidiUnavailableException("could not find requested device");
            }
        }catch (MidiUnavailableException e){
            // TODO send message to UI about failure
            throw new RuntimeException(e);
        }
    }

    public void initialize(){
        if(md == null || !md.isOpen() || recv == null){
            // TODO send message to UI that initialization needs to take place first
            return;
        }
        try {
            // TODO adapt in a way that the settings are processed and applied to the midi out
            recv.send(new ShortMessage(0xc9, 0x04, 0x00), -1);
        } catch (InvalidMidiDataException e) {
            // TODO inform ui about failure
            throw new RuntimeException(e);
        }
    }

    public void run(){
        running = true;
        if(md == null || !md.isOpen() || recv == null){
            // TODO send message to UI that initialization needs to take place first
            return;
        }
        MidiBatchMessage ms;
        ShortMessage smg = new ShortMessage();
        try {
            while (running){
                if(suspended){
                    Thread.sleep(50);
                    continue;
                }
                if(!EventQueues.toMidi.isEmpty()){
                    ms = EventQueues.toMidi.take();
                    int[] message;
                    for(int messageNo = 0; messageNo < ms.getSize(); messageNo++){
                        message = ms.getMidiMessage(messageNo);
                        smg.setMessage(ShortMessage.NOTE_ON, message[3], message[0], message[1]);
                        recv.send(smg, message[2]);
                        // TODO check whether 500000 sounds good on piano that actually sustains
                        smg.setMessage(ShortMessage.NOTE_OFF, message[3], message[0], message[1]);
                        recv.send(smg, md.getMicrosecondPosition() + 500000);
                    }
                }
            }
        } catch (InterruptedException | InvalidMidiDataException e) {
            if(running){
                // TODO maybe(?) inform UI about failure
                throw new RuntimeException(e);
            }
            // if running is false, assume that failure happened because of sync issues (device was closed between condition check and recv.send(...))
        }
    }

    public void stopDevice(){
        running = false;
        release();
    }

    public void release(){
        suspended = true;
        if(recv != null) {
            recv.close();
        }
        if(md != null && md.isOpen()) {
            md.close();
        }
    }
}
