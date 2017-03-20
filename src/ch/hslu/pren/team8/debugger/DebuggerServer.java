package ch.hslu.pren.team8.debugger;

/**
 * Created by gebs on 3/20/17.
 */
public class DebuggerServer {
    private String ipAddress;
    private  String serverName;


    public DebuggerServer(String ipAddress, String serverName) {
        this.ipAddress = ipAddress;
        this.serverName = serverName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getServerName() {
        return serverName;
    }
}
