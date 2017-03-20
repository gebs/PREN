package ch.hslu.pren.team8.debugger;

import java.io.Serializable;

/**
 * Created by gebs on 3/17/17.
 */
public class LogMessageText extends LogMessageBase implements Serializable {
    String logText;

    public LogMessageText(LogLevel logLevel,MessageType messageType,String logText){
        super(logLevel,messageType);
        this.logText = logText;
    }

    public String getLogText() {
        return logText;
    }

    public void setLogText(String logText) {
        this.logText = logText;
    }

    @Override
    public String toString() {
        return logText;
    }
}
