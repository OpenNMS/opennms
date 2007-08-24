package org.opennms.netmgt.ping;

import org.apache.log4j.Category;
import org.opennms.core.concurrent.BarrierSignaler;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.icmp.ICMPEchoPacket;

public class SinglePingResponseCallback implements PingResponseCallback {
    BarrierSignaler bs = new BarrierSignaler(1);
    Throwable error = null;
    Long responseTime = null;
    
	public void handleResponse(ICMPEchoPacket packet) {
	    info("got response for " + packet.getTID() + "/" + packet.getSequenceId() + " with a responseTime "+packet.getPingRTT());
	    responseTime = packet.getPingRTT();
	    bs.signalAll();
	}

    private Category log() {
        return ThreadCategory.getInstance(this.getClass());
    }

	public void handleTimeout(ICMPEchoPacket packet) {
	    info("timed out pinging " + packet.getTID() + "/" + packet.getSequenceId());
	    bs.signalAll();
	}

    public void handleError(PingRequest pr, Throwable t) {
        info("an error occurred pinging " + pr.getAddress(), t);
        error = t;
        bs.signalAll();
    }

    public void waitFor(long timeout) throws InterruptedException {
        bs.waitFor(timeout);
    }
    
    public void waitFor() throws InterruptedException {
        info("waiting to finish");
        bs.waitFor();
    }

    public Long getResponseTime() {
        return responseTime;
    }
    
    public void info(String msg) {
        log().info(msg);
    }
    public void info(String msg, Throwable t) {
        log().info(msg, t);
    }

}
