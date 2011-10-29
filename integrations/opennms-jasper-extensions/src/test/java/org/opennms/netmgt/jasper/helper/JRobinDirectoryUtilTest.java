package org.opennms.netmgt.jasper.helper;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JRobinDirectoryUtilTest {
    private static String RRD_DIRECTORY = "src/test/resources/share/rrd/snmp";
    private static String NODE_ID = "9";
    private static String INTERFACE = "me1-0002baaacffe";
    
    @Before
    public void setup() {
        System.setProperty("org.opennms.rrd.storeByGroup", "true");
    }
    
    
    @Test
    @Ignore
    public void testJRobinDirectoryLookupLocal() throws FileNotFoundException, IOException {
        JRobinDirectoryUtil lookup = new JRobinDirectoryUtil();
        
        String rrdDirectory = "/Users/thedesloge/git/opennms/target/opennms-1.9.93-SNAPSHOT/share/rrd/snmp";
        String nodeId = "48";
        String iFace = lookup.getInterfaceDirectory("", "mgi1", "90840dd40a7d");
        String jrb = lookup.getIfInOctetsJrb(rrdDirectory, nodeId, iFace);
        System.out.println("path and file: " + jrb);
        
        assertEquals("/Users/thedesloge/git/opennms/target/opennms-1.9.93-SNAPSHOT/share/rrd/snmp/48/mgi1-90840dd40a7d/mib2-interfaces.jrb", lookup.getIfInOctetsJrb(rrdDirectory, nodeId, iFace));
        assertEquals("/Users/thedesloge/git/opennms/target/opennms-1.9.93-SNAPSHOT/share/rrd/snmp/48/mgi1-90840dd40a7d/mib2-interfaces.jrb", lookup.getIfInOctetsJrb(rrdDirectory, nodeId, iFace));
        
        assertEquals("ifInOctets", lookup.getIfInOctetsDataSource(rrdDirectory, nodeId, iFace));
        assertEquals("ifOutOctets", lookup.getIfOutOctetsDataSource(rrdDirectory, nodeId, iFace));
    }
    
    @Test
    public void testJRobinDirectoryLookup() throws IOException {
        JRobinDirectoryUtil lookup = new JRobinDirectoryUtil();
        
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/mib2-interfaces.jrb", lookup.getIfInOctetsJrb(RRD_DIRECTORY, NODE_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/mib2-interfaces.jrb", lookup.getIfOutOctetsJrb( RRD_DIRECTORY, NODE_ID, INTERFACE));
        
        System.setProperty("org.opennms.rrd.storeByGroup", "false");
        
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/ifInOctets.jrb", lookup.getIfInOctetsJrb(RRD_DIRECTORY, NODE_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/ifOutOctets.jrb", lookup.getIfOutOctetsJrb(RRD_DIRECTORY, NODE_ID, INTERFACE));
    }
    
    @Test
    public void testGetInterfaceDirectory() {
        JRobinDirectoryUtil lookup = new JRobinDirectoryUtil();
        
        String snmpphysaddr = "0002baaacffe";
        String snmpifname = "me1";
        String snmpifdescr = "me1";
        assertEquals("me1-0002baaacffe", lookup.getInterfaceDirectory(snmpifname, snmpifdescr, snmpphysaddr));
    }
    
    @Test
    public void testGetInterfaceDirectoryNoSnmpPhysAddr() {
        JRobinDirectoryUtil lookup = new JRobinDirectoryUtil();
        
        String snmpphysaddr = null;
        String snmpifname = "me1";
        String snmpifdescr = "me1";
        assertEquals("me1", lookup.getInterfaceDirectory(snmpifname, snmpifdescr, snmpphysaddr));
        
        snmpifdescr = null;
        assertEquals("me1", lookup.getInterfaceDirectory(snmpifname, snmpifdescr, snmpphysaddr));
    }
    
    @Test
    public void testGetInterfaceDirectoryNoSnmpIfName() {
        JRobinDirectoryUtil lookup = new JRobinDirectoryUtil();
        
        String snmpphysaddr = "0002baaacffe";
        String snmpifname = null;
        String snmpifdescr = "me1";
        assertEquals("me1-0002baaacffe", lookup.getInterfaceDirectory(snmpifname, snmpifdescr, snmpphysaddr));
    }
    
    @Test
    public void testGetInterfaceDirectoryATM() {
        JRobinDirectoryUtil lookup = new JRobinDirectoryUtil();
        
        String snmpphysaddr = "00e0817xxxxx";
        String snmpifname = "";
        String snmpifdescr = "eth0";
        assertEquals("eth0-00e0817xxxxx", lookup.getInterfaceDirectory(snmpifname, snmpifdescr, snmpphysaddr));
    }
    
}
