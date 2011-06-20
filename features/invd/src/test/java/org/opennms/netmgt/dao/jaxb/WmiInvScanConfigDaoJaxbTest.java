package org.opennms.netmgt.dao.jaxb;

import java.io.InputStream;

import junit.framework.TestCase;

import org.opennms.netmgt.config.invd.wmi.WmiInventory;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

public class WmiInvScanConfigDaoJaxbTest extends TestCase {
	public void testAfterPropertiesSetWithNoConfigSet() {
		WmiInvScanConfigDaoJaxb dao = new WmiInvScanConfigDaoJaxb();
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("property configResource must be set and be non-null"));
        
        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testAfterPropertiesSetWithBogusFileResource() throws Exception {
        Resource resource = new FileSystemResource("/bogus-file");
        WmiInvScanConfigDaoJaxb dao = new WmiInvScanConfigDaoJaxb();
        dao.setConfigResource(resource);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new MarshallingResourceFailureException(ThrowableAnticipator.IGNORE_MESSAGE));
        
        try {
            dao.afterPropertiesSet();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testAfterPropertiesSetWithGoodConfigFile() throws Exception {
    	WmiInvScanConfigDaoJaxb dao = new WmiInvScanConfigDaoJaxb();
        
        InputStream in = ConfigurationTestUtils.getInputStreamForConfigFile("wmi-invscan-config.xml");
        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();
        
        assertNotNull("WMI Inv Scan configuration should not be null", dao.getConfig());
    }
    
    public void testGetWmiInterfaceByName() throws Exception {
    	WmiInvScanConfigDaoJaxb dao = new WmiInvScanConfigDaoJaxb();
        
        InputStream in = ConfigurationTestUtils.getInputStreamForConfigFile("wmi-invscan-config.xml");
        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();
        
        WmiInventory inv = dao.getWmiInventoryByName("default");
        assertNotNull("The WMI Inventory collection 'default' should exist.", inv);
    }
}


