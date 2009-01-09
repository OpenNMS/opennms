//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Aug 23: Use new RrdTestUtils.initializeNullStratgegy. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Test;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.HttpCollectionConfigFactory;
import org.opennms.netmgt.dao.support.RrdTestUtils;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class HttpCollectorTest extends AbstractCollectorTest {

    private HttpCollectionConfigFactory m_factory;
    
    private String m_testHostName = "www.opennms.org";
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("opennms.home", "/opt/opennms");
        MockUtil.println("------------ Begin Test " + getName() + " --------------------------");
        MockLogAppender.setupLogging();

        MockNetwork m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "testnode");
        m_network.addInterface(InetAddress.getByName(m_testHostName).getHostAddress());
        m_network.addService("ICMP");
        m_network.addService("HTTP");

        MockDatabase m_db = new MockDatabase();
        m_db.populate(m_network);

        DataSourceFactory.setInstance(m_db);
        
        MockEventIpcManager eventIpcManager = new MockEventIpcManager();
        
        EventIpcManagerFactory.setIpcManager(eventIpcManager);
        
        RrdTestUtils.initialize();
        
        initializeDatabaseSchemaConfig("/org/opennms/netmgt/config/test-database-schema.xml");
        
        setTransMgr();
        setFileAnticipator();
    }

    private void initializeHttpDatacollectionConfigFactory(String pathName) throws MarshalException, ValidationException, IOException {
        m_factory = new HttpCollectionConfigFactory(getDataCollectionConfigReader(pathName));
        HttpCollectionConfigFactory.setInstance(m_factory);
        HttpCollectionConfigFactory.init();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
     * Test method for {@link org.opennms.netmgt.collectd.HttpCollector#collect(
     *   org.opennms.netmgt.collectd.CollectionAgent, org.opennms.netmgt.model.events.EventProxy, java.util.Map)}.
     */
    @Test
    public final void testCollect() throws Exception {
        InetAddress opennmsDotOrg = InetAddress.getByName(m_testHostName);
        
        initializeHttpDatacollectionConfigFactory("/org/opennms/netmgt/config/http-datacollection-config.xml");
        RrdTestUtils.initializeNullStrategy();
        HttpCollector collector = new HttpCollector();
        OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
        OnmsNode node = new OnmsNode(distPoller);
        node.setId(1);
        OnmsIpInterface iface = new OnmsIpInterface(opennmsDotOrg.getHostAddress(), node );
        iface.setId(2);
        node.addIpInterface(iface);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("collection", "default");
        collector.initialize(parameters);
        
        CollectionSpecification spec = createCollectionSpec("HTTP", collector, "default");
        
        CollectionAgent agent = createCollectionAgent(iface);
        
        // node level collection
        File nodeDir = anticipatePath(getSnmpRrdDirectory(), "1");
        anticipateRrdFiles(nodeDir, "documentCount");
        anticipateRrdFiles(nodeDir, "documentType");
        anticipateRrdFiles(nodeDir, "greatAnswer");

        spec.initialize(agent);
        
        CollectionSet collectionSet = spec.collect(agent);
        assertEquals("collection status",
                     ServiceCollector.COLLECTION_SUCCEEDED,
                     collectionSet.getStatus());
        persistCollectionSet(spec, collectionSet);
        
        spec.release(agent);
        
        // Wait for any RRD writes to finish up
        Thread.sleep(1000);
    }

    @Test
    public final void testPersist() throws Exception {
        InetAddress opennmsDotOrg = InetAddress.getByName(m_testHostName);
        
        initializeHttpDatacollectionConfigFactory("/org/opennms/netmgt/config/http-datacollection-persist-test-config.xml");
        RrdTestUtils.initialize();
        HttpCollector collector = new HttpCollector();
        OnmsDistPoller distPoller = new OnmsDistPoller("localhost", "127.0.0.1");
        OnmsNode node = new OnmsNode(distPoller);
        node.setId(1);
        OnmsIpInterface iface = new OnmsIpInterface(opennmsDotOrg.getHostAddress(), node );
        iface.setId(2);
        node.addIpInterface(iface);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("collection", "default");
        collector.initialize(parameters);
        
        CollectionSpecification spec = createCollectionSpec("HTTP", collector, "default");
        
        CollectionAgent agent = createCollectionAgent(iface);
        
        // node level collection
        File nodeDir = anticipatePath(getSnmpRrdDirectory(), "1");
        anticipateRrdFiles(nodeDir, "documentCount", "documentType", "greatAnswer");
        File documentCountRrdFile = new File(nodeDir, rrd("documentCount"));
        File someNumberRrdFile = new File(nodeDir, rrd("someNumber"));
        File greatAnswerRrdFile = new File(nodeDir, rrd("greatAnswer"));
        
        int numUpdates = 2;
        int stepSizeInSecs = 1;
        
        int stepSizeInMillis = stepSizeInSecs*1000;

        spec.initialize(agent);
        
        collectNTimes(spec, agent, numUpdates);
        
        // This is the value of documentCount from the first test page
        // documentCount = Gauge32: 5
        assertEquals(5.0, RrdUtils.fetchLastValueInRange(documentCountRrdFile.getAbsolutePath(), "documentCount", stepSizeInMillis, stepSizeInMillis));

        // This is the value of documentType from the first test page
        // someNumber = Gauge32: 17
        assertEquals(17.0, RrdUtils.fetchLastValueInRange(someNumberRrdFile.getAbsolutePath(), "someNumber", stepSizeInMillis, stepSizeInMillis));

        // This is the value of greatAnswer from the second test page
        //someNumber = Gauge32: 42
        assertEquals(42.0, RrdUtils.fetchLastValueInRange(greatAnswerRrdFile.getAbsolutePath(), "greatAnswer", stepSizeInMillis, stepSizeInMillis));
        
        spec.release(agent);
        
        // Wait for any RRD writes to finish up
        Thread.sleep(1000);
    }

}
