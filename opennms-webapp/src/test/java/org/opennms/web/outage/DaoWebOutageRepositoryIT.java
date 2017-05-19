/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.outage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.model.outage.OutageSummary;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.outage.filter.ForeignSourceFilter;
import org.opennms.web.outage.filter.LocationFilter;
import org.opennms.web.outage.filter.NegativeForeignSourceFilter;
import org.opennms.web.outage.filter.NegativeLocationFilter;
import org.opennms.web.outage.filter.NegativeNodeFilter;
import org.opennms.web.outage.filter.NodeFilter;
import org.opennms.web.outage.filter.OutageCriteria;
import org.opennms.web.outage.filter.OutageIdFilter;
import org.opennms.web.outage.filter.RegainedServiceDateBeforeFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class DaoWebOutageRepositoryIT implements InitializingBean {
    
    @Autowired
    DatabasePopulator m_dbPopulator;
    
    @Autowired
    WebOutageRepository m_daoOutageRepo;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @BeforeClass
    public static void setupLogging(){
       
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");

        MockLogAppender.setupLogging(props);
    }

    protected void createNodeEventAndOutage(String location, String label, String ip, String svc) {
        OnmsMonitoringLocation onmsMonitoringLocation = m_dbPopulator.getMonitoringLocationDao().get(location);

        if (onmsMonitoringLocation == null) {
            onmsMonitoringLocation = new OnmsMonitoringLocation();
            onmsMonitoringLocation.setLocationName(location);
            onmsMonitoringLocation.setLatitude(1.0f);
            onmsMonitoringLocation.setLongitude(1.0f);
            onmsMonitoringLocation.setMonitoringArea(location);
            onmsMonitoringLocation.setPriority(1L);
            m_dbPopulator.getMonitoringLocationDao().save(onmsMonitoringLocation);
        }

        List<OnmsNode> nodes = m_dbPopulator.getNodeDao().findByLabel(label);
        OnmsNode node = (nodes.size() == 1 ? nodes.get(0) : null);

        if (node == null) {
            node = new OnmsNode(m_dbPopulator.getMonitoringLocationDao().get(location), label);
            node.setForeignSource(location);
            node.setForeignId(label);
            m_dbPopulator.getNodeDao().save(node);
        }

        int nodeId = m_dbPopulator.getNodeDao().findByForeignId(location, label).getId();

        OnmsIpInterface ipInterface = m_dbPopulator.getIpInterfaceDao().findByNodeIdAndIpAddress(nodeId, ip);

        if (ipInterface == null) {
            ipInterface = new OnmsIpInterface(addr(ip), node);
        }

        OnmsMonitoredService monitoredService = ipInterface.getMonitoredServiceByServiceType(svc);

        if (monitoredService == null) {
            monitoredService = new OnmsMonitoredService(m_dbPopulator.getIpInterfaceDao().findByNodeIdAndIpAddress(nodeId, ip), m_dbPopulator.getServiceTypeDao().findByName(svc));
            m_dbPopulator.getMonitoredServiceDao().save(monitoredService);
        }

        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(m_dbPopulator.getDistPollerDao().whoami());
        event.setEventUei("uei.opennms.org/" + location + "/" + label);
        event.setEventTime(new Date());
        event.setEventSource(location + "/" + label);
        event.setEventCreateTime(new Date());
        event.setEventSeverity(OnmsSeverity.CLEARED.getId());
        event.setEventLog("Y");
        event.setEventDisplay("N");

        m_dbPopulator.getEventDao().save(event);
        m_dbPopulator.getEventDao().flush();

        OnmsOutage outage = new OnmsOutage(new Date(), event, monitoredService);
        outage.setServiceLostEvent(event);

        m_dbPopulator.getOutageDao().save(outage);
    }
    
    @Before
    public void setUp(){
        m_dbPopulator.populateDatabase();
        
        OnmsMonitoredService svc2 = m_dbPopulator.getMonitoredServiceDao().get(2, InetAddressUtils.addr("192.168.2.1"), "ICMP");
        OnmsEvent event = m_dbPopulator.getEventDao().get(1);
        
        OnmsOutage unresolved2 = new OnmsOutage(new Date(), event, svc2);
        m_dbPopulator.getOutageDao().save(unresolved2);
        m_dbPopulator.getOutageDao().flush();
    }
    
    @Test
    @Transactional
    public void testCountMatchingOutages(){
        int count = m_daoOutageRepo.countMatchingOutages(new OutageCriteria());
        assertEquals(3, count);
        
        count = m_daoOutageRepo.countMatchingOutages(new OutageCriteria(new RegainedServiceDateBeforeFilter(new Date())));
        assertEquals(1, count);
    }

    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testGetOutagesByLocation() {
        createNodeEventAndOutage("Pittsboro", "nodeA", "172.16.10.10", "ICMP");
        createNodeEventAndOutage("Pittsboro", "nodeB", "172.16.10.20", "ICMP");
        createNodeEventAndOutage("Minneapolis", "nodeC", "172.16.20.10", "ICMP");
        createNodeEventAndOutage("Minneapolis", "nodeD", "172.16.20.20", "ICMP");
        createNodeEventAndOutage("Minneapolis", "nodeE", "172.16.20.30", "ICMP");

        Outage[] outage1 = m_daoOutageRepo.getMatchingOutages(new OutageCriteria());
        assertEquals(8, outage1.length);

        Outage[] outage2 = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new LocationFilter("Minneapolis")));
        assertEquals(3, outage2.length);
        assertTrue(Arrays.stream(outage2).allMatch(o -> o.getLocation().equals("Minneapolis")));

        Outage[] outage3 = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new NegativeLocationFilter("Minneapolis")));
        assertEquals(5, outage3.length);
        assertTrue(Arrays.stream(outage3).allMatch(o -> !o.getLocation().equals("Minneapolis")));
    }

    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testGetMatchingOutages(){
        Outage[] outage = m_daoOutageRepo.getMatchingOutages(new OutageCriteria());
        assertEquals(3, outage.length);
        
        outage = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new RegainedServiceDateBeforeFilter(new Date())));
        assertEquals(1, outage.length);
        
        outage = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new OutageIdFilter(1)));
        assertEquals(1, outage.length);
        assertEquals(1, outage[0].getId());
        
        outage = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new OutageIdFilter(2)));
        assertEquals(1, outage.length);
        assertEquals(2, outage[0].getId());
    }
    
    /**
     * @see http://issues.opennms.org/browse/NMS-8275
     */
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testGetMatchingOutagesByForeignSource() {
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new ForeignSourceFilter("imported:")));
        assertEquals(3, outages.length);
        outages = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new ForeignSourceFilter("DOESNT_EXIST")));
        assertEquals(0, outages.length);
        outages = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new NegativeForeignSourceFilter("imported:")));
        assertEquals(0, outages.length);
        outages = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new NegativeForeignSourceFilter("DOESNT_EXIST")));
        assertEquals(3, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testGetMatchingOutagesByNodeId(){
        Outage[] outages = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new NodeFilter(m_dbPopulator.getNode2().getId(), null)));
        assertEquals(1, outages.length);
        outages = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new NodeFilter(Integer.MAX_VALUE, null)));
        assertEquals(0, outages.length);
        outages = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new NegativeNodeFilter(m_dbPopulator.getNode2().getId(), null)));
        assertEquals(2, outages.length);
        outages = m_daoOutageRepo.getMatchingOutages(new OutageCriteria(new NegativeNodeFilter(Integer.MAX_VALUE, null)));
        assertEquals(3, outages.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on specific IDs so we need a fresh database
    public void testGetOutage(){
        Outage outage = m_daoOutageRepo.getOutage(1);
        assertNotNull(outage);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testGetOutageSummaries() {
        OutageSummary[] summaries = m_daoOutageRepo.getMatchingOutageSummaries(new OutageCriteria());
        assertEquals("there should be 2 outage summary in the default (current) outage criteria match", 2, summaries.length);
    }
    
    @Test
    @JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
    public void testCountMatchingSummaries(){
        
        int count = m_daoOutageRepo.countMatchingOutageSummaries(new OutageCriteria());
        assertEquals(2, count);
    }
}
