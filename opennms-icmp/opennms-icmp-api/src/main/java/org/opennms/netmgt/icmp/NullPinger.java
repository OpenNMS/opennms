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
