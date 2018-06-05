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
import org.junit.rules.TemporaryFolder;
import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.persistence.rrd.RrdPersisterFactory;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.protocols.xml.config.XmlRrd;
import org.opennms.protocols.xml.dao.jaxb.XmlDataCollectionConfigDaoJaxb;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * The Abstract Class for Testing the XML Collector.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public abstract class XmlCollectorITCase {

    /** The temporary folder. */
    @Rule
    public TemporaryFolder m_temporaryFolder = new TemporaryFolder();

    /** The collection agent. */
    private CollectionAgent m_collectionAgent;

    /** The event proxy. */
    private EventProxy m_eventProxy;

    /** The XML collection DAO. */
    private XmlDataCollectionConfigDaoJaxb m_xmlCollectionDao;

    /** The RRD strategy. */
    private RrdStrategy<?, ?> m_rrdStrategy;

    /** The resource storage DAO. */
    private FilesystemResourceStorageDao m_resourceStorageDao;

    /** The persister factory. */
    private RrdPersisterFactory m_persisterFactory;

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        initializeRrdStrategy();
        initializeDocumentBuilder();
        
        m_collectionAgent = new MockCollectionAgent(1, "mynode.local", InetAddrUtils.addr("127.0.0.1"));
        m_eventProxy = EasyMock.createMock(EventProxy.class);

        m_xmlCollectionDao = new XmlDataCollectionConfigDaoJaxb();
        Resource resource = new FileSystemResource(getConfigFileName());
        m_xmlCollectionDao.setConfigResource(resource);
        m_xmlCollectionDao.afterPropertiesSet();

        EasyMock.replay(m_eventProxy);
    }

    /**
     * Initialize document builder.
     */
    protected void initializeDocumentBuilder() {
        MockDocumentBuilder.setXmlFileName(getSampleFileName());
    }

    /**
     * Initialize RRD strategy.
     *
     * @throws Exception the exception
     */
    protected void initializeRrdStrategy() throws Exception {
        m_rrdStrategy = new JRobinRrdStrategy();
        m_resourceStorageDao = new FilesystemResourceStorageDao();
        m_resourceStorageDao.setRrdDirectory(m_temporaryFolder.getRoot());
        m_temporaryFolder.newFolder("snmp");

        m_persisterFactory = new RrdPersisterFactory();
        m_persisterFactory.setResourceStorageDao(m_resourceStorageDao);
        m_persisterFactory.setRrdStrategy(m_rrdStrategy);
    }

    /**
     * Gets the RRD strategy.
     *
     * @return the RRD strategy
     * @throws Exception the exception
     */
    protected RrdStrategy<?, ?> getRrdStrategy() throws Exception {
        return m_rrdStrategy;
    }

    /**
     * Gets the RRD extension.
     *
     * @return the RRD extension
     */
    protected String getRrdExtension() {
        return m_rrdStrategy.getDefaultFileExtension().substring(1);
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
     * Gets the test sample file name.
     *
     * @return the test sample file name
     */
    public abstract String getSampleFileName();

    /**
     * Gets the test configuration file name.
     *
     * @return the test configuration file name
     */
    public abstract String getConfigFileName();

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
     * Executes collector test.
     *
     * @param parameters the parameters
     * @param expectedFiles the expected amount of JRB files
     * @return the collection set
     * @throws Exception the exception
     */
    public CollectionSet executeCollectorTest(Map<String, Object> parameters, int expectedFiles) throws Exception {
        XmlCollector collector = new XmlCollector();
        collector.setXmlCollectionDao(m_xmlCollectionDao);

        CollectionSet collectionSet = XmlCollectorTestUtils.doCollect(collector, m_collectionAgent, parameters);
        Assert.assertEquals(CollectionStatus.SUCCEEDED, collectionSet.getStatus());

        ServiceParameters serviceParams = new ServiceParameters(new HashMap<String,Object>());
        CollectionSetVisitor persister = m_persisterFactory.createGroupPersister(serviceParams, createRrdRepository((String)parameters.get("collection")), false, false);
        collectionSet.visit(persister);

        Assert.assertEquals(expectedFiles, FileUtils.listFiles(getSnmpRootDirectory(), new String[] { getRrdExtension() }, true).size());
        return collectionSet;
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
        repository.setRrdBaseDir(getSnmpRootDirectory());
        repository.setHeartBeat(rrd.getStep() * 2);
        repository.setStep(rrd.getStep());
        repository.setRraList(rrd.getXmlRras());
        return repository;
    }

    /**
     * Gets the SNMP root directory.
     *
     * @return the SNMP root directory.
     */
    public File getSnmpRootDirectory() {
        return new File(m_temporaryFolder.getRoot(), "snmp");
    }
}
