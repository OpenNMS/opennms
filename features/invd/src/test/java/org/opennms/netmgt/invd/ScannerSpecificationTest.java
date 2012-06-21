package org.opennms.netmgt.invd;

import java.util.Map;

import org.junit.Assert;
import org.opennms.netmgt.config.invd.InvdPackage;
import org.opennms.netmgt.config.invd.InvdService;
import org.opennms.netmgt.config.invd.InvdServiceParameter;
import org.opennms.netmgt.invd.scanners.InventoryScannerStub;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.core.test.MockLogAppender;

import junit.framework.TestCase;


public class ScannerSpecificationTest extends TestCase {
	EasyMockUtils m_easyMockUtils = new EasyMockUtils();
	private InvdPackage m_invdPackage;
	private InventoryScanner m_invScanner;
//	private ScanningClient m_client;
//	private PollOutagesConfigFactory m_pollOutageFactory;
	
	@Override
	public void setUp() throws Exception {
		MockLogAppender.setupLogging();
		// Create dummy Inventory Package object.
		m_invdPackage = new InvdPackage();
        m_invdPackage.setName("pkg");
        m_invdPackage.setFilter("IPADDR IPLIKE *.*.*.*");
        m_invdPackage.addOutageCalendar("junit_test");
        
        InvdService svc = new InvdService();        
        svc.setName("FAKE");
        svc.setUserDefined(false);
        svc.setInterval(300000);
        m_invdPackage.addService(svc);
        
        InvdServiceParameter parm = new InvdServiceParameter();
        parm.setKey("collection");
        parm.setValue("default");
        svc.addServiceParameter(parm);
        svc.setStatus("on");
        
        // Set up a poll outages config.
//        m_pollOutageFactory = m_easyMockUtils.createMock(PollOutagesConfigFactory.class);        
//        PollOutagesConfigFactory.setInstance(m_pollOutageFactory);
        
        // Create an inventory scanner stub object.
        m_invScanner = new InventoryScannerStub();
        
        // Create a Mock object for the ScanningClient
        //m_client = m_easyMockUtils.createMock(ScanningClient.class);
	}

	@SuppressWarnings("unused")
	public void testCreate() {
		ScannerSpecification scanSpec = new ScannerSpecification(m_invdPackage, "FAKE", m_invScanner);
	}
	
	public void testGetReadOnlyPropertyMap() {
		ScannerSpecification scanSpec = new ScannerSpecification(m_invdPackage, "FAKE", m_invScanner);
		
		Map<String, String> params = scanSpec.getReadOnlyPropertyMap();
		
		Assert.assertFalse("The map should not be empty.", params.isEmpty());
		Assert.assertTrue("Ensure it has the \"collection\" key.", params.containsKey("collection"));
		Assert.assertEquals("Ensure that the \"collection\" key's value is \"default\".", "default", params.get("collection"));		
	}
	
//	public void testCheckScheduledOutages() {
//		expect(m_pollOutageFactory.isCurTimeInOutage("junit_test")).andReturn(true);
//		expect(m_client.getNodeId()).andReturn(1);
//		expect(m_pollOutageFactory.isNodeIdInOutage(1, "junit_test")).andReturn(true);
//		expect(m_client.getHostAddress()).andReturn("192.168.1.1");
//		expect(m_pollOutageFactory.isInterfaceInOutage("192.168.1.1", "junit_test")).andReturn(false);
//		m_easyMockUtils.replayAll();
//		
//		ScannerSpecification scanSpec = new ScannerSpecification(m_invdPackage, "FAKE", m_invScanner);
//		scanSpec.scheduledOutage(m_client);
//	}
}
