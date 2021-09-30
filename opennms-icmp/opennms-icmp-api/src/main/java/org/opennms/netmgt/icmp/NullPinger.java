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

package org.opennms.netmgt.icmp;

import java.net.InetAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullPinger implements Pinger {
    private static final UnsupportedOperationException UNSUPPORTED = new UnsupportedOperationException("ICMP is not available.");
    private static final Logger LOG = LoggerFactory.getLogger(NullPinger.class);

    @Override
    public void ping(InetAddress host, long timeout, int retries, int packetsize, int sequenceId, PingResponseCallback cb) throws Exception {
        LOG.trace("ping: host={}, timeout={}, retries={}, packetsize={}, sequenceId={}, callback={}", host, timeout, retries, packetsize, sequenceId, cb);
        cb.handleError(host, null, UNSUPPORTED);
    }

    @Override
    public void ping(InetAddress host, long timeout, int retries, int sequenceId, PingResponseCallback cb) throws Exception {
        LOG.trace("ping: host={}, timeout={}, retries={}, sequenceId={}, callback={}", host, timeout, retries, sequenceId, cb);
        cb.handleError(host, null, UNSUPPORTED);
    }

    @Override
    public Number ping(InetAddress host, long timeout, int retries, int packetsize) throws Exception {
        LOG.trace("ping: host={}, timeout={}, retries={}, packetsize={}", host, timeout, retries, packetsize);
        throw UNSUPPORTED;
    }

    @Override
    public Number ping(InetAddress host, long timeout, int retries) throws Exception {
        LOG.trace("ping: host={}, timeout={}, retries={}", host, timeout, retries);
        throw UNSUPPORTED;
    }

    @Override
    public Number ping(final InetAddress host) throws Exception {
        LOG.trace("ping: host={}", host);
        throw UNSUPPORTED;
    }

    @Override
    public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval, int size) throws Exception {
        LOG.trace("parallel-ping: host={}, count={}, timeout={}, interval={}, size={}", host, count, timeout, pingInterval, size);
        throw UNSUPPORTED;
    }

    @Override
    public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval) throws Exception {
        LOG.trace("parallel-ping: host={}, count={}, timeout={}, interval={}", host, count, timeout, pingInterval);
        throw UNSUPPORTED;
    }

    @Override
    public void initialize4() throws Exception {
        LOG.info("initialize4() called.");
    }

    @Override
    public void initialize6() throws Exception {
        LOG.info("initialize6() called.");
    }

    @Override
    public boolean isV4Available() {
        LOG.info("isV4Available() called, lying and saying 'true'");
        return true;
    }

    @Override
    public boolean isV6Available() {
        LOG.info("isV6Available() called, lying and saying 'true'");
        return true;
    }

    @Override
    public void setTrafficClass(final int tc) throws Exception {
        LOG.warn("NullPinger cannot set traffic class.  Ignoring.");
    }

    @Override
    public void setAllowFragmentation(final boolean allow) throws Exception {
        LOG.warn("NullPinger cannot set fragmentation.  Ignoring.");
    }

}
