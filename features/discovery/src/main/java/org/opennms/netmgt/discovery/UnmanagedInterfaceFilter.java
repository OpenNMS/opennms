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
package org.opennms.netmgt.discovery;

import java.net.InetAddress;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;

/**
 * Given a list of managed IP addresses, this filter will match IP addresses not in that
 * list, hence it is an unmanaged IP address filter.
 */
public class UnmanagedInterfaceFilter implements IpAddressFilter {

    private final InterfaceToNodeCache interfaceToNodeCache;

    public UnmanagedInterfaceFilter(InterfaceToNodeCache interfaceToNodeCache) {
        this.interfaceToNodeCache = Objects.requireNonNull(interfaceToNodeCache);
    }

    @Override
    public boolean matches(String location, InetAddress address) {
        return interfaceToNodeCache.getFirstNodeId(location, address).isEmpty();
    }

    @Override
    public boolean matches(String location, String address) {
        return matches(location, InetAddressUtils.addr(address));
    }
}
