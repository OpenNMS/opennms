/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.RestClient;

public class UsageStatisticsIT {
    @ClassRule
    public static OpenNMSStack stack = OpenNMSStack.MINIMAL;
    private static RestClient restClient;

    @BeforeClass
    public static void beforeClass() {
        restClient = stack.opennms().getRestClient();
        createNode("Cisco #1", "test-fs", "cisco1", ".1.3.6.1.4.1.9.1.799");
        createNode("Cisco #2", "test-fs", "cisco2", ".1.3.6.1.4.1.9.1.799");
        createNode("Cisco #3", "test-fs", "cisco3", ".1.3.6.1.4.1.9.1.799");
        createNode("Juniper #1", "test-fs", "juniper1", ".1.3.6.1.4.1.2636.1.1.1.2.137");
        createNode("Virtual Appliance #1", "test-fs", "appliance1", ".1.3.6.1.4.1.5813.42.5.1");
        createNode("Virtual Appliance #2", "test-fs", "appliance2", ".1.3.6.1.4.1.5813.42.5.1");
        createNode("Mini Appliance #1", "test-fs", "appliance3", ".1.3.6.1.4.1.5813.42.5.2");
    }

    private static void createNode(final String label, final String foreignSource, final String foreignId, final String sysObjectId) {
        final OnmsNode node = new OnmsNode();
        node.setLabel(label);
        node.setType(OnmsNode.NodeType.ACTIVE);
        node.setForeignSource(foreignSource);
        node.setForeignId(foreignId);
        node.setSysObjectId(sysObjectId);
        final Response response = restClient.addNode(node);
        assertEquals(201, response.getStatus());
    }

    @Test
    public void testUsageStatistics() throws Exception {
        final Map<String, Object> usageReport = restClient.getUsageStatistics();

        assertThat((long)usageReport.get("freePhysicalMemorySize"), greaterThan(1L));
        assertThat((long)usageReport.get("totalPhysicalMemorySize"), greaterThan(1L));
        assertThat((String) usageReport.get("version"), matchesPattern("^\\d+\\.\\d+\\.\\d+$"));
        assertThat((long)usageReport.get("availableProcessors"), greaterThan(1L));

        assertThat((String) usageReport.get("osName"), not(emptyString()));
        assertThat((String) usageReport.get("osArch"), not(emptyString()));
        assertThat((String) usageReport.get("osVersion"), not(emptyString()));

        final Map<String, Boolean> services = (Map<String, Boolean>) usageReport.get("services");

        assertEquals(32, services.size());
        assertEquals(28, services.entrySet().stream().filter(e -> e.getValue()).count());
        assertEquals(4, services.entrySet().stream().filter(e -> !e.getValue()).count());

        assertThat((String) usageReport.get("systemId"), matchesPattern("^\\S+-\\S+-\\S+-\\S+-\\S+$"));

        assertThat((String) usageReport.get("packageName"), is("opennms"));

        assertThat((boolean) usageReport.get("notificationEnablementStatus"), is(true));
        assertThat((long) usageReport.get("requisitionWithChangedFSCount"), is(0L));
        assertThat((long) usageReport.get("coreNewtsSamplesInserted"), is(0L));
        assertThat((long) usageReport.get("coreQueuedUpdatesCompleted"), is(0L));

        assertThat((long) usageReport.get("provisiondImportThreadPoolSize"), is(8L));
        assertThat((long) usageReport.get("provisiondRescanThreadPoolSize"), is(10L));
        assertThat((long) usageReport.get("provisiondScanThreadPoolSize"), is(10L));
        assertThat((long) usageReport.get("provisiondWriteThreadPoolSize"), is(8L));

        final Map<String, Long> provisiondRequisitionSchemeCount = (Map<String, Long>) usageReport.get("provisiondRequisitionSchemeCount");

        assertThat(provisiondRequisitionSchemeCount, is(anEmptyMap()));

        assertThat((String) usageReport.get("installedFeatures"), not(emptyString()));

        assertThat((long) usageReport.get("destinationPathCount"), is(1L));

        assertThat((long) usageReport.get("snmpInterfacesWithFlows"), is(0L));
        assertThat((long) usageReport.get("situations"), is(0L));
        assertThat((long) usageReport.get("monitoringLocations"), is(1L));

        final Map<String, Integer> nodesBySysOid = (Map<String, Integer>) usageReport.get("nodesBySysOid");

        assertThat(nodesBySysOid.get(".1.3.6.1.4.1.9.1.799"), is(3));
        assertThat(nodesBySysOid.get(".1.3.6.1.4.1.2636.1.1.1.2.137"), is(1));

        assertThat((String) usageReport.get("installedOIAPlugins"), not(emptyString()));
        assertThat((long) usageReport.get("onCallRoleCount"), is(0L));
        assertThat((long) usageReport.get("requisitionCount"), is(0L));
        assertThat((long) usageReport.get("businessEdgeCount"), is(0L));
        assertThat((String) usageReport.get("sinkStrategy"), is("camel"));
        assertThat((String) usageReport.get("rpcStrategy"), is("jms"));
        assertThat((String) usageReport.get("tssStrategies"), is("rrd"));
        assertThat((long) usageReport.get("pollsCompleted"), is(0L));
        assertThat((long) usageReport.get("eventLogsProcessed"), greaterThan(1L));
        assertThat((long) usageReport.get("coreFlowsPersisted"), is(0L));
        assertThat((String) usageReport.get("databaseProductVersion"), matchesPattern("^\\d+\\.\\d+$"));
        assertThat((String) usageReport.get("databaseProductName"), is("PostgreSQL"));
        assertThat((long) usageReport.get("monitoredServices"), is(0L));
        assertThat((long) usageReport.get("ipInterfaces"), is(0L));
        assertThat((long) usageReport.get("snmpInterfaces"), is(0L));
        assertThat((long) usageReport.get("nodes"), is(7L));
        assertThat((long) usageReport.get("events"), greaterThan(1L));
        assertThat((long) usageReport.get("alarms"), is(0L));
        assertThat((long) usageReport.get("minions"), is(0L));

        final Map<String, Integer> appliances = (Map<String, Integer>)usageReport.get("applianceCounts");
        assertThat((long) appliances.get("virtualAppliance"), is(2L));
        assertThat((long) appliances.get("applianceMini"), is(1L));
        assertThat((long) appliances.get("appliance1U"), is(0L));

        assertThat((boolean) usageReport.get("inContainer"), is(true));
    }
}
