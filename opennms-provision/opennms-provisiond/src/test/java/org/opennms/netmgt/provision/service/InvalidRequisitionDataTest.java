/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/importerServiceTest.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
@DirtiesContext
// @Ignore("These tests are fixed in 1.13, and backporting the fixes are not worth it.  Narf.")
public class InvalidRequisitionDataTest extends ProvisioningTestCase implements InitializingBean {
    
    @Autowired
    private MockNodeDao m_nodeDao;

    @Autowired
    private EventDao m_eventDao;
    
    @Autowired
    private Provisioner m_provisioner;
    
    @Autowired
    private ResourceLoader m_resourceLoader;
    
    @Autowired
    private MockEventIpcManager m_eventManager;

    @Autowired
    private DatabasePopulator m_populator;

    private EventAnticipator m_anticipator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        // clean out any existing nodes
        for (final OnmsNode node : m_nodeDao.findAll()) {
            m_nodeDao.delete(node);
        }
        m_nodeDao.flush();
        for (final OnmsEvent event : m_eventDao.findAll()) {
        	m_eventDao.delete(event);
        }

        MockLogAppender.setupLogging(true, "DEBUG");
        m_anticipator = new EventAnticipator();
        m_eventManager.setEventAnticipator(m_anticipator);
        m_eventManager.setSynchronous(true);
        m_provisioner.start();

        // make sure node scan scheduler is running initially
        getScanExecutor().resume();
        getScheduledExecutor().resume();
    }

    @After
    public void tearDown() throws Exception {
        waitForEverything();
        m_anticipator.verifyAnticipated();
        m_populator.resetDatabase();
    }

    @Test
    public void testImportInvalidAsset() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();
        assertEquals(0, m_nodeDao.countAll());

        final Resource invalidAssetFieldResource = getResource("classpath:/import_invalidAssetFieldName.xml");

        m_anticipator.anticipateEvent(getStarted(invalidAssetFieldResource));
        m_anticipator.anticipateEvent(getSuccessful(invalidAssetFieldResource));
        m_anticipator.anticipateEvent(getNodeAdded(nextNodeId));
        m_anticipator.anticipateEvent(getNodeGainedInterface(nextNodeId));
        m_anticipator.anticipateEvent(getNodeGainedService(nextNodeId));
        m_anticipator.anticipateEvent(getNodeScanCompleted(nextNodeId));

        // This requisition has an asset on some nodes called "pollercategory".
        // Change it to "pollerCategory" (capital 'C') and the test passes...
        m_provisioner.doImport(invalidAssetFieldResource.getURL().toString(), Boolean.TRUE.toString());
        waitForEverything();
        m_anticipator.verifyAnticipated();

        // should still import the node, just skip the asset field
        assertEquals(1, m_nodeDao.countAll());
        OnmsNode node = m_nodeDao.get(m_nodeDao.getNodeIds().iterator().next());
        assertEquals("yellow human", node.getAssetRecord().getDescription());
        assertNull(node.getAssetRecord().getPollerCategory());
    }

    /**
     * @see http://issues.opennms.org/browse/NMS-5191
     */
    @Test
    public void testImportLegacyAssetNameRequisition() throws Exception {
        final int nextNodeId = m_nodeDao.getNextNodeId();

        assertEquals(0, m_nodeDao.countAll());

        final Resource resource = getResource("classpath:/import_legacyAssetFieldName.xml");

        m_anticipator.anticipateEvent(getStarted(resource));
        m_anticipator.anticipateEvent(getSuccessful(resource));
        m_anticipator.anticipateEvent(getNodeAdded(nextNodeId));
        m_anticipator.anticipateEvent(getNodeGainedInterface(nextNodeId));
        m_anticipator.anticipateEvent(getNodeGainedService(nextNodeId));
        m_anticipator.anticipateEvent(getNodeScanCompleted(nextNodeId));

        // This requisition has an asset called "maintContractNumber" which was changed in
        // OpenNMS 1.10. We want to preserve backwards compatibility so make sure that the
        // field still works.
        m_provisioner.doImport(resource.getURL().toString(), Boolean.TRUE.toString());
        waitForEverything();
        m_anticipator.verifyAnticipated();

        // should still import the node, just skip the asset field
        assertEquals(1, m_nodeDao.countAll());
        OnmsNode node = m_nodeDao.get(m_nodeDao.getNodeIds().iterator().next());
        assertEquals("yellow human", node.getAssetRecord().getDescription());
        assertEquals("123456", node.getAssetRecord().getMaintcontract());
    }

    @Test
    public void testImportInvalidXml() throws Exception {
        assertEquals(0, m_nodeDao.countAll());

        final Resource invalidRequisitionResource = getResource("classpath:/import_invalidRequisition.xml");

        m_anticipator.anticipateEvent(getStarted(invalidRequisitionResource));
        m_anticipator.anticipateEvent(getFailed(invalidRequisitionResource));

        // This requisition has a "foreign-source" on the node tag, which is invalid,
        // foreign-source only belongs on the top-level model-import tag.
        m_provisioner.doImport(invalidRequisitionResource.getURL().toString(), Boolean.TRUE.toString());
        waitForEverything();
        m_anticipator.verifyAnticipated();

        // should fail to import the node, it should bomb if the requisition is unparseable
        assertEquals(0, m_nodeDao.countAll());
        
    }

    private Event getStarted(final Resource resource) {
        return new EventBuilder( EventConstants.IMPORT_STARTED_UEI, "Provisiond" )
        .addParam( EventConstants.PARM_IMPORT_RESOURCE, resource.toString() )
        .getEvent();
    }

    private Event getSuccessful(final Resource resource) {
        return new EventBuilder( EventConstants.IMPORT_SUCCESSFUL_UEI, "Provisiond" )
        .addParam( EventConstants.PARM_IMPORT_RESOURCE, resource.toString() )
        .getEvent();
    }

    private Event getFailed(final Resource resource) {
        return new EventBuilder( EventConstants.IMPORT_FAILED_UEI, "Provisiond" )
        .addParam( EventConstants.PARM_IMPORT_RESOURCE, resource.toString() )
        .getEvent();
    }
    
    private Event getNodeAdded(final int nodeId) {
        return new EventBuilder( EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond" )
        .setNodeid(nodeId).getEvent();
    }

    private Event getNodeGainedInterface(final int nodeId) {
        return new EventBuilder( EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond" )
        .setNodeid(nodeId).setInterface(InetAddressUtils.addr("10.0.0.1")).getEvent();
    }

    private Event getNodeGainedService(final int nodeId) {
        return new EventBuilder( EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond" )
        .setNodeid(nodeId).setInterface(InetAddressUtils.addr("10.0.0.1")).setService("ICMP").getEvent();
    }

    private Event getNodeScanCompleted(final int nodeId) {
        return new EventBuilder( EventConstants.PROVISION_SCAN_COMPLETE_UEI, "Provisiond" )
        .setNodeid(nodeId).getEvent();
    }

    protected Resource getResource(final String location) {
        return m_resourceLoader.getResource(location);
    }
}
