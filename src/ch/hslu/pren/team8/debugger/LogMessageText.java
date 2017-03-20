package ch.hslu.pren.team8.debugger;

import java.io.Serializable;

/**
 * Created by gebs on 3/17/17.
 */
public class LogMessageText extends LogMessageBase implements Serializable {
    String LogText;

    public String getLogText() {
        return LogText;
    }

    public void setLogText(String logText) {
        LogText = logText;
    }

    @Override
    public String toString() {
        return LogText;
    }
}
