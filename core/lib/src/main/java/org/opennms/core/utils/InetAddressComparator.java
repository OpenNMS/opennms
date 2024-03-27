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
package org.opennms.core.utils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Comparator;

/**
 * <p>This comparator will sort {@link InetAddress} instances in the following order:</p>
 * 
 * <ul>
 * <li><code>Inet4Address</code> instances</li>
 * <li><code>Inet6Address</code> instances that are routable with scopeId == 0</li>
 * <li><code>Inet6Address</code> instances that are link-local ordered by scopeId</li>
 * </ul>
 */
public class InetAddressComparator implements Comparator<InetAddress> {

    @Override
    public int compare(InetAddress addr1, InetAddress addr2) {
        if (addr1 == null) {
            if (addr2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (addr2 == null) {
                return 1;
            } else {
                if (addr1 instanceof Inet4Address) {
                    if (addr2 instanceof Inet4Address) {
                        // Two Inet4Address instances
                        return new ByteArrayComparator().compare(addr1.getAddress(), addr2.getAddress());
                    } else {
                        return -1;
                    }
                } else {
                    if (addr2 instanceof Inet4Address) {
                        return 1;
                    } else {
                        // Two Inet6Address instances
                        int scopeComparison = Integer.valueOf(((Inet6Address)addr1).getScopeId()).compareTo(((Inet6Address)addr2).getScopeId());
                        if (scopeComparison == 0) {
                            // If the scope IDs are identical, then compare the addresses
                            return new ByteArrayComparator().compare(addr1.getAddress(), addr2.getAddress());
                        } else {
                           return scopeComparison;
                        }
                    }
                }
            }
        }
    }
}
