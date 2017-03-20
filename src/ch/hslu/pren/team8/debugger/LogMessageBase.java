package ch.hslu.pren.team8.debugger;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by gebs on 3/17/17.
 */
public class LogMessageBase implements Serializable{
    private MessageType type;
    private Date logDate;
    private LogLevel logLevel;

    public LogMessageBase() {
        this.logDate = new Date();
    }
    public LogMessageBase(LogLevel logLevel, MessageType type){
        this.logLevel = logLevel;
        this.type = type;
        this.logDate = new Date();
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        type = type;
    }
}
