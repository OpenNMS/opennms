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
package org.opennms.netmgt.icmp.best;

import java.net.InetAddress;
import java.util.List;

import org.opennms.netmgt.icmp.NullPinger;
import org.opennms.netmgt.icmp.PingResponseCallback;
import org.opennms.netmgt.icmp.Pinger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BestMatchPinger implements Pinger {
    private static final Logger LOG = LoggerFactory.getLogger(BestMatchPinger.class);
    private Pinger m_pinger = null;
    private Boolean m_allowFragmentation;
    private Integer m_trafficClass;

    @Override
    public void ping(final InetAddress host, final long timeout, final int retries, final int packetsize, final int sequenceId, final PingResponseCallback cb) throws Exception {
        initialize();
        m_pinger.ping(host, timeout, retries, packetsize, sequenceId, cb);
    }

    @Override
    public void ping(final InetAddress host, final long timeout, final int retries, final int sequenceId, final PingResponseCallback cb) throws Exception {
        initialize();
        m_pinger.ping(host, timeout, retries, sequenceId, cb);
    }

    @Override
    public Number ping(final InetAddress host, final long timeout, final int retries, final int packetsize) throws Exception {
        initialize();
        return m_pinger.ping(host, timeout, retries, packetsize);
    }

    @Override
    public Number ping(final InetAddress host, final long timeout, final int retries) throws Exception {
        initialize();
        return m_pinger.ping(host, timeout, retries);
    }

    @Override
    public Number ping(final InetAddress host) throws Exception {
        initialize();
        return m_pinger.ping(host);
    }

    @Override
    public List<Number> parallelPing(final InetAddress host, final int count, final long timeout, final long pingInterval) throws Exception {
        initialize();
        return m_pinger.parallelPing(host, count, timeout, pingInterval);
    }

    @Override
    public List<Number> parallelPing(final InetAddress host, final int count, final long timeout, final long pingInterval, final int size) throws Exception {
        initialize();
        return m_pinger.parallelPing(host, count, timeout, pingInterval, size);
    }

    @Override
    public void initialize4() throws Exception {
        initialize();

    }

    @Override
    public void initialize6() throws Exception {
        initialize();
    }

    @Override
    public boolean isV4Available() {
        initialize();
        return m_pinger.isV4Available();
    }

    @Override
    public boolean isV6Available() {
        initialize();
        return m_pinger.isV6Available();
    }

    @Override
    public void setAllowFragmentation(final boolean allow) throws Exception {
        if (m_pinger != null) {
            m_pinger.setAllowFragmentation(allow);        
        }
        m_allowFragmentation = allow;
    }

    @Override
    public void setTrafficClass(final int tc) throws Exception {
        if (m_pinger != null) {
            m_pinger.setTrafficClass(tc);
        }
        m_trafficClass = tc;
    }

    private void initialize() {
        if (m_pinger == null) {
            final Class<? extends Pinger> pinger = BestMatchPingerFactory.findPinger();
            try {
                m_pinger = pinger.newInstance();
            } catch (final Throwable t) {
                LOG.error("Failed to initialize best match pinger ({}): {}  Falling back to the null pinger.", pinger, t.getMessage());
                LOG.trace("Failed to initialize best match pinger ({}).  Falling back to the null pinger.", pinger, t);
                m_pinger = new NullPinger();
            }

            try {
                if (m_allowFragmentation != null) {
                    m_pinger.setAllowFragmentation(m_allowFragmentation);
                }
            } catch (final Throwable t) {
                LOG.debug("Failed to set 'allow fragmentation' flag on pinger {}: {}", pinger, t.getMessage());
                LOG.trace("Failed to set 'allow fragmentation' flag on pinger {}.", pinger, t);
            }

            try {
                if (m_trafficClass != null) {
                    m_pinger.setTrafficClass(m_trafficClass);
                }
            } catch (final Throwable t) {
                LOG.debug("Failed to set traffic class on pinger {}", pinger, t);
            }
        }
    }
}
