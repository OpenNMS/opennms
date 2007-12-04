package org.opennms.netmgt.ping;

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.concurrent.BarrierSignaler;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.icmp.ICMPEchoPacket;

public class SinglePingResponseCallback implements PingResponseCallback {
    private BarrierSignaler bs = new BarrierSignaler(1);
    private Throwable error = null;
    private Long responseTime = null;
    private InetAddress m_host;
    
	public SinglePingResponseCallback(InetAddress host) {
	    m_host = host;
    }

    public void handleResponse(InetAddress address, ICMPEchoPacket packet) {
	    info("got response for address " + address + ", thread " + packet.getTID() + ", seq " + packet.getSequenceId() + " with a responseTime "+packet.getPingRTT());
	    responseTime = packet.getPingRTT();
	    bs.signalAll();
	}

    private Category log() {
        return ThreadCategory.getInstance(this.getClass());
    }

	public void handleTimeout(InetAddress address, ICMPEchoPacket packet) {
	    info("timed out pinging address " + address + ", thread " + packet.getTID() + ", seq " + packet.getSequenceId());
	    bs.signalAll();
	}

    public void handleError(InetAddress address, ICMPEchoPacket pr, Throwable t) {
        info("an error occurred pinging " + address, t);
        error = t;
        bs.signalAll();
    }

    public void waitFor(long timeout) throws InterruptedException {
        bs.waitFor(timeout);
    }
    
    public void waitFor() throws InterruptedException {
        info("waiting for ping to "+m_host+" to finish");
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
