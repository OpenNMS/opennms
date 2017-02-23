/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.rtc;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.rtc.datablock.RTCCategory;
import org.opennms.netmgt.xml.rtc.Category;
import org.opennms.netmgt.xml.rtc.EuiLevel;
import org.opennms.netmgt.xml.rtc.Node;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-rtc.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=false)
public class AvailabilityServiceIT implements TemporaryDatabaseAware<MockDatabase> {

    @Autowired
    private AvailabilityService m_availabilityService;

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private TransactionTemplate m_transactionTemplate;

    private MockDatabase m_mockDatabase;

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        m_mockDatabase = database;
    }

    @Before
    public void setUp() {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    public void categoryIsFullyAvailableWhenNoServicesArePresent() throws Exception {
        final RTCCategory rtcCat = EasyMock.createNiceMock(RTCCategory.class);
        EasyMock.expect(rtcCat.getLabel()).andReturn("Routers");
        // This nodeid should not exist in the database
        EasyMock.expect(rtcCat.getNodes()).andReturn(Lists.newArrayList(99999));
        EasyMock.replay(rtcCat);

        final EuiLevel euiLevel = m_availabilityService.getEuiLevel(rtcCat);
        assertEquals(1, euiLevel.getCategory().size());

        final Category category = euiLevel.getCategory().get(0);
        assertEquals(100.0, category.getCatvalue(), 0.001);
        assertEquals(1, category.getNode().size());

        final Node node = category.getNode().get(0);
        assertEquals(100.0, node.getNodevalue(), 0.001);
        assertEquals(0, node.getNodesvccount());
        assertEquals(0, node.getNodesvcdowncount());
    }

    @Test
    public void canCalculateAvailability() throws Exception {
        final MockNetwork mockNetwork = new MockNetwork();
        // This test depends on the specifics in the standard network definition
        mockNetwork.createStandardNetwork();
        m_mockDatabase.populate(mockNetwork);

        final RTCCategory rtcCat = EasyMock.createNiceMock(RTCCategory.class);
        EasyMock.expect(rtcCat.getLabel()).andReturn("NOC").anyTimes();
        EasyMock.expect(rtcCat.getNodes()).andReturn(Lists.newArrayList(1, 2)).anyTimes();
        EasyMock.replay(rtcCat);

        // Verify the availability when no outages are present
        EuiLevel euiLevel = m_availabilityService.getEuiLevel(rtcCat);
        assertEquals(1, euiLevel.getCategory().size());

        Category category = euiLevel.getCategory().get(0);
        assertEquals(100.0, category.getCatvalue(), 0.001);
        assertEquals(2, category.getNode().size());

        // Assumes the nodes are sorted
        assertEquals(4, category.getNode().get(0).getNodesvccount());
        assertEquals(2, category.getNode().get(1).getNodesvccount());

        // Create an outage that is both open and closed within the window
        final Date now = new Date();
        final Date oneHourAgo = new Date(now.getTime() - (60 * 60 * 1000));
        final Date thirtyMinutesAgo = new Date(now.getTime() - (30 * 60 * 1000));

        final OnmsMonitoredService icmpService = toMonitoredService(mockNetwork.getService(1, "192.168.1.1", "ICMP"));

        OnmsOutage outage = new OnmsOutage();
        outage.setMonitoredService(icmpService);
        outage.setIfLostService(oneHourAgo);
        outage.setIfRegainedService(thirtyMinutesAgo);
        m_outageDao.save(outage);
        m_outageDao.flush();

        // Verify the availability when outages are present
        euiLevel = m_availabilityService.getEuiLevel(rtcCat);
        assertEquals(1, euiLevel.getCategory().size());

        category = euiLevel.getCategory().get(0);
        // This number should only need to be adjusted if the duration of the outage
        // or the number of services in the category changes
        assertEquals(RTCUtils.getOutagePercentage(1800000, 86400000, 6), category.getCatvalue(), 0.0001);
        assertEquals(2, category.getNode().size());
    }

    private OnmsMonitoredService toMonitoredService(MockService svc) {
        return m_monitoredServiceDao.get(svc.getNodeId(), svc.getAddress(), svc.getSvcName());
    }
}
