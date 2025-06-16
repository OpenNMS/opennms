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
