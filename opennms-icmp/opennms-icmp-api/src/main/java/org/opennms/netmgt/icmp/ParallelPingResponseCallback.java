/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
    @Override
    public void handleError(InetAddress address, EchoPacket request, Throwable t) {
        try {
            if (request != null) {
                m_responseTimes[request.getSequenceNumber()] = null;
            }
            m_error = t;
        } finally {
            m_latch.countDown();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void handleResponse(InetAddress address, EchoPacket response) {
        try {
            if (response != null) {
                m_responseTimes[response.getSequenceNumber()] = response.elapsedTime(TimeUnit.MICROSECONDS);
            }
        } finally {
            m_latch.countDown();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void handleTimeout(InetAddress address, EchoPacket request) {
        try {
            if (request != null) {
                m_responseTimes[request.getSequenceNumber()] = null;
            }
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
