package org.opennms.netmgt.ping;

import java.net.InetAddress;

import org.opennms.protocols.icmp.ICMPEchoPacket;

public interface PingResponseCallback {

	public void handleResponse(InetAddress address, ICMPEchoPacket packet);
	public void handleTimeout(InetAddress address, ICMPEchoPacket packet);
    public void handleError(InetAddress address, ICMPEchoPacket packet, Throwable t);

}
