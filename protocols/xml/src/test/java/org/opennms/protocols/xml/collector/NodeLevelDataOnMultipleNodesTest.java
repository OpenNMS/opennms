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
import org.jrobin.core.RrdException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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
public class NodeLevelDataOnMultipleNodesTest {

    @Rule
    public TemporaryFolder m_temporaryFolder = new TemporaryFolder();

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

        m_xmlCollectionDao = new XmlDataCollectionConfigDaoJaxb();
        Resource resource = new FileSystemResource(getXmlConfigFileName());
        m_xmlCollectionDao.setConfigResource(resource);
        m_xmlCollectionDao.afterPropertiesSet();

        m_collector = new XmlCollector();
        m_collector.setXmlCollectionDao(m_xmlCollectionDao);
    }

    protected RrdStrategy<?, ?> getRrdStrategy() throws Exception {
        return new MultithreadedJniRrdStrategy();
    }

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
        File file = new File(getSnmpRoot(), "1/node-level-stats.rrd");
        String[] dsnames = new String[] { "v1", "v2", "v3", "v4", "v5", "v6" };
        Double[] dsvalues = new Double[] { 10.0, 11.0, 12.0, 13.0, 14.0, 15.0 };
        validateRrd(file, dsnames, dsvalues);

        executeCollectorTest(2, "127.0.0.2", "src/test/resources/node-level-2.xml", parameters, 1);
        file = new File(getSnmpRoot(), "2/node-level-stats.rrd");
        dsnames = new String[] { "v1", "v2", "v3", "v4", "v5", "v6" };
        dsvalues = new Double[] { 20.0, 21.0, 22.0, 23.0, 24.0, 25.0 };
        validateRrd(file, dsnames, dsvalues);

        executeCollectorTest(3, "127.0.0.3", "src/test/resources/node-level-3.xml", parameters, 1);
        file = new File(getSnmpRoot(), "3/node-level-stats.rrd");
        dsnames = new String[] { "v1", "v2", "v3", "v4", "v5", "v6" };
        dsvalues = new Double[] { 30.0, 31.0, 32.0, 33.0, 34.0, 35.0 };
        validateRrd(file, dsnames, dsvalues);
    }

    /**
     * Gets the RRD extension.
     *
     * @return the RRD extension
     */
    protected String getRrdExtension() {
        return "rrd";
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

        CollectionAgent collectionAgent = new MockCollectionAgent(nodeId, "mynode", InetAddressUtils.addr(ipAddress));

        CollectionSet collectionSet = XmlCollectorTestUtils.doCollect(m_collector, collectionAgent, parameters);
        Assert.assertEquals(CollectionStatus.SUCCEEDED, collectionSet.getStatus());

        ServiceParameters serviceParams = new ServiceParameters(new HashMap<String,Object>());

        CollectionSetVisitor persister = m_persisterFactory.createGroupPersister(serviceParams, createRrdRepository((String)parameters.get("collection")), false, false);
        collectionSet.visit(persister);

        Assert.assertEquals(expectedFiles, FileUtils.listFiles(new File(getSnmpRoot(), Integer.toString(nodeId)), new String[] { getRrdExtension() }, true).size());
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
