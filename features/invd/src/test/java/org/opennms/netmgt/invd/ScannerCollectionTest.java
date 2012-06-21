package org.opennms.netmgt.invd;

import static org.easymock.EasyMock.expect;

import java.util.Collections;

import org.junit.Assert;
import org.opennms.netmgt.config.invd.InvdPackage;
import org.opennms.netmgt.config.invd.InvdScanner;
import org.opennms.netmgt.config.invd.InvdService;
import org.opennms.netmgt.config.invd.InvdServiceParameter;
import org.opennms.netmgt.dao.InvdConfigDao;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.core.test.MockLogAppender;

import junit.framework.TestCase;


public class ScannerCollectionTest extends TestCase{
	EasyMockUtils m_easyMockUtils = new EasyMockUtils();
	private ScannerCollection m_scannerCollection;
	private InvdPackage m_invdPackage;
	private InvdConfigDao m_invdConfigDao;
	private InvdScanner m_invScanner;
	
	@Override
	public void setUp() {
		MockLogAppender.setupLogging();
		
		// Create Scanner Collection object.
		m_scannerCollection = new ScannerCollection();
		
		// Create Invd Config Dao Mock object.
		m_invdConfigDao = m_easyMockUtils.createMock(InvdConfigDao.class);
		
		// Assign the mock Invd Config Dao to the Scanner Collection.
		m_scannerCollection.setInvdConfigDao(m_invdConfigDao);
		
		// Create dummy Inventory Package object.
		m_invdPackage = new InvdPackage();
        m_invdPackage.setName("pkg");
        m_invdPackage.setFilter("IPADDR IPLIKE *.*.*.*");
        InvdService svc = new InvdService();
        m_invdPackage.addService(svc);
        svc.setName("WMI");
        svc.setUserDefined(false);
        svc.setInterval(300000);
        InvdServiceParameter parm = new InvdServiceParameter();
        parm.setKey("collection");
        parm.setValue("default");
        svc.addServiceParameter(parm);
        svc.setStatus("on");
        
        // Create a dummy scanner config.
        m_invScanner = new InvdScanner();
        m_invScanner.setClassName("org.opennms.netmgt.invd.scanners.InventoryScannerStub");
        m_invScanner.setService("FAKE");
	}
	
	public void testInstantiateScanners() {
		expect(m_invdConfigDao.getScanners()).andReturn(Collections.singleton(m_invScanner));
		m_easyMockUtils.replayAll();
		
		m_scannerCollection.instantiateScanners();
		
		InventoryScanner scanner = m_scannerCollection.getInventoryScanner("FAKE");
		
		Assert.assertNotNull("Ensure that we retrieved a scanner stub.", scanner);
	}
}
