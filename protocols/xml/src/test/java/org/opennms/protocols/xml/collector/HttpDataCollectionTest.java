/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
package org.opennms.protocols.xml.collector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;

import org.jrobin.core.Datasource;
import org.jrobin.core.RrdDb;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.http.JUnitHttpServerExecutionListener;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.collectd.BasePersister;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.GroupPersister;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DefaultDataCollectionConfigDao;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.protocols.http.collector.HttpCollectionHandler;
import org.opennms.protocols.json.collector.DefaultJsonCollectionHandler;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.protocols.xml.config.XmlRrd;

import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The Test Class for HTTP Data Collection.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    JUnitHttpServerExecutionListener.class
})
public class HttpDataCollectionTest {

    /** The Constant TEST_SNMP_DIRECTORY. */
    private static final String TEST_SNMP_DIRECTORY = "target/snmp/";

    /** The collection agent. */
    private CollectionAgent m_collectionAgent;

    /** The OpenNMS Node DAO. */
    private NodeDao m_nodeDao;

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(TEST_SNMP_DIRECTORY));
        MockLogAppender.setupLogging();
        DefaultDataCollectionConfigDao dao = new DefaultDataCollectionConfigDao();
        dao.setConfigDirectory("src/test/resources/etc/datacollection");
        dao.setConfigResource(new FileSystemResource("src/test/resources/etc/datacollection-config.xml"));
        dao.afterPropertiesSet();
        DataCollectionConfigFactory.setInstance(dao);

        System.setProperty("org.opennms.rrd.usetcp", "false");
        System.setProperty("org.opennms.rrd.usequeue", "false");
        RrdUtils.setStrategy(new JRobinRrdStrategy());

        m_collectionAgent = EasyMock.createMock(CollectionAgent.class);
        EasyMock.expect(m_collectionAgent.getNodeId()).andReturn(1).anyTimes();
        EasyMock.expect(m_collectionAgent.getHostAddress()).andReturn("127.0.0.1").anyTimes();
        EasyMock.expect(m_collectionAgent.getStorageDir()).andReturn(new File("1")).anyTimes();

        m_nodeDao = EasyMock.createMock(NodeDao.class);
        OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("mynode.local");
        node.setAssetRecord(new OnmsAssetRecord());
        EasyMock.expect(m_nodeDao.get(1)).andReturn(node).anyTimes();
        EasyMock.replay(m_collectionAgent, m_nodeDao);
    }

    /**
     * Tear down.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        EasyMock.verify(m_collectionAgent, m_nodeDao);
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Test HTTP Data Collection with XPath
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitHttpServer(port=10342, https=false, webapps={
            @Webapp(context="/junit", path="src/test/resources/test-webapp")
    })
    public void testHttpCollection() throws Exception {
        File configFile = new File("src/test/resources/http-datacollection-config.xml");
        XmlDataCollectionConfig config = JaxbUtils.unmarshal(XmlDataCollectionConfig.class, configFile);
        XmlDataCollection collection = config.getDataCollectionByName("Http-Count");
        RrdRepository repository = createRrdRepository(collection.getXmlRrd());

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "Http-Count");

        DefaultXmlCollectionHandler collector = new DefaultXmlCollectionHandler();
        collector.setNodeDao(m_nodeDao);
        collector.setRrdRepository(repository);
        collector.setServiceName("HTTP");

        XmlCollectionSet collectionSet = collector.collect(m_collectionAgent, collection, parameters);
        Assert.assertEquals(ServiceCollector.COLLECTION_SUCCEEDED, collectionSet.getStatus());

        ServiceParameters serviceParams = new ServiceParameters(new HashMap<String,Object>());
        BasePersister persister =  new GroupPersister(serviceParams, repository); // storeByGroup=true;
        collectionSet.visit(persister);

        RrdDb jrb = new RrdDb(new File("target/snmp/1/count-stats.jrb"));
        Assert.assertNotNull(jrb);
        Assert.assertEquals(1, jrb.getDsCount());
        Datasource ds = jrb.getDatasource("count");
        Assert.assertNotNull(ds);
        Assert.assertEquals(new Double(5), Double.valueOf(ds.getLastValue()));
    }

    /**
     * Test HTTP Data Collection with CSS Selector
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitHttpServer(port=10342, https=false, webapps={
            @Webapp(context="/junit", path="src/test/resources/test-webapp")
    })
    public void testCssSelectorHttpCollection() throws Exception {
        File configFile = new File("src/test/resources/http-datacollection-config.xml");
        XmlDataCollectionConfig config = JaxbUtils.unmarshal(XmlDataCollectionConfig.class, configFile);
        XmlDataCollection collection = config.getDataCollectionByName("Http-Market");
        RrdRepository repository = createRrdRepository(collection.getXmlRrd());

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "Http-Market");

        HttpCollectionHandler collector = new HttpCollectionHandler();
        collector.setNodeDao(m_nodeDao);
        collector.setRrdRepository(repository);
        collector.setServiceName("HTTP");

        XmlCollectionSet collectionSet = collector.collect(m_collectionAgent, collection, parameters);
        Assert.assertEquals(ServiceCollector.COLLECTION_SUCCEEDED, collectionSet.getStatus());

        ServiceParameters serviceParams = new ServiceParameters(new HashMap<String,Object>());
        BasePersister persister =  new GroupPersister(serviceParams, repository); // storeByGroup=true;
        collectionSet.visit(persister);

        RrdDb jrb = new RrdDb(new File("target/snmp/1/market.jrb"));
        Assert.assertNotNull(jrb);
        Assert.assertEquals(2, jrb.getDsCount());
        Datasource ds = jrb.getDatasource("nasdaq");
        Assert.assertNotNull(ds);
        Assert.assertEquals(new Double(3578.30), Double.valueOf(ds.getLastValue()));
    }

    /**
     * Test HTTP Data Collection with JSON
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitHttpServer(port=10342, https=false, webapps={
            @Webapp(context="/junit", path="src/test/resources/test-webapp")
    })
    public void testJsonHttpCollection() throws Exception {
        File configFile = new File("src/test/resources/solaris-zones-datacollection-config.xml");
        XmlDataCollectionConfig config = JaxbUtils.unmarshal(XmlDataCollectionConfig.class, configFile);
        XmlDataCollection collection = config.getDataCollectionByName("Solaris");
        RrdRepository repository = createRrdRepository(collection.getXmlRrd());

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "Solaris");

        DefaultJsonCollectionHandler collector = new DefaultJsonCollectionHandler();
        collector.setNodeDao(m_nodeDao);
        collector.setRrdRepository(repository);
        collector.setServiceName("HTTP");

        XmlCollectionSet collectionSet = collector.collect(m_collectionAgent, collection, parameters);
        Assert.assertEquals(ServiceCollector.COLLECTION_SUCCEEDED, collectionSet.getStatus());

        ServiceParameters serviceParams = new ServiceParameters(new HashMap<String,Object>());
        BasePersister persister =  new GroupPersister(serviceParams, repository); // storeByGroup=true;
        collectionSet.visit(persister);

        RrdDb jrb = new RrdDb(new File("target/snmp/1/solarisZoneStats/global/solaris-zone-stats.jrb"));
        Assert.assertNotNull(jrb);
        Assert.assertEquals(6, jrb.getDsCount());
        Datasource ds = jrb.getDatasource("nproc");
        Assert.assertNotNull(ds);
        Assert.assertEquals(new Double(245.0), Double.valueOf(ds.getLastValue()));
    }

    /**
     * Creates the RRD repository.
     *
     * @return the RRD repository
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private RrdRepository createRrdRepository(XmlRrd rrd) throws IOException {
        RrdRepository repository = new RrdRepository();
        repository.setRrdBaseDir(new File(TEST_SNMP_DIRECTORY));
        repository.setHeartBeat(rrd.getStep() * 2);
        repository.setStep(rrd.getStep());
        repository.setRraList(rrd.getXmlRras());
        return repository;
    }
}
