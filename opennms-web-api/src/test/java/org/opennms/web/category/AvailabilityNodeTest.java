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
package org.opennms.web.category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;

public class AvailabilityNodeTest {

    @Test
    public void setThresholdsStoresValues() {
        final AvailabilityNode node = new AvailabilityNode();
        node.setThresholds(99.99, 97);
        assertEquals(Double.valueOf(99.99), node.getNormalThreshold());
        assertEquals(Double.valueOf(97.0), node.getWarningThreshold());
    }

    @Test
    public void negativeThresholdsAreOmitted() {
        final AvailabilityNode node = new AvailabilityNode();
        node.setThresholds(-1, -1);
        assertNull(node.getNormalThreshold());
        assertNull(node.getWarningThreshold());
    }

    @Test
    public void managedServiceCountCountsOnlyStatusA() {
        final OnmsNode node = new OnmsNode();
        node.setId(1);
        final OnmsIpInterface if1 = addInterface(node, "192.168.1.1", "M");
        final OnmsIpInterface if2 = addInterface(node, "192.168.1.2", "M");
        addService(if1, "ICMP", "A");
        addService(if1, "SNMP", "F");
        addService(if2, "ICMP", "A");

        final AvailabilityNode avail = new AvailabilityNode(node, 100d);

        assertEquals(Long.valueOf(2), avail.getManagedServiceCount());
        assertEquals(Long.valueOf(3), avail.getServiceCount());
    }

    /**
     * The availability figures count a service only on a managed interface, so the count must not
     * include the services of an unmanaged one, whatever their own status.
     */
    @Test
    public void managedServiceCountSkipsUnmanagedInterfaces() {
        final OnmsNode node = new OnmsNode();
        node.setId(1);
        final OnmsIpInterface managed = addInterface(node, "192.168.1.1", "M");
        final OnmsIpInterface unmanaged = addInterface(node, "192.168.1.2", "U");
        final OnmsIpInterface unset = addInterface(node, "192.168.1.3", null);
        addService(managed, "ICMP", "A");
        addService(unmanaged, "ICMP", "A");
        addService(unmanaged, "SNMP", "A");
        addService(unset, "ICMP", "A");

        final AvailabilityNode avail = new AvailabilityNode(node, 100d);

        assertEquals(Long.valueOf(1), avail.getManagedServiceCount());
        assertEquals(Long.valueOf(4), avail.getServiceCount());
    }

    private static OnmsIpInterface addInterface(final OnmsNode node, final String address, final String isManaged) {
        final OnmsIpInterface iface = new OnmsIpInterface(address, node);
        iface.setIsManaged(isManaged);
        node.addIpInterface(iface);
        return iface;
    }

    private static void addService(final OnmsIpInterface iface, final String name, final String status) {
        final OnmsMonitoredService svc = new OnmsMonitoredService(iface, new OnmsServiceType(name));
        svc.setStatus(status);
    }
}
