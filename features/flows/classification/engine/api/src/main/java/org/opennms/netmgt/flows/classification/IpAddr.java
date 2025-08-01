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
package org.opennms.netmgt.flows.classification;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.opennms.core.network.IPAddress;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public abstract class IpAddr implements Comparable<IpAddr> {

    public static IpAddr of(String dottedNotation) {
        if (dottedNotation == null) {
            return null;
        }
        try {
            return of(InetAddress.getByName(dottedNotation));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPAddress " + dottedNotation, e);
        }
    }

    public static IpAddr of(InetAddress addr) {
        var bytes = addr.getAddress();
        if (bytes.length == 4) {
            return new Ip4Addr(
                    Ints.fromBytes(bytes[0], bytes[1], bytes[2], bytes[3])
            );
        } else if (bytes.length == 16) {
            return new Ip6Addr(
                Longs.fromBytes(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]),
                Longs.fromBytes(bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15])
            );
        } else {
            throw new RuntimeException("unexpected number of bytes of ip address - addr: " + addr);
        }
    }

    public abstract IpAddr inc();

    public static final class Ip4Addr extends IpAddr {
        private final int value;

        public Ip4Addr(int value) {
            this.value = value;
        }

        @Override
        public int compareTo(IpAddr other) {
            if (other instanceof Ip6Addr) {
                return -1;
            }
            var o = (Ip4Addr) other;
            return Integer.compareUnsigned(value, o.value);
        }

        @Override
        public IpAddr inc() {
            if (value + 1 == 0) {
                throw new RuntimeException("IP address can no more; limit reached");
            }
            return new Ip4Addr(value + 1);
        }

        @Override
        public String toString() {
            return new IPAddress(Ints.toByteArray(value)).toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Ip4Addr ip4Addr = (Ip4Addr) o;
            return value == ip4Addr.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    public static final class Ip6Addr extends IpAddr {
        private final long high, low;

        public Ip6Addr(long high, long low) {
            this.high = high;
            this.low = low;
        }

        @Override
        public int compareTo(IpAddr other) {
            if (other instanceof Ip4Addr) {
                return 1;
            }
            var o = (Ip6Addr) other;
            return high != o.high ?
                   Long.compareUnsigned(high, o.high) :
                   Long.compareUnsigned(low, o.low);

        }

        @Override
        public IpAddr inc() {
            if (low + 1 == 0) {
                if (high + 1 == 0) {
                    throw new RuntimeException("IP address can no more; limit reached");
                }
                return new Ip6Addr(high + 1, 0);
            } else {
                return new Ip6Addr(high, low + 1);
            }
        }

        @Override
        public String toString() {
            return new IPAddress(ArrayUtils.addAll(Longs.toByteArray(high), Longs.toByteArray(low))).toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Ip6Addr ip6Addr = (Ip6Addr) o;
            return high == ip6Addr.high && low == ip6Addr.low;
        }

        @Override
        public int hashCode() {
            return Objects.hash(high, low);
        }
    }
}
