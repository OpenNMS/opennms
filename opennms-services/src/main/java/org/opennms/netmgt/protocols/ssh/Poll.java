package org.opennms.netmgt.protocols.ssh;

import java.net.InetAddress;
import java.util.Collections;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.monitors.TimeoutTracker;
import org.opennms.netmgt.protocols.InsufficientParametersException;

public class Poll extends Ssh implements org.opennms.netmgt.protocols.Poll {

    public Poll() { }
    
    public Poll(InetAddress address) {
        setAddress(address);
    }
    
    public Poll(InetAddress address, int port) {
        setAddress(address);
        setPort(port);
    }
    
    public Poll(InetAddress address, int port, int timeout) {
        setAddress(address);
        setPort(port);
        setTimeout(timeout);
    }

    public PollStatus poll(TimeoutTracker tracker) throws InsufficientParametersException {
        tracker.startAttempt();
        boolean isAvailable = connect();
        double responseTime = tracker.elapsedTimeInMillis();

        PollStatus ps = PollStatus.unavailable();
        
        String errorMessage = "";
        if (getError() != null) {
            errorMessage = getError().getMessage();
        }
        
        if (isAvailable) {
            ps = PollStatus.available(responseTime);
        } else if (errorMessage.matches("^.*java.net.NoRouteToHostException.*$")) {
            ps = PollStatus.unavailable("no route to host");
        } else if (errorMessage.matches("^.*(timeout: socket is not established|java.io.InterruptedIOException).*$")) {
            ps = PollStatus.unavailable("connection timed out");
        } else if (errorMessage.matches("^.*(connection is closed by foreign host|java.net.ConnectException).*$")) {
            ps = PollStatus.unavailable("connection exception");
        } else if (errorMessage.matches("^.*java.io.IOException.*$")) {
            ps = PollStatus.unavailable("I/O exception");
        }
        
        return ps;
    }
    
    public PollStatus poll() throws InsufficientParametersException {
        TimeoutTracker tracker = new TimeoutTracker(Collections.emptyMap(), 1, getTimeout());
        return poll(tracker);
        
    }

}
