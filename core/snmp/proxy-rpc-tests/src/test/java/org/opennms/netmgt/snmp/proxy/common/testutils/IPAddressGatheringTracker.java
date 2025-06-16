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
package org.opennms.netmgt.snmp.proxy.common.testutils;

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.provision.service.IPAddressTableTracker;

import com.google.common.collect.Lists;

public class IPAddressGatheringTracker extends IPAddressTableTracker {
    private final List<IPAddressTableTracker.IPAddressRow> ipAddressRows = Lists.newArrayList();

    public String getDescription() {
        return"IP address tables";
    }

    @Override
    public void processIPAddressRow(final IPAddressTableTracker.IPAddressRow row) {
        ipAddressRows.add(row);
    }

    public List<String> getIpAddresses() {
        return ipAddressRows.stream()
                .map(row -> row.getIpAddress())
                .collect(Collectors.toList());
    }
}
