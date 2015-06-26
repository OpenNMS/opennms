/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jasper.helper;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JRobinDirectoryUtilTest {
    private static String JRB_DIRECTORY = "src/test/resources/share/rrd/snmp";
    private static String RRD_TOOL_DIRECTORY = "src/test/resources/share/rrd";
    private static String NODE_ID = "9";
    private static String FOREIGN_SOURCE = "Servers";
    private static String FOREIGN_ID = "S001";
    private static String INTERFACE = "me1-0002baaacffe";
    
    @Before
    public void setup() {
        System.clearProperty("org.opennms.rrd.fileExtension");
        System.setProperty("org.opennms.rrd.storeByGroup", "true");
        System.setProperty("org.opennms.rrd.storeByForeignSource", "false");
        System.setProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy");
    }
    
    
    @Test
    @Ignore
    public void testJRobinDirectoryLookupLocal() throws FileNotFoundException, IOException {
        JRobinDirectoryUtil lookup = new JRobinDirectoryUtil();
        
        String rrdDirectory = "/Users/thedesloge/git/opennms/target/opennms-1.9.93-SNAPSHOT/share/rrd/snmp";
        String nodeId = "48";
        String iFace = lookup.getInterfaceDirectory("", "mgi1", "90840dd40a7d");
        String jrb = lookup.getIfInOctetsJrb(rrdDirectory, nodeId, null, null, iFace);
        System.out.println("path and file: " + jrb);
        
        assertEquals("/Users/thedesloge/git/opennms/target/opennms-1.9.93-SNAPSHOT/share/rrd/snmp/48/mgi1-90840dd40a7d/mib2-interfaces.jrb", lookup.getIfInOctetsJrb(rrdDirectory, nodeId, null, null, iFace));
        assertEquals("/Users/thedesloge/git/opennms/target/opennms-1.9.93-SNAPSHOT/share/rrd/snmp/48/mgi1-90840dd40a7d/mib2-interfaces.jrb", lookup.getIfInOctetsJrb(rrdDirectory, nodeId, null, null, iFace));
        
        assertEquals("ifInOctets", lookup.getIfInOctetsDataSource(rrdDirectory, nodeId, null, null, iFace));
        assertEquals("ifOutOctets", lookup.getIfOutOctetsDataSource(rrdDirectory, nodeId, null, null, iFace));
    }
    
    @Test
    public void testJRobinDirectoryLookup() throws IOException {
        JRobinDirectoryUtil lookup = new JRobinDirectoryUtil();
        
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/mib2-interfaces.jrb", lookup.getIfInOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/mib2-interfaces.jrb", lookup.getIfOutOctetsJrb( JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        
        System.setProperty("org.opennms.rrd.storeByGroup", "false");
        
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/ifHCInOctets.jrb", lookup.getIfInOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/ifHCOutOctets.jrb", lookup.getIfOutOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
    }
    
    @Test
    public void testJRobinDirectoryUtilRrdExtension() throws FileNotFoundException, IOException {
        System.setProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.jrobin.JniRrdStrategy");
        JRobinDirectoryUtil lookupUtil = new JRobinDirectoryUtil();
        
        assertEquals("src/test/resources/share/rrd/9/me1-0002baaacffe/mib2-interfaces.rrd", lookupUtil.getIfInOctetsJrb(RRD_TOOL_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/9/me1-0002baaacffe/mib2-interfaces.rrd", lookupUtil.getIfOutOctetsJrb(RRD_TOOL_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        
        System.setProperty("org.opennms.rrd.storeByGroup", "false");
        
        assertEquals("src/test/resources/share/rrd/9/me1-0002baaacffe/ifHCInOctets.rrd", lookupUtil.getIfInOctetsJrb(RRD_TOOL_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/9/me1-0002baaacffe/ifHCOutOctets.rrd", lookupUtil.getIfOutOctetsJrb(RRD_TOOL_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
    }
    
    @Test
    public void testJRobinDirectoryUtilCustomExtension() throws FileNotFoundException, IOException {
        System.setProperty("org.opennms.rrd.fileExtension", ".jrb");
        JRobinDirectoryUtil lookupUtil = new JRobinDirectoryUtil();
        
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/mib2-interfaces.jrb", lookupUtil.getIfInOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/mib2-interfaces.jrb", lookupUtil.getIfOutOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        
        System.setProperty("org.opennms.rrd.fileExtension", ".bogus");
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/mib2-interfaces.bogus", lookupUtil.getIfInOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/mib2-interfaces.bogus", lookupUtil.getIfOutOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        
        System.setProperty("org.opennms.rrd.fileExtension", ".rrd");
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/mib2-interfaces.rrd", lookupUtil.getIfInOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/mib2-interfaces.rrd", lookupUtil.getIfOutOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        
    }
    
    @Test
    public void testUseByForeignSource() throws FileNotFoundException, IOException {
        System.setProperty("org.opennms.rrd.fileExtension", ".jrb");
        JRobinDirectoryUtil lookupUtil = new JRobinDirectoryUtil();

        System.setProperty("org.opennms.rrd.storeByForeignSource", "false");
        assertEquals("src/test/resources/share/rrd/snmp/9", lookupUtil.getNodeLevelResourceDirectory(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID));
        assertEquals("ifInOctets", lookupUtil.getIfInOctetsDataSource(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("ifOutOctets", lookupUtil.getIfOutOctetsDataSource(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/mib2-interfaces.jrb", lookupUtil.getIfInOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/snmp/9/me1-0002baaacffe/mib2-interfaces.jrb", lookupUtil.getIfOutOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));

        System.setProperty("org.opennms.rrd.storeByForeignSource", "true");
        assertEquals("src/test/resources/share/rrd/snmp/fs/Servers/S001", lookupUtil.getNodeLevelResourceDirectory(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID));
        assertEquals("ifHCInOctets", lookupUtil.getIfInOctetsDataSource(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("ifHCOutOctets", lookupUtil.getIfOutOctetsDataSource(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/snmp/fs/Servers/S001/me1-0002baaacffe/mib2-X-interfaces.jrb", lookupUtil.getIfInOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/snmp/fs/Servers/S001/me1-0002baaacffe/mib2-X-interfaces.jrb", lookupUtil.getIfOutOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));

        System.setProperty("org.opennms.rrd.storeByGroup", "false");
        assertEquals("src/test/resources/share/rrd/snmp/fs/Servers/S001/me1-0002baaacffe/ifHCInOctets.jrb", lookupUtil.getIfInOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
        assertEquals("src/test/resources/share/rrd/snmp/fs/Servers/S001/me1-0002baaacffe/ifHCOutOctets.jrb", lookupUtil.getIfOutOctetsJrb(JRB_DIRECTORY, NODE_ID, FOREIGN_SOURCE, FOREIGN_ID, INTERFACE));
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
