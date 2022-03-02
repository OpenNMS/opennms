/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2021 The OpenNMS Group, Inc.
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
package org.opennms.core.network;

import java.util.Objects;

public class IPPortRange {
    private final int begin;
    private final int end;

    public IPPortRange(int begin, int end) {
        if (begin < 0 || end > 65535 || begin > end) throw new IllegalArgumentException("invalid port range - begin: " + begin + "; end: " + end);
        this.begin = begin;
        this.end = end;
    }

    public IPPortRange(int port) {
        this(port, port);
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public boolean contains(int port) {
        return begin <= port && end >= port;
    }

    @Override
    public String toString() {
        return "IPPortRange{" +
               "begin=" + begin +
               ", end=" + end +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IPPortRange that = (IPPortRange) o;
        return begin == that.begin && end == that.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(begin, end);
    }
}
