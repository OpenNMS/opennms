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
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.  All rights reserved.
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * <p>ParallelPingResponseCallback class.</p>
 *
 * @author <a href="ranger@opennms.org">Ben Reed</a>
 * @version $Id: $
 */
public class ParallelPingResponseCallback implements PingResponseCallback {
    CountDownLatch m_latch;
    /**
     * Value of round-trip-time for the ping packets in microseconds.
     */
    Number[] m_responseTimes;
    
    Throwable m_error;

    /**
     * <p>Constructor for ParallelPingResponseCallback.</p>
     *
     * @param count a int.
     */
    public ParallelPingResponseCallback(int count) {
        m_latch = new CountDownLatch(count);
        m_responseTimes = new Number[count];
    }

    /** {@inheritDoc} */
    public void handleError(InetAddress address, EchoPacket request, Throwable t) {
        try {
            m_responseTimes[request.getSequenceNumber()] = null;
            m_error = t;
        } finally {
            m_latch.countDown();
        }
    }

    /** {@inheritDoc} */
    public void handleResponse(InetAddress address, EchoPacket response) {
        try {
            m_responseTimes[response.getSequenceNumber()] = response.elapsedTime(TimeUnit.MICROSECONDS);
        } finally {
            m_latch.countDown();
        }
    }

    /** {@inheritDoc} */
    public void handleTimeout(InetAddress address, EchoPacket request) {
        try {
            m_responseTimes[request.getSequenceNumber()] = null;
        } finally {
            m_latch.countDown();
        }
    }

    /**
     * <p>waitFor</p>
     *
     * @throws java.lang.InterruptedException if any.
     */
    public void waitFor() throws InterruptedException {
        m_latch.await();
    }
    
    public void rethrowError() throws Exception {
        if (m_error instanceof Error) {
            throw (Error)m_error;
        } else if (m_error instanceof Exception) {
            throw (Exception)m_error;
        }
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
