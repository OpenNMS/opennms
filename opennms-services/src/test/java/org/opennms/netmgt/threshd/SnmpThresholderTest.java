package org.opennms.netmgt.threshd;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class SnmpThresholderTest extends TestCase {

    private SnmpThresholder m_snmpThresholder;
    private ThresholdNetworkInterface m_iface;
    private SnmpThresholdNetworkInterface m_thresholdInterface;
    private Map<String, String> m_params;
    private DefaultThresholdsDao m_thresholdsDao;
        
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        int nodeId = 1;
        String ipAddress = "192.168.1.1";
        
        setUpThresholdingConfig();
        m_thresholdsDao = new DefaultThresholdsDao();
        m_thresholdsDao.setThresholdingConfigFactory(ThresholdingConfigFactory.getInstance());
        m_thresholdsDao.afterPropertiesSet();
        
        m_snmpThresholder = new SnmpThresholder();
        m_snmpThresholder.setThresholdsDao(m_thresholdsDao);
        
        m_iface = new ThresholderTestCase.ThresholdNetworkInterfaceImpl(nodeId, InetAddress.getByName(ipAddress));
        m_params = new HashMap<String, String>();
        m_params.put("thresholding-group", "default-snmp");
//      m_thresholdInterface = new SnmpThresholdInterface(m_iface, nodeId, null, 'N');
        m_thresholdInterface = new SnmpThresholdNetworkInterface(m_thresholdsDao, m_iface, m_params);
    }
    
//  private void setUpThresholdingConfig(String dirName, String fileName, String ipAddress, String serviceName, String groupName) throws Exception {
    private void setUpThresholdingConfig() throws Exception {
//        File dir = new File(dirName);

        Resource config = new ClassPathResource("/test-thresholds.xml");
        Reader r = new InputStreamReader(config.getInputStream());
        ThresholdingConfigFactory.setInstance(new ThresholdingConfigFactory(r));
        r.close();
//        ThresholdingConfigFactory.getInstance().getGroup("default-snmp").setRrdRepository("foo!");
//      ThresholdingConfigFactory.getInstance().getGroup(groupName).setRrdRepository(dir.getParentFile().getAbsolutePath());
    }

    public void testCheckNodeDirNullDirectory() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("directory argument cannot be null"));
        
        try {
            m_snmpThresholder.checkNodeDir(null, m_thresholdInterface, new Date(), new Events());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testCheckNodeDirNullThresholdNetworkInterface() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("thresholdNetworkInterface argument cannot be null"));
        
        try {
            m_snmpThresholder.checkNodeDir(new File(""), null, new Date(), new Events());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    // FIXME: This doesn't work now that config has been moved into SnmpThresholdNetworkInterface 
    public void FIXMEtestCheckNodeDirNullThresholdConfiguration() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("thresholdConfiguration argument cannot be null"));
        
        try {
            m_snmpThresholder.checkNodeDir(new File(""), null, new Date(), new Events());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testCheckNodeDirNullDate() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("date argument cannot be null"));
        
        try {
            m_snmpThresholder.checkNodeDir(new File(""), m_thresholdInterface, null, new Events());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testCheckNodeDirNullEvents() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("events argument cannot be null"));
        
        try {
            m_snmpThresholder.checkNodeDir(new File(""), m_thresholdInterface, new Date(), null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    // FIXME: This doesn't work because the nodeId underneath is now an int, not an Integer
    public void FIXMEtestCheckNodeDirNullSnmpIfaceNodeId() throws Exception {
        ThresholdNetworkInterface intf = new ThresholderTestCase.ThresholdNetworkInterfaceImpl(0, InetAddress.getByName("192.168.1.1"));
        SnmpThresholdNetworkInterface snmpIface = new SnmpThresholdNetworkInterface(m_thresholdsDao, intf, m_params);
        //snmpIface.setNodeId(null);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("getNodeId() of snmpIface argument cannot be null"));
        
        try {
            m_snmpThresholder.checkNodeDir(new File(""), snmpIface, new Date(), new Events());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    // FIXME: This doesn't work because of an NPE in SnmpThresholdInterface.getIpAddress() 
    public void FIXMEtestCheckNodeDirNullSnmpIfaceInetAddress() {
//        IPv4NetworkInterface intf = new IPv4NetworkInterface(null);
        ThresholdNetworkInterface intf = new ThresholderTestCase.ThresholdNetworkInterfaceImpl(1, null);
        SnmpThresholdNetworkInterface snmpIface = new SnmpThresholdNetworkInterface(m_thresholdsDao, intf, m_params);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("events argument cannot be null"));
        
        try {
            m_snmpThresholder.checkNodeDir(new File(""), snmpIface, new Date(), new Events());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    public void testCheckNodeDirNullFoo() {
        /*
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("directory argument cannot be null"));
        
        try {
        */
                m_snmpThresholder.checkNodeDir(new File(""), m_thresholdInterface, new Date(), new Events());
            /*
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        */
    }
    
    
    public void testStripRrdExtensionWithValidExtension() throws Exception {
        setUpRrdStrategy();
        String strippedName = m_snmpThresholder.stripRrdExtension("foo" + RrdUtils.getExtension()); 
        assertNotNull("stripped file name should not be null", strippedName);
        assertEquals("stripped file name", "foo", strippedName);
    }
    
    public void testStripRrdExtensionWithNoExtension() throws Exception {
        setUpRrdStrategy();
        String strippedName = m_snmpThresholder.stripRrdExtension("foo");
        assertNull("stripped file name should be null, but was: " + strippedName, strippedName);
    }
    
    public void testStripRrdExtensionWithValidExtensionTwice() throws Exception {
        setUpRrdStrategy();
        String strippedName = m_snmpThresholder.stripRrdExtension("foo" + RrdUtils.getExtension() + RrdUtils.getExtension()); 
        assertNotNull("stripped file name should not be null", strippedName);
        assertEquals("stripped file name", "foo" + RrdUtils.getExtension(), strippedName);
    }
    
    public void testStripRrdExtensionWithValidExtensionNotAtEnd() throws Exception {
        setUpRrdStrategy();
        String strippedName = m_snmpThresholder.stripRrdExtension("foo" + RrdUtils.getExtension() + ".bar"); 
        assertNull("stripped file name should be null, but was: " + strippedName, strippedName);
    }


    private void setUpRrdStrategy() throws RrdException {
        RrdConfig.setProperties(new Properties());
        RrdUtils.initialize();
    }

}
