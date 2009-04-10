/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 29, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.threshd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.easymock.EasyMock;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.test.FileAnticipator;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SnmpThresholderTest {

    @SuppressWarnings("deprecation")
    private SnmpThresholder m_snmpThresholder;
    private ThresholdNetworkInterface m_iface;
    private SnmpThresholdNetworkInterface m_thresholdInterface;
    private Map<String, String> m_params;
    private DefaultThresholdsDao m_thresholdsDao;
    private FileAnticipator m_fileAnticipator;
    private IfInfoGetter m_ifInfoGetter;
    
    private EasyMockUtils m_mocks = new EasyMockUtils();
        
    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception {

        m_fileAnticipator = new FileAnticipator();
        
        int nodeId = 1;
        String ipAddress = "192.168.1.1";
        
        setUpThresholdingConfig();
        m_thresholdsDao = new DefaultThresholdsDao();
        m_thresholdsDao.setThresholdingConfigFactory(ThresholdingConfigFactory.getInstance());
        m_thresholdsDao.afterPropertiesSet();
        
        m_snmpThresholder = new SnmpThresholder();
        m_snmpThresholder.setThresholdsDao(m_thresholdsDao);
        
        m_ifInfoGetter = m_mocks.createMock(IfInfoGetter.class);
        m_snmpThresholder.setIfInfoGetter(m_ifInfoGetter);
        
        m_iface = new ThresholderTestCase.ThresholdNetworkInterfaceImpl(nodeId, InetAddress.getByName(ipAddress));
        m_params = new HashMap<String, String>();
        m_params.put("thresholding-group", "default-snmp");
        m_thresholdInterface = new SnmpThresholdNetworkInterface(m_thresholdsDao, m_iface, m_params);
    }
    
    @After
    public void tearDown() {
        m_fileAnticipator.tearDown();
    }
    
    private void setUpThresholdingConfig() throws Exception {
        Resource config = new ClassPathResource("/test-thresholds.xml");
        Reader r = new InputStreamReader(config.getInputStream());
        ThresholdingConfigFactory.setInstance(new ThresholdingConfigFactory(r));
        r.close();
    }

    @SuppressWarnings("deprecation")
    @Test
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
    
    @SuppressWarnings("deprecation")
    @Test
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
    @SuppressWarnings("deprecation")
    @Test
    public void testCheckNodeDirNullThresholdConfiguration() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("thresholdNetworkInterface argument cannot be null"));
        
        try {
            m_snmpThresholder.checkNodeDir(new File(""), null, new Date(), new Events());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @SuppressWarnings("deprecation")
    @Test
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
    
    @SuppressWarnings("deprecation")
    @Test
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
    
    @SuppressWarnings("deprecation")
    @Test
    public void testCheckNodeDirNullSnmpIfaceInetAddress() {
        ThresholdNetworkInterface intf = new ThresholderTestCase.ThresholdNetworkInterfaceImpl(1, null);
        SnmpThresholdNetworkInterface snmpIface = new SnmpThresholdNetworkInterface(m_thresholdsDao, intf, m_params);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("getInetAddress() of thresholdNetworkInterface argument cannot be null"));
        
        try {
            m_snmpThresholder.checkNodeDir(new File(""), snmpIface, new Date(), new Events());
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testCheckNodeDirNullFoo() throws Exception {
        /*
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("directory argument cannot be null"));
        
        try {
        */
        setUpRrdStrategy();
           
                m_snmpThresholder.checkNodeDir(new File(""), m_thresholdInterface, new Date(), new Events());
            /*
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
        */
    }
    
    
    @SuppressWarnings("deprecation")
    @Test
    public void testStripRrdExtensionWithValidExtension() throws Exception {
        setUpRrdStrategy();
        String strippedName = m_snmpThresholder.stripRrdExtension("foo" + RrdUtils.getExtension()); 
        assertNotNull("stripped file name should not be null", strippedName);
        assertEquals("stripped file name", "foo", strippedName);
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testStripRrdExtensionWithNoExtension() throws Exception {
        setUpRrdStrategy();
        String strippedName = m_snmpThresholder.stripRrdExtension("foo");
        assertNull("stripped file name should be null, but was: " + strippedName, strippedName);
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testStripRrdExtensionWithValidExtensionTwice() throws Exception {
        setUpRrdStrategy();
        String strippedName = m_snmpThresholder.stripRrdExtension("foo" + RrdUtils.getExtension() + RrdUtils.getExtension()); 
        assertNotNull("stripped file name should not be null", strippedName);
        assertEquals("stripped file name", "foo" + RrdUtils.getExtension(), strippedName);
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testStripRrdExtensionWithValidExtensionNotAtEnd() throws Exception {
        setUpRrdStrategy();
        String strippedName = m_snmpThresholder.stripRrdExtension("foo" + RrdUtils.getExtension() + ".bar"); 
        assertNull("stripped file name should be null, but was: " + strippedName, strippedName);
    }

    private void setUpRrdStrategy() throws RrdException {
        RrdConfig.setProperties(new Properties());
        RrdUtils.initialize();
    }

    @Test
    public void testThresholdFilters() throws Exception {
        System.err.println("--------------------------------------------------------");
        ThresholdGroup group = m_thresholdsDao.get("generic-snmp");
        m_thresholdInterface.getThresholdConfiguration().setThresholdGroup(group);
        Collection<Basethresholddef> thresholds = m_thresholdsDao.getThresholdingConfigFactory().getThresholds("generic-snmp");
        int count = 0;
        for (Basethresholddef threshold: thresholds) {
            count += threshold.getResourceFilterCount();
        }
        assertEquals(5, count); // Count how many resource-filter entries are defined on test-thresholods.xml
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testInterfaces() throws Exception {
        System.err.println("--------------------------------------------------------");
        setUpRrdStrategy();
        // Set storeByGroup, because JRBs will be created with this feature
        System.setProperty("org.opennms.rrd.storeByGroup", "true");

        // Get ThresholdGroup and validate data
        ThresholdGroup group = m_thresholdsDao.get("generic-snmp");

        // Common Variables
        File path = m_fileAnticipator.getTempDir();
        File nodeDir = m_fileAnticipator.tempDir(path, "1");
        long start = System.currentTimeMillis();        
        List<String> sources = new ArrayList<String>();
        sources.add("ifInOctets");
        sources.add("ifOutOctets");
        sources.add("ifInPackets");
        sources.add("ifOutPackets");

        // Create JRB File for Resource 1
        File intf1Dir = m_fileAnticipator.tempDir(nodeDir, "eth0");
        File rrd1 = m_fileAnticipator.tempFile(intf1Dir, "mib2-stats.jrb");
        createDsProperties(intf1Dir, sources, "mib2-stats");
        List<String> data1 = new ArrayList<String>();
        data1.add("100:200:300:350"); // TRIGGERED:TRIGGERED:NONE:NONE
        createAndUpdateRrd(rrd1, start, sources, data1);
                
        // Create Temporal Files for Resource 2
        File intf2Dir = m_fileAnticipator.tempDir(nodeDir, "wlan0");
        File rrd2 = m_fileAnticipator.tempFile(intf2Dir, "mib2-stats.jrb");
        createDsProperties(intf2Dir, sources, "mib2-stats");
        List<String> data2 = new ArrayList<String>();
        data2.add("50:150:400:300"); // NO_CHANGE:TRIGGERED:NONE:NONE
        createAndUpdateRrd(rrd2, start, sources, data2);

        // Run Thresholds Check and Validate. It must generate 3 events
        m_thresholdInterface.getThresholdConfiguration().setThresholdGroup(group);
        Events events = new Events();
        
        // Creating Mock ifInfo Data for eth0
        Map<String,String> ifInfoEth0 = new HashMap<String,String>();
        ifInfoEth0.put("snmpifindex", "1");
        ifInfoEth0.put("snmpifdesc", "eth0");
        ifInfoEth0.put("snmpifalias", "ethernet interface");
        // 3 times because there are 2 thresholds instances, one with two filters and one without filter (a total of 3) on test-thresholds.xml
        EasyMock.expect(m_ifInfoGetter.getIfInfoForNodeAndLabel(1, "eth0")).andReturn(ifInfoEth0).times(3);

        // Creating Mock ifInfo Data for wlan0
        Map<String,String> ifInfoWlan0 = new HashMap<String,String>();
        ifInfoWlan0.put("snmpifindex", "2");
        ifInfoWlan0.put("snmpifdesc", "wlan0");
        ifInfoWlan0.put("snmpifalias", "wireless interface");
        // 3 times because there are 2 thresholds instances, one with two filters and one without filter (a total of 3) on test-thresholds.xml
        EasyMock.expect(m_ifInfoGetter.getIfInfoForNodeAndLabel(1, "wlan0")).andReturn(ifInfoWlan0).times(3);

        m_mocks.replayAll();
        m_snmpThresholder.checkIfDir(intf1Dir, m_thresholdInterface, new Date(start), events);
        m_snmpThresholder.checkIfDir(intf2Dir, m_thresholdInterface, new Date(start), events);
        m_mocks.verifyAll();
        
        //assertEquals(3, events.getEventCount()); // with no Filters. See test-thresholds.xml
        assertEquals(2, events.getEventCount()); // with Filters Enabled. See test-thresholds.xml
    }
  
    @SuppressWarnings("deprecation")
    @Test
    public void testThresholdWithGenericResourceTypes() throws Exception {
        System.err.println("--------------------------------------------------------");
        setUpRrdStrategy();
        // Set storeByGroup, because JRBs will be created with this feature
        System.setProperty("org.opennms.rrd.storeByGroup", "true");

        // Get ThresholdGroup and validate data
        ThresholdGroup group = m_thresholdsDao.get("generic-snmp");
        assertEquals(2, group.getGenericResourceTypeMap().get("frCircuitIfIndex").getThresholdMap().size());

        // Common Variables
        File path = m_fileAnticipator.getTempDir();
        File nodeDir = m_fileAnticipator.tempDir(path, "1");
        File rtDir = m_fileAnticipator.tempDir(nodeDir, "frCircuitIfIndex");
        Properties strings = new Properties();
        long start = System.currentTimeMillis();
        List<String> sources = new ArrayList<String>();
        sources.add("frSentFrames");
        sources.add("frSentOctets");
        sources.add("frReceivedFrames");
        sources.add("frReceivedOctets");

        // Create JRB File for Resource 1
        File r1Dir = m_fileAnticipator.tempDir(rtDir, "Se0.100");
        File rrd1 = m_fileAnticipator.tempFile(r1Dir, "rfc1315-frame-relay.jrb");
        createDsProperties(r1Dir, sources, "rfc1315-frame-relay");
                
        // Creating strings.properties for Resource 1
        strings.setProperty("frName", "caracas");
        strings.setProperty("frDlci", "100");
        strings.setProperty("frIntf", "0");
        File sFile1 = m_fileAnticipator.tempFile(r1Dir, "strings.properties");
        strings.store(new FileOutputStream(sFile1), null);
        
        // Creating JRB content for Resource 1
        List<String> data1 = new ArrayList<String>();
        data1.add("100:200:300:350"); // TRIGGERED:TRIGGERED:NONE:NONE
        createAndUpdateRrd(rrd1, start, sources, data1);
                
        // Create Temporal Files for Resource 2
        File r2Dir = m_fileAnticipator.tempDir(rtDir, "Se1.200");
        File rrd2 = m_fileAnticipator.tempFile(r2Dir, "rfc1315-frame-relay.jrb");
        createDsProperties(r2Dir, sources, "rfc1315-frame-relay");

        // Creating strings.properties for Resource 2
        strings.setProperty("frDlci", "200");
        strings.setProperty("frIntf", "1");
        File sFile2 = m_fileAnticipator.tempFile(r2Dir, "strings.properties");
        strings.store(new FileOutputStream(sFile2), null);
        
        // Creating JRB content for Resource 2        
        List<String> data2 = new ArrayList<String>();
        data2.add("50:150:400:300"); // RE_ARMED:NO_CHANGE:NONE:NONE
        createAndUpdateRrd(rrd2, start, sources, data2);

        // Run Thresholds Check and Validate. It must generate 3 events
        m_thresholdInterface.getThresholdConfiguration().setThresholdGroup(group);
        Events events = new Events();
        m_snmpThresholder.checkResourceDir(rtDir, m_thresholdInterface, new Date(start), events);
        //assertEquals(3, events.getEventCount()); // with no Filters. See test-thresholds.xml
        assertEquals(2, events.getEventCount()); // with Filters Enabled. See test-thresholds.xml
        // Validating ds-value for bug 2129
        for (Event e : events.getEvent()) {
        	assertEquals("label", e.getParms().getParm(6).getParmName());
        	assertEquals("caracas", e.getParms().getParm(6).getValue().getContent());
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testExpressionWithGenericResourceTypes() throws Exception {
        System.err.println("--------------------------------------------------------");
        // Set storeByGroup, because JRBs will be created with this feature
        System.setProperty("org.opennms.rrd.storeByGroup", "true");

        // Get ThresholdGroup and validate data
        ThresholdGroup group = m_thresholdsDao.get("generic-snmp");
        assertEquals(1, group.getGenericResourceTypeMap().get("hrStorageIndex").getThresholdMap().size());

        // Common Variables
        File path = m_fileAnticipator.getTempDir();
        File nodeDir = m_fileAnticipator.tempDir(path, "1");
        File rtDir = m_fileAnticipator.tempDir(nodeDir, "hrStorageIndex");
        Properties strings = new Properties();
        long start = System.currentTimeMillis();
        List<String> sources = new ArrayList<String>();
        sources.add("hrStorageAllocUnits");
        sources.add("hrStorageSize");
        sources.add("hrStorageUsed");

        // Create JRB File for Resource 1
        File r1Dir = m_fileAnticipator.tempDir(rtDir, "opt");
        File rrd1 = m_fileAnticipator.tempFile(r1Dir, "mib2-host-resources-storage.jrb");
        createDsProperties(r1Dir, sources, "mib2-host-resources-storage");
                
        // Creating strings.properties for Resource 1
        strings.setProperty("hrStorageDescr", "/opt");
        File sFile1 = m_fileAnticipator.tempFile(r1Dir, "strings.properties");
        strings.store(new FileOutputStream(sFile1), null);
        
        // Creating JRB content for Resource 1
        List<String> data1 = new ArrayList<String>();
        data1.add("2:200:80");
        createAndUpdateRrd(rrd1, start, sources, data1);

        // Run Thresholds Check and Validate. It must generate 3 events
        m_thresholdInterface.getThresholdConfiguration().setThresholdGroup(group);
        Events events = new Events();
        m_snmpThresholder.checkResourceDir(rtDir, m_thresholdInterface, new Date(start), events);
        // with no Filters. See test-thresholds.xml
        assertEquals(1, events.getEventCount());
        // Validating ds-value for bug 2129
        for (Event e : events.getEvent()) {
        	assertEquals("label", e.getParms().getParm(6).getParmName());
        	assertEquals("/opt", e.getParms().getParm(6).getValue().getContent());
        }
    }

    private void createDsProperties(File dir, List<String> sources, String group) throws Exception {
        Properties ds = new Properties();
        for (String source : sources) {
            ds.setProperty(source, group);
        }
        File dsFile = m_fileAnticipator.tempFile(dir, "ds.properties");
        ds.store(new FileOutputStream(dsFile), null);
    }
    
    private void createAndUpdateRrd(File rrdPath, long start, List<String> sources, List<String> values) throws Exception {
        // Creating RRD
        long ts = start/1000;
        RrdDef rrdDef = new RrdDef(rrdPath.getAbsolutePath(), ts - 300, 300);
        for (String source : sources) {
            rrdDef.addDatasource(source, "GAUGE", 600, 0, Double.NaN);
        }
        rrdDef.addArchive("AVERAGE", 0.5, 1, 600);
        System.err.println(rrdDef.dump());
        RrdDb rrdDb = new RrdDb(rrdDef);
        rrdDb.close();
        // Updating RRD
        rrdDb = new RrdDb(rrdPath.getAbsolutePath());
        Sample sample = rrdDb.createSample();
        for (String value : values) {
            sample.setAndUpdate(Long.toString(ts) + ":" + value);
            System.err.println(sample.dump());
            ts += 300;
        }
        rrdDb.close();
    }
}
