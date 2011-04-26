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
package org.opennms.netmgt.icmp;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.opennms.core.utils.ThreadCategory;

/**
 * <p>SinglePingResponseCallback class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class SinglePingResponseCallback implements PingResponseCallback {
    private CountDownLatch m_latch = new CountDownLatch(1);

    /**
     * Value of round-trip-time for the ping in microseconds.
     */
    private Long m_responseTime = null;

    private InetAddress m_host;

    /**
     * <p>Constructor for SinglePingResponseCallback.</p>
     *
     * @param host a {@link java.net.InetAddress} object.
     */
    public SinglePingResponseCallback(InetAddress host) {
        m_host = host;
    }

    /** {@inheritDoc} */
    public void handleResponse(InetAddress address, EchoPacket response) {
        try {
            info("got response for address " + address + ", thread " + response.getIdentifier() + ", seq " + response.getSequenceNumber() + " with a responseTime "+response.elapsedTime(TimeUnit.MILLISECONDS)+"ms");
            m_responseTime = (long)Math.round(response.elapsedTime(TimeUnit.MICROSECONDS));
        } finally {
            m_latch.countDown();
        }
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(this.getClass());
    }

    /** {@inheritDoc} */
    public void handleTimeout(InetAddress address, EchoPacket request) {
        try {
            assert(request != null);
            info("timed out pinging address " + address + ", thread " + request.getIdentifier() + ", seq " + request.getSequenceNumber());
        } finally {
            m_latch.countDown();
        }
    }

    /** {@inheritDoc} */
    public void handleError(InetAddress address, EchoPacket request, Throwable t) {
        try {
            info("an error occurred pinging " + address, t);
        } finally {
            m_latch.countDown();
        }
    }

    /**
     * <p>waitFor</p>
     *
     * @param timeout a long.
     * @throws java.lang.InterruptedException if any.
     */
    public void waitFor(long timeout) throws InterruptedException {
        m_latch.await(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * <p>waitFor</p>
     *
     * @throws java.lang.InterruptedException if any.
     */
    public void waitFor() throws InterruptedException {
        info("waiting for ping to "+m_host+" to finish");
        m_latch.await();
        info("finished waiting for ping to "+m_host+" to finish");
    }

    /**
     * <p>Getter for the field <code>responseTime</code>.</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getResponseTime() {
        return m_responseTime;
    }

    /**
     * <p>info</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    public void info(String msg) {
        log().info(msg);
    }
    /**
     * <p>info</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param t a {@link java.lang.Throwable} object.
     */
    public void info(String msg, Throwable t) {
        log().info(msg, t);
    }

}
