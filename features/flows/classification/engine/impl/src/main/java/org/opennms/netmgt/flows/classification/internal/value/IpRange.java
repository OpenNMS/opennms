/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
