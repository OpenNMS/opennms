package org.opennms.netmgt.ping;

import java.util.Arrays;
import java.util.List;

import org.opennms.core.concurrent.BarrierSignaler;
import org.opennms.protocols.icmp.ICMPEchoPacket;

public class ParallelPingResponseCallback implements PingResponseCallback {
    BarrierSignaler bs;
    Number[] m_responseTimes;

    public ParallelPingResponseCallback(int count) {
        bs = new BarrierSignaler(count);
        m_responseTimes = new Number[count];
    }

    public void handleError(PingRequest request, Throwable t) {
        m_responseTimes[request.getSequenceId()] = null;
        bs.signalAll();
    }

    public void handleResponse(ICMPEchoPacket packet) {
        m_responseTimes[packet.getSequenceId()] = packet.getPingRTT();
        bs.signalAll();
    }

    public void handleTimeout(ICMPEchoPacket packet) {
        m_responseTimes[packet.getSequenceId()] = null;
        bs.signalAll();
    }

    public void waitFor() throws InterruptedException {
        bs.waitFor();
    }
    
    public List<Number> getResponseTimes() {
        return Arrays.asList(m_responseTimes);
    }
}
