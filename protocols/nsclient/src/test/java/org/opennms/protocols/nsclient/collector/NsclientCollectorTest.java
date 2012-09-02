/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.protocols.nsclient.collector;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockPlatformTransactionManager;
import org.opennms.netmgt.collectd.AbstractCollectionSetVisitor;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.DefaultCollectionAgent;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.protocols.nsclient.AbstractNsclientTest;
import org.opennms.protocols.nsclient.collector.NSClientCollector;
import org.opennms.protocols.nsclient.config.NSClientDataCollectionConfigFactory;
import org.opennms.protocols.nsclient.config.NSClientPeerFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * <p>JUnit Test Class for NsclientCollector.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public class NsclientCollectorTest extends AbstractNsclientTest {

    private PlatformTransactionManager m_transactionManager;

    private IpInterfaceDao m_ipInterfaceDao;

    private EventProxy m_eventProxy;

    private CollectionAgent m_collectionAgent;

    private class CountResourcesVisitor extends AbstractCollectionSetVisitor {

        private int count = 0;

        public int getCount() {
            return count;
        }

        @Override
        public void visitAttribute(CollectionAttribute attribute) {
            count++;
            Assert.assertEquals(10, Integer.parseInt(attribute.getNumericValue()));
        }
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        startServer("None&8&", "10");

        // Initialize Mocks
        m_transactionManager = new MockPlatformTransactionManager();
        m_ipInterfaceDao = EasyMock.createMock(IpInterfaceDao.class);
        m_eventProxy = EasyMock.createMock(EventProxy.class);
        NetworkBuilder builder = new NetworkBuilder();
        builder.addNode("winsrv");
        builder.addInterface(getServer().getInetAddress().getHostAddress()).addSnmpInterface(1).setCollectionEnabled(true);
        builder.getCurrentNode().setId(1);
        OnmsIpInterface iface = builder.getCurrentNode().getIpInterfaces().iterator().next();
        iface.setIsSnmpPrimary(PrimaryType.PRIMARY);
        iface.setId(1);
        EasyMock.expect(m_ipInterfaceDao.load(1)).andReturn(iface).anyTimes();
        EasyMock.replay(m_ipInterfaceDao, m_eventProxy);

        // Initialize NSClient Configuration
        String nsclient_config = "<nsclient-config port=\"" + getServer().getLocalPort() + "\" retry=\"1\" timeout=\"3000\" />";
        NSClientPeerFactory.setInstance(new NSClientPeerFactory(new ByteArrayInputStream(nsclient_config.getBytes())));
        NSClientDataCollectionConfigFactory.setInstance(new NSClientDataCollectionConfigFactory("src/test/resources/nsclient-datacollection-config.xml"));

        // Initialize Collection Agent
        m_collectionAgent = DefaultCollectionAgent.create(1, m_ipInterfaceDao, m_transactionManager);
    }

    @After
    public void tearDown() throws Exception {
        stopServer();
        EasyMock.verify(m_ipInterfaceDao, m_eventProxy);
        super.tearDown();
    }

    @Test
    public void testCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("port", getServer().getLocalPort());
        NSClientCollector collector = getCollector(parameters);
        CollectionSet collectionSet = collector.collect(m_collectionAgent, m_eventProxy, parameters);
        Assert.assertEquals(ServiceCollector.COLLECTION_SUCCEEDED, collectionSet.getStatus());
        CountResourcesVisitor visitor = new CountResourcesVisitor();
        collectionSet.visit(visitor);
        Assert.assertEquals(42, visitor.getCount());
    }

    private NSClientCollector getCollector(Map<String, Object> parameters) {
        NSClientCollector collector = new NSClientCollector();
        collector.initialize(m_collectionAgent, parameters);
        return collector;
    }

}
