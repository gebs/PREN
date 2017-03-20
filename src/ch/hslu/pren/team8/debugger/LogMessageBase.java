package ch.hslu.pren.team8.debugger;

import java.io.Serializable;

/**
 * Created by gebs on 3/17/17.
 */
public class LogMessageBase implements Serializable{
    private MessageType Type;

    public MessageType getType() {
        return Type;
    }

    public void setType(MessageType type) {
        Type = type;
    }
}
