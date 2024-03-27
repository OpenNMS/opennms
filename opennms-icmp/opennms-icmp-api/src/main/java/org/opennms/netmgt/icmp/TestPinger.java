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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Pinger} that always responds successfully.
 *
 * Should be used exclusively for testing.
 */
public class TestPinger implements Pinger {

    @Override
    public void ping(InetAddress host, long timeout, int retries, int packetsize, int sequenceId, PingResponseCallback cb ) throws Exception {
        cb.handleResponse( host, new EchoPacket() {

            @Override
            public boolean isEchoReply() {
                return true;
            }

            @Override
            public int getIdentifier() {
                return 0;
            }

            @Override
            public int getSequenceNumber() {
                return 0;
            }

            @Override
            public long getThreadId() {
                return 0;
            }

            @Override
            public long getReceivedTimeNanos() {
                return 0;
            }

            @Override
            public long getSentTimeNanos() {
                return 0;
            }

            @Override
            public double elapsedTime(TimeUnit timeUnit) {
                return 0;
            }
        });
    }

    @Override
    public void ping(InetAddress host, long timeout, int retries, int sequenceId, PingResponseCallback cb) throws Exception {
        ping( host, timeout, retries, 0, sequenceId, cb );
    }

    @Override
    public Number ping(InetAddress host, long timeout, int retries, int packetsize) throws Exception {
        return 1;
    }

    @Override
    public Number ping(InetAddress host, long timeout, int retries) throws Exception {
        return 1;
    }

    @Override
    public Number ping(InetAddress host) throws Exception {
        return 1;
    }

    @Override
    public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval) {
        final List<Number> numbers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            numbers.add(1d);
        }
        return numbers;
    }

    @Override
    public List<Number> parallelPing(InetAddress host, int count, long timeout, long pingInterval, int size) {
        return parallelPing(host, count, timeout, pingInterval);
    }

    @Override
    public void initialize4() {
        // pass
    }

    @Override
    public void initialize6() {
        // pass
    }

    @Override
    public boolean isV4Available() {
        return true;
    }

    @Override
    public boolean isV6Available() {
        return true;
    }

    @Override
    public void setAllowFragmentation(boolean allow) {
        // pass
    }

    @Override
    public void setTrafficClass(int tc) {
        // pass
    }
}
