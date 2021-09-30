/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.jicmp.standalone;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * PingReplyMetric
 *
 * @author brozow
 */
public class PingReplyMetric extends Metric implements PingReplyListener {
    
    CountDownLatch m_latch;
    int m_count;
    long m_interval;
    
    public PingReplyMetric(int count, long interval) {
        m_latch = new CountDownLatch(count);
        m_count = count;
        m_interval = interval;
    }

    @Override
    public void onPingReply(InetAddress address, PingReply reply) {
        try {
            update(reply.getElapsedTimeNanos());
        } finally {
            m_latch.countDown();
        }
    }

    public void await() throws InterruptedException {
        m_latch.await(m_interval*m_count + 1000, TimeUnit.MILLISECONDS);
    }

}
