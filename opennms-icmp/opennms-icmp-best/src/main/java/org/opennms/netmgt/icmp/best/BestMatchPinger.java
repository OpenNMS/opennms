/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
