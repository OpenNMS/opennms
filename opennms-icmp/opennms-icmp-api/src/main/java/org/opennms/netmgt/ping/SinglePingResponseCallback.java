package org.opennms.netmgt.ping;

import org.opennms.core.concurrent.BarrierSignaler;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.icmp.ICMPEchoPacket;

public class SinglePingResponseCallback implements PingResponseCallback {
    BarrierSignaler bs = new BarrierSignaler(1);
    Throwable error = null;
    Long responseTime = null;
    
	public void handleResponse(ICMPEchoPacket packet) {
	    ThreadCategory.getInstance(this.getClass()).info("got response for " + packet.getTID() + "/" + packet.getSequenceId());
	    responseTime = packet.getPingRTT();
	    bs.signalAll();
	}

	public void handleTimeout(ICMPEchoPacket packet) {
	    ThreadCategory.getInstance(this.getClass()).info("timed out pinging " + packet.getTID() + "/" + packet.getSequenceId());
	    bs.signalAll();
	}

    public void handleError(PingRequest pr, Throwable t) {
        ThreadCategory.getInstance(this.getClass()).info("an error occurred pinging " + pr.getAddress(), t);
        error = t;
        bs.signalAll();
    }

    public void waitFor(long timeout) throws InterruptedException {
        bs.waitFor(timeout);
    }
    
    public void waitFor() throws InterruptedException {
        ThreadCategory.getInstance(this.getClass()).info("waiting to finish");
        bs.waitFor();
    }

    public Long getResponseTime() {
        return responseTime;
    }

}
