/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created August 29, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.discovery;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.ping.PingResponseCallback;
import org.opennms.protocols.icmp.ICMPEchoPacket;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 */

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

    private ThreadCategory log() {
        return ThreadCategory.getInstance(this.getClass());
    }

}
