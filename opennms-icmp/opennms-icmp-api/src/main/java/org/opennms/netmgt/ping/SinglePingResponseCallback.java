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
 * Created: August 22, 2007
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
package org.opennms.netmgt.ping;

import java.net.InetAddress;

import org.apache.log4j.Category;
import org.opennms.core.concurrent.BarrierSignaler;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.icmp.ICMPEchoPacket;

/**
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class SinglePingResponseCallback implements PingResponseCallback {
    private BarrierSignaler bs = new BarrierSignaler(1);
    @SuppressWarnings("unused")
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

    private ThreadCategory log() {
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
        info("finished waiting for ping to "+m_host+" to finish");
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
