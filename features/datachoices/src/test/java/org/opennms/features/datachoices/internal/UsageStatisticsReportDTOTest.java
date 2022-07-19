/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.datachoices.internal;

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

        String actualJson = usageStatisticsReport.toJson();
        System.err.println(actualJson);
        String expectedJson = "{\"alarms\":0,\"availableProcessors\":null,\"businessEdgeCount\":0,\"destinationPathCount\":-1,\"events\":0,\"freePhysicalMemorySize\":null,\"installedFeatures\":null,\"installedOIAPlugins\":null,\"ipInterfaces\":0,\"minions\":0,\"monitoredServices\":0,\"monitoringLocations\":0,\"nodes\":0,\"nodesBySysOid\":{\".1.2.3.4\":2,\".1.2.3.5\":6},\"notificationEnablementStatus\":null,\"onCallRoleCount\":1,\"osArch\":null,\"osName\":null,\"osVersion\":null,\"packageName\":\"opennms\",\"provisiondImportThreadPoolSize\":0,\"provisiondRequisitionSchemeCount\":{\"file\":4,\"vmware\":3},\"provisiondRescanThreadPoolSize\":0,\"provisiondScanThreadPoolSize\":0,\"provisiondWriteThreadPoolSize\":0,\"requisitionCount\":0,\"requisitionWithChangedFSCount\":0,\"services\":{\"Provisiond\":true,\"Telemetryd\":false},\"situations\":0,\"snmpInterfaces\":0,\"snmpInterfacesWithFlows\":0,\"systemId\":\"aae3fdeb-3014-47b4-bb13-c8aa503fccb7\",\"totalPhysicalMemorySize\":null,\"version\":\"10.5.7\"}";
        assertEquals(expectedJson, actualJson);
    }
}
