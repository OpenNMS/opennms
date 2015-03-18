/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.outage.OutageSummary;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author mhuot
 *
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class OutageDaoTest implements InitializingBean {
    @Autowired
    private DistPollerDao m_distPollerDao;
    
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private OutageDao m_outageDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private EventDao m_eventDao;

    @Autowired
    TransactionTemplate m_transTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                OnmsServiceType t = new OnmsServiceType("ICMP");
                m_serviceTypeDao.save(t);
            }
        });
    }

    @Test
    @Transactional
    public void testSave() {
        OnmsNode node = new OnmsNode(getLocalHostDistPoller());
        node.setLabel("localhost");
        m_nodeDao.save(node);

        OnmsIpInterface ipInterface = new OnmsIpInterface(addr("172.16.1.1"), node);

        OnmsServiceType serviceType = m_serviceTypeDao.findByName("ICMP");
        assertNotNull(serviceType);

        OnmsMonitoredService monitoredService = new OnmsMonitoredService(ipInterface, serviceType);

        OnmsEvent event = new OnmsEvent();

        OnmsOutage outage = new OnmsOutage(new Date(), monitoredService);
        outage.setServiceLostEvent(event);
        m_outageDao.save(outage);

        //it works we're so smart! hehe
        outage = m_outageDao.load(outage.getId());
        assertEquals("ICMP", outage.getMonitoredService().getServiceType().getName());
//        outage.setEventBySvcRegainedEvent();
        
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetMatchingOutages() {
        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                OnmsNode node = new OnmsNode(getLocalHostDistPoller());
                node.setLabel("localhost");
                m_nodeDao.save(node);
                insertEntitiesAndOutage("172.16.1.1", "ICMP", node);
            }
        });

        /*
         * We need to flush and finish the transaction because JdbcFilterDao
         * gets its own connection from the DataSource and won't see our data
         * otherwise.
         */

        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                String[] svcs = new String[] { "ICMP" };
                ServiceSelector selector = new ServiceSelector("ipAddr IPLIKE 172.16.1.1", Arrays.asList(svcs));
                Collection<OnmsOutage> outages = m_outageDao.matchingCurrentOutages(selector);
                assertEquals("outage count", 1, outages.size());
            }
        });
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetMatchingOutagesWithEmptyServiceList() {
        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                OnmsNode node = new OnmsNode(getLocalHostDistPoller());
                node.setLabel("localhost");
                m_nodeDao.save(node);
                insertEntitiesAndOutage("172.16.1.1", "ICMP", node);
            }
        });

        /*
         * We need to flush and finish the transaction because JdbcFilterDao
         * gets its own connection from the DataSource and won't see our data
         * otherwise.
         */

        m_transTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                ServiceSelector selector = new ServiceSelector("ipAddr IPLIKE 172.16.1.1", new ArrayList<String>(0));
                Collection<OnmsOutage> outages = m_outageDao.matchingCurrentOutages(selector);
                assertEquals(1, outages.size());
            }
        });
    }

    @Test
    @Transactional
    public void testDuplicateOutages() {
        for (final OnmsNode node : m_nodeDao.findAll()) {
            m_nodeDao.delete(node);
        }
        OnmsNode node = new OnmsNode(getLocalHostDistPoller());
        node.setLabel("shoes");
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.1", "ICMP", node);
        insertEntitiesAndOutage("172.20.1.1", "ICMP", node);
        
        node = new OnmsNode(getLocalHostDistPoller());
        node.setLabel("megaphone");
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.2", "ICMP", node);
        insertEntitiesAndOutage("172.17.1.2", "ICMP", node);
        insertEntitiesAndOutage("172.18.1.2", "ICMP", node);

        node = new OnmsNode(getLocalHostDistPoller());
        node.setLabel("grunties");
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.3", "ICMP", node);

        List<OutageSummary> outages = m_outageDao.getNodeOutageSummaries(0);
        System.err.println(outages);
        assertEquals(3, outages.size());
    }

    @Test
    @Transactional
    public void testLimitDuplicateOutages() {
        for (final OnmsNode node : m_nodeDao.findAll()) {
            m_nodeDao.delete(node);
        }
        OnmsNode node = new OnmsNode(getLocalHostDistPoller());
        node.setLabel("shoes");
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.1", "ICMP", node);
        insertEntitiesAndOutage("172.20.1.1", "ICMP", node);
        
        node = new OnmsNode(getLocalHostDistPoller());
        node.setLabel("megaphone");
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.2", "ICMP", node);
        insertEntitiesAndOutage("172.17.1.2", "ICMP", node);
        insertEntitiesAndOutage("172.18.1.2", "ICMP", node);

        node = new OnmsNode(getLocalHostDistPoller());
        node.setLabel("grunties");
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.3", "ICMP", node);

        List<OutageSummary> outages = m_outageDao.getNodeOutageSummaries(2);
        System.err.println(outages);
        assertEquals(2, outages.size());

        outages = m_outageDao.getNodeOutageSummaries(3);
        System.err.println(outages);
        assertEquals(3, outages.size());

        outages = m_outageDao.getNodeOutageSummaries(4);
        System.err.println(outages);
        assertEquals(3, outages.size());

        outages = m_outageDao.getNodeOutageSummaries(5);
        System.err.println(outages);
        assertEquals(3, outages.size());
    }

    private OnmsDistPoller getLocalHostDistPoller() {
        return m_distPollerDao.load("localhost");
    }
    
    private OnmsOutage insertEntitiesAndOutage(final String ipAddr, final String serviceName, OnmsNode node) {
        OnmsIpInterface ipInterface = getIpInterface(ipAddr, node);
        OnmsServiceType serviceType = getServiceType(serviceName);
        OnmsMonitoredService monitoredService = getMonitoredService(ipInterface, serviceType);
        
        OnmsEvent event = getEvent();

        OnmsOutage outage = getOutage(monitoredService, event);
        
        return outage;
    }

    private OnmsOutage getOutage(OnmsMonitoredService monitoredService, OnmsEvent event) {
        OnmsOutage outage = new OnmsOutage(new Date(), monitoredService);
        outage.setServiceLostEvent(event);
        m_outageDao.save(outage);
        return outage;
    }

    private OnmsEvent getEvent() {
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(getLocalHostDistPoller());
        event.setEventUei("foo!");
        event.setEventTime(new Date());
        event.setEventCreateTime(new Date());
        event.setEventSeverity(1);
        event.setEventSource("your mom");
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        m_eventDao.save(event);
        return event;
    }

    private OnmsMonitoredService getMonitoredService(OnmsIpInterface ipInterface, OnmsServiceType serviceType) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsMonitoredService.class)
            .add(Restrictions.eq("ipInterface", ipInterface))
            .add(Restrictions.eq("serviceType", serviceType));
        final List<OnmsMonitoredService> services = m_monitoredServiceDao.findMatching(criteria);
        OnmsMonitoredService monitoredService;
        if (services.size() > 0) {
            monitoredService = services.get(0);
        } else {
            monitoredService = new OnmsMonitoredService(ipInterface, serviceType);
        }
        m_monitoredServiceDao.save(monitoredService);
        return monitoredService;
    }

    private OnmsServiceType getServiceType(final String serviceName) {
        OnmsServiceType serviceType = m_serviceTypeDao.findByName(serviceName);
        assertNotNull("Couldn't find " + serviceName + " in the database", serviceType);
        return serviceType;
    }

    private OnmsIpInterface getIpInterface(String ipAddr, OnmsNode node) {
        OnmsIpInterface ipInterface = m_ipInterfaceDao.findByNodeIdAndIpAddress(node.getId(), ipAddr);
        if (ipInterface == null) {
            ipInterface = new OnmsIpInterface(addr(ipAddr), node);
            m_ipInterfaceDao.save(ipInterface);
        }
        return ipInterface;
    }
}
