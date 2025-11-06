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
package org.opennms.protocols.xml.collector;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.persistence.rrd.RrdPersisterFactory;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.model.v3.DS;
import org.opennms.netmgt.rrd.model.v3.RRDv3;
import org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy;
import org.opennms.protocols.xml.config.XmlRrd;
import org.opennms.protocols.xml.dao.jaxb.XmlDataCollectionConfigDaoJaxb;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

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
        
        m_collectionAgent = new MockCollectionAgent(1, "mynode.local", InetAddressUtils.addr("127.0.0.1"));

        m_xmlCollectionDao = new XmlDataCollectionConfigDaoJaxb();
        Resource resource = new FileSystemResource(getConfigFileName());
        m_xmlCollectionDao.setConfigResource(resource);
        m_xmlCollectionDao.afterPropertiesSet();
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
        m_rrdStrategy = new MultithreadedJniRrdStrategy();
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
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Executes collector test.
     *
     * @param parameters the parameters
     * @param expectedFiles the expected amount of RRD files
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
     * Validates a RRD.
     * <p>It assumes storeByGroup=true</p>
     * 
     * @param file the RRD file instance
     * @param dsnames the array of data source names
     * @param dsvalues the array of data source values
     * @throws Exception the exception
     */
    public void validateRrd(File file, String[] dsnames, Double[] dsvalues) throws Exception {
        Assert.assertTrue(file.exists());
        try {
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            Process process = Runtime.getRuntime().exec(new String[] {"rrdtool", "dump", file.getAbsolutePath()});
            SAXSource source = new SAXSource(xmlReader, new InputSource(new InputStreamReader(process.getInputStream())));
            JAXBContext jc = JAXBContext.newInstance(RRDv3.class);
            Unmarshaller u = jc.createUnmarshaller();
            final RRDv3 rrdv3 = (RRDv3) u.unmarshal(source);

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
