package de.dhbw.music;

import de.dhbw.Settings;
import de.dhbw.communication.EventQueues;
import de.dhbw.communication.MidiMessage;

import javax.sound.midi.*;

public class MidiOutputDevice extends Thread{

    MidiDevice md;
    Receiver recv;
    boolean running = true;

    public void setMidiDevice(int deviceNo){
        release();
        MidiDevice.Info[] mdInfo = MidiSystem.getMidiDeviceInfo();
        try {
            md = MidiSystem.getMidiDevice(mdInfo[deviceNo]);
            if(!md.isOpen()){
                md.open();
            }
            recv = md.getReceiver();
        } catch (MidiUnavailableException e) {
            // TODO send message to UI about failure
            throw new RuntimeException(e);
        }
    }

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

    public static MidiDevice.Info[] getMidiDeviceInfo(){
        return MidiSystem.getMidiDeviceInfo();
    }

    public void updateSettings(Settings settings){
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
        MidiMessage ms;
        ShortMessage smg = new ShortMessage();
        try {
            while (running){
                if(!EventQueues.toMidi.isEmpty()){
                    ms = EventQueues.toMidi.take();
                    smg.setMessage(ShortMessage.NOTE_ON, 9, ms.getNote(), ms.getVelocity());
                    recv.send(smg, ms.getOffset());
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

    public void release(){
        running = false;
        if(recv != null) {
            recv.close();
        }
        if(md != null && md.isOpen()) {
            md.close();
        }
    }
}
