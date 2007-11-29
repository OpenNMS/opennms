package org.opennms.netmgt.mock;

import java.io.File;

public class OpenNMSIntegrationTestCaseTest extends OpenNMSIntegrationTestCase {
    
    
    @Override
    protected String[] getConfigLocations() {
        return new String[] { 
                "classpath:META-INF/opennms/applicationContext-dao.xml",
                "classpath:META-INF/opennms/applicationContext-daemon.xml" 
        };
    }

    public void testHomeDirCreated() {

        String homePath = System.getProperty("opennms.home");
        assertNotNull(homePath);
        
        assertTrue(new File(homePath).exists());
        
    }
    
    public void testEtcDirExists() {
        
        String homePath = System.getProperty("opennms.home");
        assertNotNull(homePath);
        
        File homeDir = new File(homePath);
        File etcDir = new File(homeDir, "etc");
        
        assertTrue(etcDir.exists());
    }



}
