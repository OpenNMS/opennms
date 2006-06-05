package org.opennms.netmgt.threshd;

import java.io.FileReader;
import java.util.Properties;

import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigManager;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockLogAppender;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.rrd.RrdConfig;
import org.opennms.netmgt.threshd.mock.MockThreshdConfigManager;

public class ThreshdTest extends ThresholderTestCase {
    
    public static final String THRESHD_CONFIG = "<?xml version=\"1.0\"?>\n" + 
            "<?castor class-name=\"org.opennms.netmgt.threshd.ThreshdConfiguration\"?>\n" + 
            "<threshd-configuration \n" + 
            "   threads=\"5\">\n" + 
            "   \n" + 
            "   <package name=\"example1\">\n" + 
            "       <filter>IPADDR IPLIKE *.*.*.*</filter>   \n" + 
            "       <specific>0.0.0.0</specific>\n" + 
//            "       <include-range begin=\"192.168.1.1\" end=\"192.168.1.254\"/>\n" + 
            "       \n" + 
            "       <service name=\"SNMP\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "           <parameter key=\"thresholding-group\" value=\"default-snmp\"/>\n" + 
            "       </service>\n" + 
            "       \n" + 
            "        <service name=\"ICMP\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "                        <parameter key=\"thresholding-group\" value=\"icmp-latency\"/>\n" + 
            "                </service>\n" + 
            "\n" + 
            "                <service name=\"HTTP\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "                        <parameter key=\"thresholding-group\" value=\"http-latency\"/>\n" + 
            "                </service>\n" + 
            "\n" + 
            "                <service name=\"HTTP-8000\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "                        <parameter key=\"thresholding-group\" value=\"http-8000-latency\"/>\n" + 
            "                </service>\n" + 
            "\n" + 
            "                <service name=\"HTTP-8080\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "                        <parameter key=\"thresholding-group\" value=\"http-8080-latency\"/>\n" + 
            "                </service>\n" + 
            "\n" + 
            "                <service name=\"DNS\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "                        <parameter key=\"thresholding-group\" value=\"dns-latency\"/>\n" + 
            "                </service>\n" + 
            "\n" + 
            "                <service name=\"DHCP\" interval=\"3000\" user-defined=\"false\" status=\"on\">\n" + 
            "                        <parameter key=\"thresholding-group\" value=\"dhcp-latency\"/>\n" + 
            "                </service>\n" + 
            "\n" + 
            "       <outage-calendar>zzz from poll-outages.xml zzz</outage-calendar>\n" + 
            "   </package>\n" + 
            "   \n" + 
            "   <thresholder service=\"SNMP\"   class-name=\"org.opennms.netmgt.threshd.SnmpThresholder\"/>\n" + 
            "   <thresholder service=\"ICMP\"     class-name=\"org.opennms.netmgt.threshd.LatencyThresholder\"/>\n" + 
            "        <thresholder service=\"HTTP\"     class-name=\"org.opennms.netmgt.threshd.LatencyThresholder\"/>\n" + 
            "        <thresholder service=\"HTTP-8000\"        class-name=\"org.opennms.netmgt.threshd.LatencyThresholder\"/>\n" + 
            "        <thresholder service=\"HTTP-8080\"        class-name=\"org.opennms.netmgt.threshd.LatencyThresholder\"/>\n" + 
            "        <thresholder service=\"DNS\"      class-name=\"org.opennms.netmgt.threshd.LatencyThresholder\"/>\n" + 
            "        <thresholder service=\"DHCP\"     class-name=\"org.opennms.netmgt.threshd.LatencyThresholder\"/>\n" + 
            "</threshd-configuration>\n";
    private MockDatabase m_db;
    
    
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ThreshdTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        MockLogAppender.setupLogging();
        
		setupDatabase();
		
        FileReader rdr = new FileReader("etc/database-schema.xml");
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(rdr));
        rdr.close();
        
        Properties rrdProperties = new Properties();
        rrdProperties.put("org.opennms.rrd.strategyClass", "org.opennms.netmgt.mock.MockRrdStrategy");
        rrdProperties.put("org.opennms.rrd.usequeue", "false");
        RrdConfig.setProperties(rrdProperties);
        
        EventIpcManagerFactory.setIpcManager(new MockEventIpcManager());
		
        String dirName = "/tmp/192.168.1.1";
        String fileName = "icmp.rrd";
        String ipAddress = "192.168.1.1";
        String serviceName = "ICMP";
        String groupName = "icmp-latency";
		
		setupThresholdConfig(dirName, fileName, ipAddress, serviceName, groupName);
        
        FileReader pollOutagesRdr = new FileReader("etc/poll-outages.xml");
        PollOutagesConfigFactory.setInstance(new PollOutagesConfigFactory(pollOutagesRdr));
        pollOutagesRdr.close();
        
    }

    protected void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
    }

    public void testThreshd() throws Exception {
		
        Threshd threshd = new Threshd();
        ThreshdConfigManager config = new MockThreshdConfigManager(THRESHD_CONFIG, "localhost", false);
        threshd.setThreshdConfig(config);
        threshd.init();
        threshd.start();
        
        Thread.sleep(10000);
        
        threshd.stop();
    }
}
