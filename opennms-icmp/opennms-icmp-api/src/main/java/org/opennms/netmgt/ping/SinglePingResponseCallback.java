package org.opennms.netmgt.ping;

import org.opennms.core.concurrent.BarrierSignaler;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.icmp.ICMPEchoPacket;

public class SinglePingResponseCallback implements PingResponseCallback {
    BarrierSignaler bs = new BarrierSignaler(1);
    Throwable error = null;
    Long responseTime = null;
    
	public void handleResponse(ICMPEchoPacket packet) {
	    responseTime = packet.getPingRTT();
	    bs.signalAll();
	}

	public void handleTimeout(ICMPEchoPacket packet) {
	    bs.signalAll();
	}

    public void handleError(PingRequest pr, Throwable t) {
        ThreadCategory.getInstance(this.getClass()).info("an error occurred pinging " + pr.getAddress(), t);
        error = t;
        bs.signalAll();
    }

    public boolean isSuccessful() {
        return error == null;
    }
    
    public void waitFor(long timeout) throws InterruptedException {
        bs.waitFor(timeout);
    }
    
    public void waitFor() throws InterruptedException {
        bs.waitFor();
    }

    public Long getResponseTime() {
        return responseTime;
    }

}
