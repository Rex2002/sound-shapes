package de.dhbw.communication;

public class Message {
    public final MsgType type;
    public Object data;

    public Message(MsgType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public Message(MsgType type) {
        this.type = type;
    }
}
