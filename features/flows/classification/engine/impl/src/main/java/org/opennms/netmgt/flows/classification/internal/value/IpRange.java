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
package org.opennms.netmgt.flows.classification.internal.value;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.opennms.netmgt.flows.classification.IpAddr;

public class IpRange implements Iterable<IpAddr> {

    public static IpRange of(String addr) {
        final var a = IpAddr.of(addr);
        return new IpRange(a, a);
    }

    public static IpRange of(String begin, String end) {
        return new IpRange(IpAddr.of(begin), IpAddr.of(end));
    }

    public final IpAddr begin, end;

    public IpRange(IpAddr begin, IpAddr end) {
        if (begin.getClass() != end.getClass()) {
            throw new IllegalArgumentException("IpRange can not mix IPv4 and IPv6 addresses - begin: " + begin.getClass().getSimpleName() + "; end: " + end.getClass().getSimpleName());
        }
        if (begin.compareTo(end) > 0) {
            throw new IllegalArgumentException(String.format("beginning of range (%s) must come before end of range (%s)", begin, end));
        }
        this.begin = begin;
        this.end = end;
    }

    public boolean contains(IpAddr addr) {
        return begin.compareTo(addr) <= 0 && addr.compareTo(end) <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IpRange ipAddrs = (IpRange) o;
        return begin.equals(ipAddrs.begin) && end.equals(ipAddrs.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(begin, end);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[').append(begin).append(',').append(end).append(']');
        return buf.toString();
    }

    @Override
    public Iterator<IpAddr> iterator() {
        return new Iterator<IpAddr>() {
            private IpAddr next = begin;

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public IpAddr next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                var n = next;
                if (next.equals(end)) {
                    next = null;
                } else {
                    next = next.inc();
                }
                return n;
            }
        };
    }
}
