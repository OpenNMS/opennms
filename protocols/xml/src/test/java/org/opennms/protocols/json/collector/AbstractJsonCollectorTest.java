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

package org.opennms.protocols.json.collector;

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
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.collectd.BasePersister;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.GroupPersister;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.protocols.xml.collector.XmlCollector;
import org.opennms.protocols.xml.config.XmlRrd;
import org.opennms.protocols.xml.dao.jaxb.XmlDataCollectionConfigDaoJaxb;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * The Abstract Class for Testing the JSON Collector.
 * 
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public abstract class AbstractJsonCollectorTest {

    /** The Constant TEST_SNMP_DIRECTORY. */
    private static final String TEST_SNMP_DIRECTORY = "target/snmp/";

    /** The collection agent. */
    private CollectionAgent m_collectionAgent;

    /** The event proxy. */
    private EventProxy m_eventProxy;

    /** The XML collection DAO. */
    private XmlDataCollectionConfigDaoJaxb m_xmlCollectionDao;

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(new File(TEST_SNMP_DIRECTORY));
        MockLogAppender.setupLogging();

        System.setProperty("org.opennms.rrd.usetcp", "false");
        System.setProperty("org.opennms.rrd.usequeue", "false");
        System.setProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy");

        m_collectionAgent = EasyMock.createMock(CollectionAgent.class);
        EasyMock.expect(m_collectionAgent.getNodeId()).andReturn(1).anyTimes();
        EasyMock.expect(m_collectionAgent.getHostAddress()).andReturn("127.0.0.1").anyTimes();
        EasyMock.expect(m_collectionAgent.getStorageDir()).andReturn(new File(String.valueOf(1))).anyTimes();
        m_eventProxy = EasyMock.createMock(EventProxy.class);

        m_xmlCollectionDao = new XmlDataCollectionConfigDaoJaxb();
        Resource resource = new FileSystemResource(getJSONConfigFileName());
        m_xmlCollectionDao.setConfigResource(resource);
        m_xmlCollectionDao.afterPropertiesSet();
        MockDocumentBuilder.setJSONFileName(getJSONSampleFileName());

        EasyMock.replay(m_collectionAgent, m_eventProxy);
    }

    /**
     * Gets the XML configuration DAO.
     *
     * @return the XML configuration DAO
     */
    public XmlDataCollectionConfigDaoJaxb getConfigDao() {
        return m_xmlCollectionDao;
    }

    /**
     * Gets the test JSON sample file name.
     *
     * @return the test JSON sample file name
     */
    public abstract String getJSONSampleFileName();

    /**
     * Gets the test JSON configuration file name.
     *
     * @return the test JSON configuration file name
     */
    public abstract String getJSONConfigFileName();

    /**
     * Tear down.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        EasyMock.verify(m_collectionAgent, m_eventProxy);
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Executes collector test.
     *
     * @param parameters the parameters
     * @param expectedFiles the expected amount of JRB files
     * @throws Exception the exception
     */
    public void executeCollectorTest(Map<String, Object> parameters, int expectedFiles) throws Exception {
        XmlCollector collector = new XmlCollector();
        collector.setXmlCollectionDao(m_xmlCollectionDao);
        collector.initialize(m_collectionAgent, parameters);
        CollectionSet collectionSet = collector.collect(m_collectionAgent, m_eventProxy, parameters);
        collector.release(m_collectionAgent);
        Assert.assertEquals(ServiceCollector.COLLECTION_SUCCEEDED, collectionSet.getStatus());

        ServiceParameters serviceParams = new ServiceParameters(new HashMap<String,Object>());
        BasePersister persister =  new GroupPersister(serviceParams, createRrdRepository((String)parameters.get("collection"))); // storeByGroup=true;
        collectionSet.visit(persister);
        
        Assert.assertEquals(expectedFiles, FileUtils.listFiles(new File(TEST_SNMP_DIRECTORY), new String[] { "jrb" }, true).size());
    }
    
    /**
     * Validates a JRB.
     * <p>It assumes storeByGroup=true</p>
     * 
     * @param file the JRB file instance
     * @param dsnames the array of data source names
     * @param dsvalues the array of data source values
     * @throws Exception the exception
     */
    public void validateJrb(File file, String[] dsnames, Double[] dsvalues) throws Exception {
        Assert.assertTrue(file.exists());
        RrdDb jrb = new RrdDb(file);
        Assert.assertEquals(dsnames.length, jrb.getDsCount());
        for (int i = 0; i < dsnames.length; i++) {
            Datasource ds = jrb.getDatasource(dsnames[i]);
            Assert.assertNotNull(ds);
            Assert.assertEquals(dsvalues[i], Double.valueOf(ds.getLastValue()));
        }

    }

    /**
     * Creates the RRD repository.
     *
     * @return the RRD repository
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private RrdRepository createRrdRepository(String collection) throws IOException {
        XmlRrd rrd = m_xmlCollectionDao.getDataCollectionByName(collection).getXmlRrd();
        RrdRepository repository = new RrdRepository();
        repository.setRrdBaseDir(new File(TEST_SNMP_DIRECTORY));
        repository.setHeartBeat(rrd.getStep() * 2);
        repository.setStep(rrd.getStep());
        repository.setRraList(rrd.getXmlRras());
        return repository;
    }

}
