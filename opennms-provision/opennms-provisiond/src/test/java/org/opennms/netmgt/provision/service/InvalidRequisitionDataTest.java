package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.eventd.mock.EventAnticipator;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
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
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-provisiond.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath*:/META-INF/opennms/detectors.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/importerServiceTest.xml"
})
/* This test is for bug 3778 */
@JUnitTemporaryDatabase
@JUnitConfigurationEnvironment
@DirtiesContext
public class InvalidRequisitionDataTest implements InitializingBean {
    
    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private EventDao m_eventDao;
    
    @Autowired
    private Provisioner m_provisioner;
    
    @Autowired
    private ResourceLoader m_resourceLoader;
    
    @Autowired
    @Qualifier("mock")
    private MockEventIpcManager m_eventManager;

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
    }

    @After
    public void tearDown() throws Exception {
        m_anticipator.verifyAnticipated();
    }
    
    @Test
    @JUnitTemporaryDatabase
    public void testImportInvalidAsset() throws Exception {
        assertEquals(0, m_nodeDao.countAll());

        final Resource invalidAssetFieldResource = getResource("classpath:/import_invalidAssetFieldName.xml");

        m_anticipator.anticipateEvent(getStarted(invalidAssetFieldResource));
        m_anticipator.anticipateEvent(getSuccessful(invalidAssetFieldResource));
        m_anticipator.anticipateEvent(getNodeAdded());
        m_anticipator.anticipateEvent(getNodeGainedInterface());
        m_anticipator.anticipateEvent(getNodeGainedService());

        // This requisition has an asset on some nodes called "pollercategory".
        // Change it to "pollerCategory" (capital 'C') and the test passes...
        m_provisioner.doImport(invalidAssetFieldResource.getURL().toString(), true);

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
    @JUnitTemporaryDatabase
    public void testImportLegacyAssetNameRequisition() throws Exception {
        assertEquals(0, m_nodeDao.countAll());

        final Resource resource = getResource("classpath:/import_legacyAssetFieldName.xml");

        m_anticipator.anticipateEvent(getStarted(resource));
        m_anticipator.anticipateEvent(getSuccessful(resource));
        m_anticipator.anticipateEvent(getNodeAdded());
        m_anticipator.anticipateEvent(getNodeGainedInterface());
        m_anticipator.anticipateEvent(getNodeGainedService());

        // This requisition has an asset called "maintContractNumber" which was changed in
        // OpenNMS 1.10. We want to preserve backwards compatibility so make sure that the
        // field still works.
        m_provisioner.doImport(resource.getURL().toString(), true);

        // should still import the node, just skip the asset field
        assertEquals(1, m_nodeDao.countAll());
        OnmsNode node = m_nodeDao.get(m_nodeDao.getNodeIds().iterator().next());
        assertEquals("yellow human", node.getAssetRecord().getDescription());
        assertEquals("123456", node.getAssetRecord().getMaintcontract());
    }

    @Test
    @JUnitTemporaryDatabase
    public void testImportInvalidXml() throws Exception {
        assertEquals(0, m_nodeDao.countAll());

        final Resource invalidRequisitionResource = getResource("classpath:/import_invalidRequisition.xml");

        m_anticipator.anticipateEvent(getStarted(invalidRequisitionResource));
        m_anticipator.anticipateEvent(getFailed(invalidRequisitionResource));

        // This requisition has a "foreign-source" on the node tag, which is invalid,
        // foreign-source only belongs on the top-level model-import tag.
        m_provisioner.doImport(invalidRequisitionResource.getURL().toString(), true);

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
    
    private Event getNodeAdded() {
        return new EventBuilder( EventConstants.NODE_ADDED_EVENT_UEI, "Provisiond" )
        .setNodeid(1).getEvent();
    }

    private Event getNodeGainedInterface() {
        return new EventBuilder( EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI, "Provisiond" )
        .setNodeid(1).setInterface(InetAddressUtils.addr("10.0.0.1")).getEvent();
    }

    private Event getNodeGainedService() {
        return new EventBuilder( EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "Provisiond" )
        .setNodeid(1).setInterface(InetAddressUtils.addr("10.0.0.1")).setService("ICMP").getEvent();
    }

    protected Resource getResource(final String location) {
        return m_resourceLoader.getResource(location);
    }
}
