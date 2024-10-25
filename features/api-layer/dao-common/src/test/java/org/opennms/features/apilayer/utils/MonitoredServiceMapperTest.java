/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.utils;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import org.opennms.features.apilayer.model.mappers.MonitoredServiceMapper;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;

public class MonitoredServiceMapperTest {

    @Test
    public void testMappingOfStatus() {
       var mapper =  Mappers.getMapper(MonitoredServiceMapper.class);
        // No outages set, so status should be up.
        OnmsMonitoredService onmsMonitoredService = new OnmsMonitoredService();
        onmsMonitoredService.setServiceType(new OnmsServiceType("SNMP"));
        onmsMonitoredService.setStatus("A");
        var monitoredService = mapper.map(onmsMonitoredService);
        Assert.assertTrue(monitoredService.getStatus());
        // Add outage,  status should be down
        OnmsOutage onmsOutage = new OnmsOutage(Date.from(Instant.now()), onmsMonitoredService);
        var outages = Sets.newHashSet(onmsOutage);
        onmsMonitoredService.setCurrentOutages(outages);
        monitoredService = mapper.map(onmsMonitoredService);
        Assert.assertFalse(monitoredService.getStatus());
        // Set status to be unmanaged, status should be down
        onmsMonitoredService.setStatus("U");
        onmsMonitoredService.setCurrentOutages(new HashSet<>());
        monitoredService = mapper.map(onmsMonitoredService);
        Assert.assertFalse(monitoredService.getStatus());

    }
}
