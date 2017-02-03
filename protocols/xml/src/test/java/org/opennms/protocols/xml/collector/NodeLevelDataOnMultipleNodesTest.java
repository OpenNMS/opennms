/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.persistence.rrd.RrdPersisterFactory;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.protocols.xml.config.XmlRrd;
import org.opennms.protocols.xml.dao.jaxb.XmlDataCollectionConfigDaoJaxb;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * The Abstract Class for Testing the XML Collector.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class NodeLevelDataOnMultipleNodesTest {

    @Rule
    public TemporaryFolder m_temporaryFolder = new TemporaryFolder();

    /** The event proxy. */
    private EventProxy m_eventProxy;

    /** The XML collection DAO. */
    private XmlDataCollectionConfigDaoJaxb m_xmlCollectionDao;

    /** The XML collector. */
    private XmlCollector m_collector;

    /** The RRD strategy. */
    private RrdStrategy<?, ?> m_rrdStrategy;

    private FilesystemResourceStorageDao m_resourceStorageDao;

    private RrdPersisterFactory m_persisterFactory;

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_rrdStrategy = getRrdStrategy();
        m_resourceStorageDao = new FilesystemResourceStorageDao();
        m_resourceStorageDao.setRrdDirectory(m_temporaryFolder.getRoot());
        m_temporaryFolder.newFolder("snmp");

        m_persisterFactory = new RrdPersisterFactory();
        m_persisterFactory.setResourceStorageDao(m_resourceStorageDao);
        m_persisterFactory.setRrdStrategy(m_rrdStrategy);

        m_eventProxy = EasyMock.createMock(EventProxy.class);

        m_xmlCollectionDao = new XmlDataCollectionConfigDaoJaxb();
        Resource resource = new FileSystemResource(getXmlConfigFileName());
        m_xmlCollectionDao.setConfigResource(resource);
        m_xmlCollectionDao.afterPropertiesSet();

        m_collector = new XmlCollector();
        m_collector.setXmlCollectionDao(m_xmlCollectionDao);

        EasyMock.replay(m_eventProxy);
    }

    protected RrdStrategy<?, ?> getRrdStrategy() throws Exception {
        return new JRobinRrdStrategy();
    }

    /**
     * Tear down.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        EasyMock.verify(m_eventProxy);
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Test XML collector with default handler for several nodes with node-level data
     *
     * @throws Exception the exception
     */
    @Test
    public void testMultipleNodes() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "NodeLevel");
        parameters.put("handler-class", "org.opennms.protocols.xml.collector.MockDefaultXmlCollectionHandler");

        executeCollectorTest(1, "127.0.0.1", "src/test/resources/node-level.xml", parameters, 1);
        File file = new File(getSnmpRoot(), "1/node-level-stats.jrb");
        String[] dsnames = new String[] { "v1", "v2", "v3", "v4", "v5", "v6" };
        Double[] dsvalues = new Double[] { 10.0, 11.0, 12.0, 13.0, 14.0, 15.0 };
        validateJrb(file, dsnames, dsvalues);

        executeCollectorTest(2, "127.0.0.2", "src/test/resources/node-level-2.xml", parameters, 1);
        file = new File(getSnmpRoot(), "2/node-level-stats.jrb");
        dsnames = new String[] { "v1", "v2", "v3", "v4", "v5", "v6" };
        dsvalues = new Double[] { 20.0, 21.0, 22.0, 23.0, 24.0, 25.0 };
        validateJrb(file, dsnames, dsvalues);

        executeCollectorTest(3, "127.0.0.3", "src/test/resources/node-level-3.xml", parameters, 1);
        file = new File(getSnmpRoot(), "3/node-level-stats.jrb");
        dsnames = new String[] { "v1", "v2", "v3", "v4", "v5", "v6" };
        dsvalues = new Double[] { 30.0, 31.0, 32.0, 33.0, 34.0, 35.0 };
        validateJrb(file, dsnames, dsvalues);
    }

    /**
     * Gets the RRD extension.
     *
     * @return the RRD extension
     */
    protected String getRrdExtension() {
        return "jrb";
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
     * Gets the test XML sample file name.
     *
     * @return the test XML sample file name
     */
    public String getXmlConfigFileName() {
        return "src/test/resources/node-level-datacollection-config.xml";
    }

    /**
     * Executes collector test.
     *
     * @param nodeId the node id
     * @param ipAddress the IP address
     * @param xmlSampleFileName the XML sample file name
     * @param parameters the parameters
     * @param expectedFiles the expected amount of JRB files
     * @throws Exception the exception
     */
    public void executeCollectorTest(int nodeId, String ipAddress, String xmlSampleFileName, Map<String, Object> parameters, int expectedFiles) throws Exception {
        MockDocumentBuilder.setXmlFileName(xmlSampleFileName);

        CollectionAgent collectionAgent = EasyMock.createMock(CollectionAgent.class);
        EasyMock.expect(collectionAgent.getNodeId()).andReturn(nodeId).anyTimes();
        EasyMock.expect(collectionAgent.getHostAddress()).andReturn(ipAddress).anyTimes();
        EasyMock.expect(collectionAgent.getStorageResourcePath()).andReturn(ResourcePath.get(Integer.toString(nodeId))).anyTimes();
        EasyMock.replay(collectionAgent);

        m_collector.initialize(collectionAgent, parameters);
        CollectionSet collectionSet = m_collector.collect(collectionAgent, m_eventProxy, parameters);
        m_collector.release(collectionAgent);
        collectionSet = m_collector.collect(collectionAgent, m_eventProxy, parameters);
        m_collector.release(collectionAgent);
        Assert.assertEquals(ServiceCollector.COLLECTION_SUCCEEDED, collectionSet.getStatus());

        ServiceParameters serviceParams = new ServiceParameters(new HashMap<String,Object>());

        CollectionSetVisitor persister = m_persisterFactory.createGroupPersister(serviceParams, createRrdRepository((String)parameters.get("collection")), false, false);
        collectionSet.visit(persister);

        Assert.assertEquals(expectedFiles, FileUtils.listFiles(new File(getSnmpRoot(), Integer.toString(nodeId)), new String[] { getRrdExtension() }, true).size());
        EasyMock.verify(collectionAgent);
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
        repository.setRrdBaseDir(getSnmpRoot());
        repository.setHeartBeat(rrd.getStep() * 2);
        repository.setStep(rrd.getStep());
        repository.setRraList(rrd.getXmlRras());
        return repository;
    }

    public File getSnmpRoot() {
        return new File(m_temporaryFolder.getRoot(), "snmp");
    }
}
