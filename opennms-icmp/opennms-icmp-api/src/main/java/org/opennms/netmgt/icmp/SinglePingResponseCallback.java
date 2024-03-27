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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>SinglePingResponseCallback class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class SinglePingResponseCallback implements PingResponseCallback {
	
	
	private static final Logger LOG = LoggerFactory
			.getLogger(SinglePingResponseCallback.class);

    
    /**
     * Value of round-trip-time for the ping in microseconds.
     */
    private Long m_responseTime = null;

    private InetAddress m_host;
    
    private Throwable m_error = null;

    private CountDownLatch m_latch = new CountDownLatch(1);

    /**
     * <p>Constructor for SinglePingResponseCallback.</p>
     *
     * @param host a {@link java.net.InetAddress} object.
     */
    public SinglePingResponseCallback(InetAddress host) {
        m_host = host;
    }

    /** {@inheritDoc} */
    @Override
    public void handleResponse(InetAddress address, EchoPacket response) {
        try {
            info("got response for address " + address + ", thread " + response.getIdentifier() + ", seq " + response.getSequenceNumber() + " with a responseTime "+response.elapsedTime(TimeUnit.MILLISECONDS)+"ms");
            m_responseTime = (long)Math.round(response.elapsedTime(TimeUnit.MICROSECONDS));
        } finally {
            m_latch.countDown();
        }
    }


    /** {@inheritDoc} */
    @Override
    public void handleTimeout(InetAddress address, EchoPacket request) {
        try {
            assert(request != null);
            info("timed out pinging address " + address + ", thread " + request.getIdentifier() + ", seq " + request.getSequenceNumber());
        } finally {
            m_latch.countDown();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void handleError(InetAddress address, EchoPacket request, Throwable t) {
        try {
            m_error = t;
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

    public void rethrowError() throws Exception {
        if (m_error instanceof Error) {
            throw (Error)m_error;
        } else if (m_error instanceof Exception) {
            throw (Exception)m_error;
        }
    }

    /**
     * <p>Getter for the field <code>responseTime</code>.</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getResponseTime() {
        return m_responseTime;
    }
    
    public Throwable getError() {
        return m_error;
    }

    /**
     * <p>info</p>
     *
     * @param msg a {@link java.lang.String} object.
     */
    public void info(String msg) {
        LOG.info(msg);
    }
    /**
     * <p>info</p>
     *
     * @param msg a {@link java.lang.String} object.
     * @param t a {@link java.lang.Throwable} object.
     */
    public void info(String msg, Throwable t) {
        LOG.info(msg, t);
    }


}
