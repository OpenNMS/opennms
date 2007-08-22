package org.opennms.netmgt.ping;

import org.opennms.protocols.icmp.ICMPEchoPacket;

public interface PingResponseCallback {

	public void handleResponse(ICMPEchoPacket packet);
	public void handleTimeout(ICMPEchoPacket packet);
    public void handleError(PingRequest request, Throwable t);

}
