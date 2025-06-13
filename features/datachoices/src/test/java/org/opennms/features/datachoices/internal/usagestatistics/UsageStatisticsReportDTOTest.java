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
package org.opennms.features.datachoices.internal.usagestatistics;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.google.common.collect.Maps;

public class UsageStatisticsReportDTOTest {

    @Test
    public void canMarshallToJson() {
        final UsageStatisticsReportDTO usageStatisticsReport = new UsageStatisticsReportDTO();
        usageStatisticsReport.setSystemId("aae3fdeb-3014-47b4-bb13-c8aa503fccb7");
        usageStatisticsReport.setVersion("10.5.7");
        usageStatisticsReport.setPackageName("opennms");

        Map<String, Long> numberOfNodesBySysOid = Maps.newHashMap();
        numberOfNodesBySysOid.put(".1.2.3.4", 2L);
        numberOfNodesBySysOid.put(".1.2.3.5", 6L);
        usageStatisticsReport.setNodesBySysOid(numberOfNodesBySysOid);

        final Map<String, Long> requisitionSchemeCount = new TreeMap<>();
        requisitionSchemeCount.put("vmware", 3L);
        requisitionSchemeCount.put("file", 4L);
        usageStatisticsReport.setProvisiondRequisitionSchemeCount(requisitionSchemeCount);

        final Map<String, Boolean> services = new TreeMap<>();
        services.put("Provisiond", true);
        services.put("Telemetryd", false);
        usageStatisticsReport.setServices(services);

        usageStatisticsReport.setOnCallRoleCount(1);
        usageStatisticsReport.setNotificationEnablementStatus(null);
        usageStatisticsReport.setDestinationPathCount(-1);
        usageStatisticsReport.setUsers(0);
        usageStatisticsReport.setGroups(0);
        usageStatisticsReport.setOnmsStartupTimeSeconds(1000L);
        usageStatisticsReport.setInContainer(false);
        String actualJson = usageStatisticsReport.toJson();
        System.err.println(actualJson);

        String expectedJson = "{" +
            "\"alarms\":0," +
            "\"applianceCounts\":{}," +
            "\"applications\":0," +
            "\"availableProcessors\":null," +
            "\"businessEdgeCount\":0," +
            "\"coreFlowsPersisted\":0," +
            "\"coreNewtsSamplesInserted\":0," +
            "\"coreQueuedUpdatesCompleted\":0," +
            "\"coreTssWritesCompleted\":0," +
            "\"databaseProductName\":null," +
            "\"databaseProductVersion\":null," +
            "\"dcbFailed\":0," +
            "\"dcbSucceed\":0," +
            "\"dcbWebUiEntries\":0," +
            "\"destinationPathCount\":-1," +
            "\"eventLogsProcessed\":0," +
            "\"events\":0," +
            "\"freePhysicalMemorySize\":null," +
            "\"groups\":0," +
            "\"inContainer\":false," +
            "\"installedFeatures\":null," +
            "\"installedOIAPlugins\":null," +
            "\"ipInterfaces\":0," +
            "\"minions\":0," +
            "\"monitoredServices\":0," +
            "\"monitoringLocations\":0," +
            "\"nodes\":0," +
            "\"nodesBySysOid\":{\".1.2.3.4\":2,\".1.2.3.5\":6}," +
            "\"nodesWithDeviceConfigBySysOid\":{}," +
            "\"notificationEnablementStatus\":null," +
            "\"notifications\":0," +
            "\"onCallRoleCount\":1," +
            "\"onmsStartupTimeSeconds\":1000," +
            "\"osArch\":null," +
            "\"osName\":null," +
            "\"osVersion\":null," +
            "\"outages\":0," +
            "\"packageName\":\"opennms\"," +
            "\"pollsCompleted\":0," +
            "\"provisiondImportThreadPoolSize\":0," +
            "\"provisiondRequisitionSchemeCount\":{\"file\":4,\"vmware\":3}," +
            "\"provisiondRescanThreadPoolSize\":0," +
            "\"provisiondScanThreadPoolSize\":0," +
            "\"provisiondWriteThreadPoolSize\":0," +
            "\"requisitionCount\":0," +
            "\"requisitionWithChangedFSCount\":0," +
            "\"rpcStrategy\":null," +
            "\"services\":{\"Provisiond\":true," +
            "\"Telemetryd\":false}," +
            "\"sinkStrategy\":null," +
            "\"situations\":0," +
            "\"snmpInterfaces\":0," +
            "\"snmpInterfacesWithFlows\":0," +
            "\"systemId\":\"aae3fdeb-3014-47b4-bb13-c8aa503fccb7\"," +
            "\"totalPhysicalMemorySize\":null," +
            "\"tssStrategies\":null," +
            "\"users\":0," +
            "\"version\":\"10.5.7\"" +
        "}";

        assertEquals(expectedJson, actualJson);
    }
}
