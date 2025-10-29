/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.protocols.xml.vtdxml;

import org.apache.commons.io.FileUtils;
import org.jrobin.core.RrdException;
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
import org.opennms.netmgt.rrd.model.RrdConvertUtils;
import org.opennms.netmgt.rrd.model.v3.DS;
import org.opennms.netmgt.rrd.model.v3.RRDv3;
import org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.protocols.xml.collector.XmlCollector;
import org.opennms.protocols.xml.collector.XmlCollectorTestUtils;
import org.opennms.protocols.xml.config.XmlRrd;
import org.opennms.protocols.xml.dao.jaxb.XmlDataCollectionConfigDaoJaxb;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * The Abstract Class for Testing the XML Collector using VTD-XML.
 *
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 */
public abstract class AbstractVTDXmlCollectorTest {

    @Rule
    public TemporaryFolder m_temporaryFolder = new TemporaryFolder();

    /** The collection agent. */
    private CollectionAgent m_collectionAgent;

    /** The event proxy. */
    private EventProxy m_eventProxy;

    /** The XML collection DAO. */
    private XmlDataCollectionConfigDaoJaxb m_xmlCollectionDao;

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
        System.setProperty("rrd.binary", "rrdtool");
        MockLogAppender.setupLogging();

        m_rrdStrategy = new MultithreadedJniRrdStrategy();
        m_resourceStorageDao = new FilesystemResourceStorageDao();
        m_resourceStorageDao.setRrdDirectory(m_temporaryFolder.getRoot());
        m_temporaryFolder.newFolder("snmp");

        m_persisterFactory = new RrdPersisterFactory();
        m_persisterFactory.setResourceStorageDao(m_resourceStorageDao);
        m_persisterFactory.setRrdStrategy(m_rrdStrategy);

        m_collectionAgent = new MockCollectionAgent(1, "mynode.local", InetAddrUtils.addr("127.0.0.1"));
        m_eventProxy = mock(EventProxy.class);

        m_xmlCollectionDao = new XmlDataCollectionConfigDaoJaxb();
        Resource resource = new FileSystemResource(getXmlConfigFileName());
        m_xmlCollectionDao.setConfigResource(resource);
        m_xmlCollectionDao.afterPropertiesSet();
        MockDocumentBuilder.setXmlFileName(getXmlSampleFileName());
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
    public abstract String getXmlSampleFileName();

    /**
     * Gets the test XML configuration file name.
     *
     * @return the test XML configuration file name
     */
    public abstract String getXmlConfigFileName();

    /**
     * Tear down.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_eventProxy);
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
        CollectionSet collectionSet = XmlCollectorTestUtils.doCollect(collector, m_collectionAgent, parameters);
        Assert.assertEquals(CollectionStatus.SUCCEEDED, collectionSet.getStatus());

        ServiceParameters serviceParams = new ServiceParameters(new HashMap<String,Object>());
        CollectionSetVisitor persister = m_persisterFactory.createGroupPersister(serviceParams, createRrdRepository((String)parameters.get("collection")), false, false);
        collectionSet.visit(persister);

        Assert.assertEquals(expectedFiles, FileUtils.listFiles(getSnmpRoot(), new String[] { "rrd" }, true).size());
    }

    /**
     * Validates a RRD.
     * <p>It assumes storeByGroup=true</p>
     *
     * @param file the JRB file instance
     * @param dsnames the array of data source names
     * @param dsvalues the array of data source values
     * @throws Exception the exception
     */
    public void validateRrd(File file, String[] dsnames, Double[] dsvalues) throws Exception {
        Assert.assertTrue(file.exists());
        try {
            final RRDv3 rrdv3 = RrdConvertUtils.dumpRrd(file);

            Assert.assertEquals(dsnames.length, rrdv3.getDataSources().size());
            for (int i = 0; i < dsnames.length; i++) {
                final String dsname = dsnames[i];
                final Optional<DS> ds = rrdv3.getDataSources().stream().filter(d -> dsname.equals(d.getName())).findAny();
                Assert.assertTrue(ds.isPresent());
                Assert.assertEquals(dsvalues[i], ds.get().getLastDs());
            }
        } catch (Exception e) {
            throw new RrdException("Can't parse RRD Dump", e);
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
