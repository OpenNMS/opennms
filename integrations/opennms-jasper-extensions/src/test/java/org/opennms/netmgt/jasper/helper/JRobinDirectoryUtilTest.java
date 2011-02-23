package org.opennms.netmgt.jasper.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class JRobinDirectoryUtilTest {
    private static String RRD_DIRECTORY = "src/test/resources/share/rrd";
    private static String NODE_ID = "9";
    private static String INTERFACE = "me1-0002baaacffe";
    
    @Before
    public void setup() {
        System.setProperty("org.opennms.rrd.storeByGroup", "true");
    }
    
    @Test
    public void testJRobinDirectoryLookup() throws IOException {
        JRobinDirectoryUtil lookup = new JRobinDirectoryUtil();
        
        assertEquals("src/test/resources/share/rrd/9/me1-0002baaacffe/mib2-interfaces.jrb", lookup.getIfInOctetsJrb(RRD_DIRECTORY, NODE_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/9/me1-0002baaacffe/mib2-interfaces.jrb", lookup.getIfOutOctetsJrb( RRD_DIRECTORY, NODE_ID, INTERFACE));
        
        System.setProperty("org.opennms.rrd.storeByGroup", "false");
        
        assertEquals("src/test/resources/share/rrd/9/me1-0002baaacffe/ifHCInOctets.jrb", lookup.getIfInOctetsJrb(RRD_DIRECTORY, NODE_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/9/me1-0002baaacffe/ifHCOutOctets.jrb", lookup.getIfOutOctetsJrb(RRD_DIRECTORY, NODE_ID, INTERFACE));
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
    public void testGetInterfaceDirectoryNoSnmpIfName() {
        JRobinDirectoryUtil lookup = new JRobinDirectoryUtil();
        
        String snmpphysaddr = "0002baaacffe";
        String snmpifname = null;
        String snmpifdescr = "me1";
        assertEquals("me1-0002baaacffe", lookup.getInterfaceDirectory(snmpifname, snmpifdescr, snmpphysaddr));
    }
    
    @Test
    public void testGetInterfaceDirectoryLocalSystem() throws FileNotFoundException, IOException {
        JRobinDirectoryUtil lookup = new JRobinDirectoryUtil();
        
        String rrdDir = "/Users/thedesloge/git/opennms/target/opennms-1.8.10-SNAPSHOT/share/rrd/snmp/";
        String nodeid = "11";
        String iFace = "eth0-00163e13f215";
        
        System.out.println(lookup.getIfInOctetsJrb(rrdDir, nodeid, iFace));
        File f = new File(lookup.getIfInOctetsJrb(rrdDir, nodeid, iFace));
        if(f.exists()) {
            assertTrue(true);
        }else {
            assertTrue(false);
        }
        
    }
}
