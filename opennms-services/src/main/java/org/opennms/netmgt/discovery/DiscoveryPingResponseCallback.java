package org.opennms.netmgt.discovery;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.ping.PingResponseCallback;
import org.opennms.netmgt.utils.EventBuilder;
import org.opennms.protocols.icmp.ICMPEchoPacket;

public class DiscoveryPingResponseCallback implements PingResponseCallback {
    final static String EVENT_SOURCE_VALUE = "OpenNMS.Discovery";

    public void handleResponse(InetAddress address, ICMPEchoPacket packet) {
        EventBuilder eb = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, EVENT_SOURCE_VALUE);
        eb.setInterface(address.getHostAddress());

        try {
            eb.setHost(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException uhE) {
            eb.setHost("unresolved.host");
            log().warn("Failed to resolve local hostname", uhE);
        }

        eb.addParam("RTT", packet.getReceivedTime() - packet.getSentTime());

        try {
            EventIpcManagerFactory.getIpcManager().sendNow(eb.getEvent());

            if (log().isDebugEnabled()) {
                log().debug("Sent event: " + EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
            }
        } catch (Throwable t) {
            log().warn("run: unexpected throwable exception caught during send to middleware", t);
        }

    }

    public void handleTimeout(InetAddress address, ICMPEchoPacket packet) {
        log().debug("request timed out: " + address);
    }

    public void handleError(InetAddress address, ICMPEchoPacket packet, Throwable t) {
        log().debug("an error occurred pinging " + address, t);
    }

    private Category log() {
        return ThreadCategory.getInstance(this.getClass());
    }

}
