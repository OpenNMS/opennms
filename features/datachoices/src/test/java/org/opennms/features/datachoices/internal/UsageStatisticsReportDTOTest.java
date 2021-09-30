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

import org.junit.Test;
import org.opennms.features.datachoices.internal.UsageStatisticsReportDTO;

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

        String actualJson = usageStatisticsReport.toJson();
        String expectedJson = "{\"alarms\":0,\"events\":0,\"ipInterfaces\":0,\"monitoredServices\":0,\"nodes\":0,\"nodesBySysOid\":{\".1.2.3.4\":2,\".1.2.3.5\":6},\"osArch\":null,\"osName\":null,\"osVersion\":null,\"packageName\":\"opennms\",\"snmpInterfaces\":0,\"systemId\":\"aae3fdeb-3014-47b4-bb13-c8aa503fccb7\",\"version\":\"10.5.7\"}";
        assertEquals(expectedJson, actualJson);
    }
}
