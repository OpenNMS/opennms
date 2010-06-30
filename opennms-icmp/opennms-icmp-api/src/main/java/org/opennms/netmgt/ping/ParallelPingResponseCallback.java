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
import java.util.Arrays;
import java.util.List;

import org.opennms.core.concurrent.BarrierSignaler;
import org.opennms.protocols.icmp.ICMPEchoPacket;

/**
 * <p>ParallelPingResponseCallback class.</p>
 *
 * @author <a href="ranger@opennms.org">Ben Reed</a>
 * @version $Id: $
 */
public class ParallelPingResponseCallback implements PingResponseCallback {
    BarrierSignaler bs;
    Number[] m_responseTimes;

    /**
     * <p>Constructor for ParallelPingResponseCallback.</p>
     *
     * @param count a int.
     */
    public ParallelPingResponseCallback(int count) {
        bs = new BarrierSignaler(count);
        m_responseTimes = new Number[count];
    }

    /** {@inheritDoc} */
    public void handleError(InetAddress address, ICMPEchoPacket packet, Throwable t) {
        m_responseTimes[packet.getSequenceId()] = null;
        bs.signalAll();
    }

    /** {@inheritDoc} */
    public void handleResponse(InetAddress address, ICMPEchoPacket packet) {
        m_responseTimes[packet.getSequenceId()] = packet.getPingRTT();
        bs.signalAll();
    }

    /** {@inheritDoc} */
    public void handleTimeout(InetAddress address, ICMPEchoPacket packet) {
        m_responseTimes[packet.getSequenceId()] = null;
        bs.signalAll();
    }

    /**
     * <p>waitFor</p>
     *
     * @throws java.lang.InterruptedException if any.
     */
    public void waitFor() throws InterruptedException {
        bs.waitFor();
    }
    
    /**
     * <p>getResponseTimes</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Number> getResponseTimes() {
        return Arrays.asList(m_responseTimes);
    }
}
