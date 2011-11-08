/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
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
package org.opennms.protocols.xml.collector;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.opennms.netmgt.collectd.BasePersister;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.collectd.GroupPersister;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.protocols.xml.dao.jaxb.XmlDataCollectionConfigDaoJaxb;
import org.opennms.test.FileAnticipator;
import org.opennms.test.mock.MockLogAppender;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * The Class XmlCollectorTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class XmlCollectorTest {

    /** The file anticipator. */
    private FileAnticipator m_fileAnticipator;

    private CollectionAgent m_collectionAgent;
    private EventProxy m_eventProxy;
    private XmlDataCollectionConfigDaoJaxb m_xmlCollectionDao;

    /** The SNMP directory. */
    private File m_rrdDirectory;

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        m_fileAnticipator = new FileAnticipator();
        m_collectionAgent = EasyMock.createMock(CollectionAgent.class);
        EasyMock.expect(m_collectionAgent.getNodeId()).andReturn(1).anyTimes();
        EasyMock.expect(m_collectionAgent.getHostAddress()).andReturn("127.0.0.1").anyTimes();
        m_eventProxy = EasyMock.createMock(EventProxy.class);

        m_xmlCollectionDao = new XmlDataCollectionConfigDaoJaxb();
        Resource resource = new FileSystemResource("src/test/resources/xml-datacollection-config.xml");
        m_xmlCollectionDao.setConfigResource(resource);
        m_xmlCollectionDao.afterPropertiesSet();

        EasyMock.replay(m_collectionAgent, m_eventProxy);
    }

    /**
     * Tear down.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        EasyMock.verify(m_collectionAgent, m_eventProxy);
        m_fileAnticipator.deleteExpected();
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Test XML collector.
     *
     * @throws Exception the exception
     */
    @Test
    public void testXmlCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "3GPP");
        parameters.put("handler-class", "org.opennms.protocols.xml.collector.MockDefaultXmlCollectionHandler");
        doTest(parameters);
    }
    
    /**
     * Test XML collector.
     *
     * @throws Exception the exception
     */
    @Test
    public void test3GPPXmlCollector() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("collection", "3GPP");
        parameters.put("handler-class", "org.opennms.protocols.xml.collector.MockSftp3gppXmlCollectionHandler");
        doTest(parameters);
    }

    /**
     * Do test.
     *
     * @param parameters the parameters
     * @throws CollectionException the collection exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void doTest(Map<String, Object> parameters) throws Exception {
        XmlCollector collector = new XmlCollector();
        collector.setXmlCollectionDao(m_xmlCollectionDao);
        collector.initialize(m_collectionAgent, parameters);
        CollectionSet collectionSet = collector.collect(m_collectionAgent, m_eventProxy, parameters);
        collector.release(m_collectionAgent);
        Assert.assertEquals(ServiceCollector.COLLECTION_SUCCEEDED, collectionSet.getStatus());

        ServiceParameters serviceParams = new ServiceParameters(new HashMap<String,Object>());
        BasePersister persister =  new GroupPersister(serviceParams, createRrdRepository()); // storeByGroup=true;
        collectionSet.visit(persister);
    }

    /**
     * Creates the RRD repository.
     *
     * @return the RRD repository
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private RrdRepository createRrdRepository() throws IOException {
        RrdRepository repository = new RrdRepository();
        repository.setRrdBaseDir(getRrdDirectory());
        repository.setHeartBeat(600);
        repository.setStep(300);
        repository.setRraList(Collections.singletonList("RRA:AVERAGE:0.5:1:100"));
        return repository;
    }

    /**
     * Gets the RRD directory.
     *
     * @return the RRD directory
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private File getRrdDirectory() throws IOException {
        if (m_rrdDirectory == null) {
            m_rrdDirectory = m_fileAnticipator.tempDir("snmp"); 
        }
        return m_rrdDirectory;
    }

}
