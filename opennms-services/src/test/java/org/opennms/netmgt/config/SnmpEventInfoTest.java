//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Aug 23: Move snmp-config.xml file into an external file. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.config;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;


import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * JUnit tests for the configureSNMP event handling and optimization of
 * the SNMP configuration XML.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 *
 */
public class SnmpEventInfoTest {
    
    @Test
    public void testConigRangeCreate() {
        ConfigRange r = new ConfigRange("192.168.1.1", "192.168.1.2");
        assertEquals(0xc0a80101L, r.getBeginLong());
        assertEquals(0xc0a80102L, r.getEndLong());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConigRangeCreateOutOfOrder() {
        new ConfigRange("192.168.1.2", "192.168.1.1");
    }
    
    @Test
    public void testContainsAddr() {
        ConfigRange r = new ConfigRange("192.168.1.1", "192.168.1.3");
        assertFalse(r.contains("192.168.0.1"));
        assertTrue(r.contains("192.168.1.1"));
        assertTrue(r.contains("192.168.1.2"));
        assertTrue(r.contains("192.168.1.3"));
        assertFalse(r.contains("192.168.1.4"));
    }


    @Test
    public void testContainsRange() {
        ConfigRange r = new ConfigRange("192.168.1.1", "192.168.1.10");
        assertTrue(r.contains(new ConfigRange("192.168.1.1", "192.168.1.1")));
        assertTrue(r.contains(new ConfigRange("192.168.1.10", "192.168.1.10")));
        assertTrue(r.contains(new ConfigRange("192.168.1.2", "192.168.1.7")));
        assertFalse(r.contains(new ConfigRange("192.168.1.0", "192.168.1.1")));
        assertFalse(r.contains(new ConfigRange("192.168.1.2", "192.168.1.11")));
        assertFalse(r.contains(new ConfigRange("192.168.1.0", "192.168.1.11")));

    }

    @Test
    public void testPreceedsRange() {
        ConfigRange r = new ConfigRange("192.168.1.10", "192.168.1.20");
        assertTrue(r.preceeds(new ConfigRange("192.168.1.21", "192.168.1.30")));
        assertTrue(r.preceeds(new ConfigRange("192.168.1.21", "192.168.1.21")));
        assertFalse(r.preceeds(new ConfigRange("192.168.1.20", "192.168.1.30")));
        assertFalse(r.preceeds(new ConfigRange("192.168.1.7", "192.168.1.9")));
    }

    @Test
    public void testOverlapsRange() {
        ConfigRange r = new ConfigRange("192.168.1.10", "192.168.1.20");
        assertTrue(r.overlaps(new ConfigRange("192.168.1.10", "192.168.1.10")));
        assertTrue(r.overlaps(new ConfigRange("192.168.1.20", "192.168.1.20")));
        assertTrue(r.overlaps(new ConfigRange("192.168.1.10", "192.168.1.20")));
        assertTrue(r.overlaps(new ConfigRange("192.168.1.9", "192.168.1.22")));
        assertTrue(r.overlaps(new ConfigRange("192.168.1.15", "192.168.1.22")));
        assertTrue(r.overlaps(new ConfigRange("192.168.1.9", "192.168.1.15")));
        assertTrue(r.overlaps(new ConfigRange("192.168.1.11", "192.168.1.19")));
        assertFalse(r.overlaps(new ConfigRange("192.168.1.5", "192.168.1.9")));
        assertFalse(r.overlaps(new ConfigRange("192.168.1.21", "192.168.1.22")));

    }

    @Test
    public void testAdjacentRange() {
        ConfigRange r = new ConfigRange("192.168.1.10", "192.168.1.20");
        assertTrue(r.adjacent(new ConfigRange("192.168.1.7", "192.168.1.9")));
        assertFalse(r.adjacent(new ConfigRange("192.168.1.7", "192.168.1.8")));
        assertFalse(r.adjacent(new ConfigRange("192.168.1.7", "192.168.1.21")));
        assertTrue(r.adjacent(new ConfigRange("192.168.1.21", "192.168.1.21")));
    }
    
    @Test
    public void testConfigRangeEquals() {
        ConfigRange r = new ConfigRange("192.168.1.10", "192.168.1.20");
        assertEquals(r, r);
        assertEquals(r, new ConfigRange("192.168.1.10", "192.168.1.20"));
        assertFalse(r.equals( new ConfigRange("192.168.1.10", "192.168.1.19") ));
    }
    
    @Test
    public void testCombine() {
        ConfigRange r = new ConfigRange("192.168.1.10", "192.168.1.20");
        
        assertEquals(r, r.combine(new ConfigRange("192.168.1.10", "192.168.1.20")));
        assertEquals(r, r.combine(new ConfigRange("192.168.1.11", "192.168.1.20")));
        assertEquals(r, r.combine(new ConfigRange("192.168.1.10", "192.168.1.19")));
        assertEquals(r, r.combine(new ConfigRange("192.168.1.11", "192.168.1.19")));
        assertEquals(new ConfigRange("192.168.1.9", "192.168.1.20"), r.combine(new ConfigRange("192.168.1.9", "192.168.1.12")));
        assertEquals(new ConfigRange("192.168.1.10", "192.168.1.22"), r.combine(new ConfigRange("192.168.1.13", "192.168.1.22")));
        assertEquals(new ConfigRange("192.168.1.9", "192.168.1.22"), r.combine(new ConfigRange("192.168.1.9", "192.168.1.22")));
        assertEquals(new ConfigRange("192.168.1.7", "192.168.1.20"), r.combine(new ConfigRange("192.168.1.7", "192.168.1.9")));
        assertEquals(new ConfigRange("192.168.1.10", "192.168.1.24"), r.combine(new ConfigRange("192.168.1.21", "192.168.1.24")));
    }
    
    @Test
    public void testRemove() {
        ConfigRange r = new ConfigRange("192.168.1.10", "192.168.1.20");

        assertArrayEquals(new ConfigRange[0], r.remove(r));
        assertArrayEquals(new ConfigRange[0], r.remove(new ConfigRange("192.168.1.5", "192.168.1.27")));
        assertArrayEquals(new ConfigRange[] { r }, r.remove(new ConfigRange("192.168.1.5", "192.168.1.7")));
        assertArrayEquals(new ConfigRange[] { r }, r.remove(new ConfigRange("192.168.1.22", "192.168.1.27")));
        assertArrayEquals(new ConfigRange[] { new ConfigRange("192.168.1.15", "192.168.1.20") }, r.remove(new ConfigRange("192.168.1.5", "192.168.1.14")));
        assertArrayEquals(new ConfigRange[] { new ConfigRange("192.168.1.10", "192.168.1.14") }, r.remove(new ConfigRange("192.168.1.15", "192.168.1.24")));
        assertArrayEquals(new ConfigRange[] { new ConfigRange("192.168.1.10", "192.168.1.14"), new ConfigRange("192.168.1.16", "192.168.1.20") }, r.remove(new ConfigRange("192.168.1.15", "192.168.1.15")));
    }
    
    @Test
    public void testListAdd() {
        ConfigRange r = new ConfigRange("192.168.1.10", "192.168.1.20");

        ConfigRangeList ranges = new ConfigRangeList();
        ranges.add(r);
        
        assertArrayEquals(new ConfigRange[] { r }, ranges.toArray());
        
        ConfigRange s = new ConfigRange("192.168.1.30", "192.168.1.40");
        ranges.add(s);

        assertArrayEquals(new ConfigRange[] { r, s }, ranges.toArray());

        ConfigRange t = new ConfigRange("192.168.1.2", "192.168.1.8");
        ranges.add(t);

        assertArrayEquals(new ConfigRange[] { t, r, s }, ranges.toArray());

        ConfigRange u = new ConfigRange("192.168.1.22", "192.168.1.28");
        ranges.add(u);

        assertArrayEquals(new ConfigRange[] { t, r, u, s }, ranges.toArray());
        
        ranges.add(new ConfigRange("192.168.1.18", "192.168.1.24"));
        
        assertArrayEquals(new ConfigRange[] { t, new ConfigRange("192.168.1.10", "192.168.1.28"), s }, ranges.toArray());

        ranges.add(new ConfigRange("192.168.1.9", "192.168.1.9"));
        
        assertArrayEquals(new ConfigRange[] { new ConfigRange("192.168.1.2", "192.168.1.28"), s }, ranges.toArray());

    }
    
    @Test
    public void testListRemove() {
        ConfigRange r = new ConfigRange("192.168.1.1", "192.168.1.100");

        ConfigRangeList ranges = new ConfigRangeList();
        ranges.add(r);
        
        ConfigRange s = new ConfigRange("192.168.1.30", "192.168.1.40");
        ranges.remove(s);

        assertArrayEquals(new ConfigRange[] { new ConfigRange("192.168.1.1", "192.168.1.29"), new ConfigRange("192.168.1.41", "192.168.1.100") }, ranges.toArray());

        ConfigRange t = new ConfigRange("192.168.1.20", "192.168.1.35");
        ranges.remove(t);

        assertArrayEquals(new ConfigRange[] { new ConfigRange("192.168.1.1", "192.168.1.19"), new ConfigRange("192.168.1.41", "192.168.1.100") }, ranges.toArray());

        ConfigRange u = new ConfigRange("192.168.1.35", "192.168.1.50");
        ranges.remove(u);

        assertArrayEquals(new ConfigRange[] { new ConfigRange("192.168.1.1", "192.168.1.19"), new ConfigRange("192.168.1.51", "192.168.1.100") }, ranges.toArray());
        
        ranges.remove(new ConfigRange("192.168.1.60", "192.168.1.70"));
        
        assertArrayEquals(new ConfigRange[] { new ConfigRange("192.168.1.1", "192.168.1.19"), new ConfigRange("192.168.1.51", "192.168.1.59"), new ConfigRange("192.168.1.71", "192.168.1.100") }, ranges.toArray());

        ranges.remove(new ConfigRange("192.168.1.10", "192.168.1.80"));
        
        assertArrayEquals(new ConfigRange[] { new ConfigRange("192.168.1.1", "192.168.1.9"), new ConfigRange("192.168.1.81", "192.168.1.100") }, ranges.toArray());

    }


/**
     * This tests the ability of a configureSNMP event to change the community
     * string of a specific address.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    @Test
    public final void testModifySpecificInDef() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific xmlns=\"\">192.168.0.5</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition read-community=\"abc\">\n" + 
        "        <specific xmlns=\"\">192.168.0.5</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" +
        "";

        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setCommunityString("abc");
        info.setFirstIPAddress("192.168.0.5");
        
        SnmpPeerFactory.getInstance().define(info);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
        
    }

    /**
     * This tests the merging of a new specific into a definition that already contains a specific
     * that is adjacent.  The two specifics should be converted to a single range in the definition.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    @Test
    public final void testAddAdjacentSpecificToDef() throws MarshalException, ValidationException, IOException {

        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific xmlns=\"\">192.168.0.5</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.0.5\" end=\"192.168.0.6\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());
        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress("192.168.0.6");
        
        SnmpPeerFactory.getInstance().define(info);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
        
}

    /**
     * This tests the merging of a new specific into a definition that already contains a specific
     * that is adjacent.  The two specifics should be converted to a single range in the definition.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    @Test
    public final void testAddSpecificBetweenAdjacentsSpecifics() throws MarshalException, ValidationException, IOException {

        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific xmlns=\"\">192.168.0.5</specific>\n" + 
        "        <specific xmlns=\"\">192.168.0.7</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.0.5\" end=\"192.168.0.7\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());
        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress("192.168.0.6");
        
        SnmpPeerFactory.getInstance().define(info);

        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
        
}

    @Test
    public final void testAddSpecificBetweenAdjacentSpecificAndRange() throws MarshalException, ValidationException, IOException {

        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.0.7\" end=\"192.168.0.9\"/>\n" + 
        "        <specific xmlns=\"\">192.168.0.5</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.0.5\" end=\"192.168.0.9\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());
        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress("192.168.0.6");
        
        SnmpPeerFactory.getInstance().define(info);

        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
        
}
    /**
     * This tests the ability to add a new range of IPs that create a new definition and these IPs
     * were previously defined withing the a range of a current definition.  The result should be
     * that the previous range is split into 2 ranges and the new range is now contained in a new
     * separate definition.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    @Test
    public void testSplitRange() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.100\" end=\"192.168.1.200\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.100\" end=\"192.168.1.119\"/>\n" + 
        "        <ns2:range xmlns:ns2=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.131\" end=\"192.168.1.200\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <ns3:range xmlns:ns3=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.120\" end=\"192.168.1.130\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());


        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.1.120");
        info.setLastIPAddress("192.168.1.130");

        SnmpPeerFactory.getInstance().define(info);
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
        
    }

    /**
     * This tests the ability to create a new definition from an IP that was previously covered by
     * a range in an existing definition.  The result should be the previous range is converted into 2
     * new ranges and the new specific is added to a new definition in the config.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    @Test
    public void testRemoveSpecificFromRange() throws MarshalException, ValidationException, IOException {
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.100\" end=\"192.168.1.200\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" +
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.100\" end=\"192.168.1.119\"/>\n" + 
        "        <ns2:range xmlns:ns2=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.121\" end=\"192.168.1.200\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <specific xmlns=\"\">192.168.1.120</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());


        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.1.120");

        SnmpPeerFactory.getInstance().define(info);
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
        
    }
    
    /**
     * This tests the ability to move a specific from one definition into a range of another definition.  The
     * results should be that the 2 ranges in the first definition are recombined into a single range based on 
     * the single IP address that was in a different existing defintion that will now be removed and the definition
     * deleted.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    @Test
    public void testRecombineSpecificIntoRange() throws MarshalException, ValidationException, IOException {
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.10\" end=\"192.168.1.14\"/>\n" + 
        "        <ns2:range xmlns:ns2=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.16\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <specific xmlns=\"\">192.168.1.15</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.10\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";    

        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());


        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress("192.168.1.15");

        SnmpPeerFactory.getInstance().define(info);
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
        
    }
    
    /**
     * This tests the ability to remove a specific IP from one definition with a newly specified range.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    @Test
    public void testRemoveSpecificInSeparateDefWithNewRange() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        		"<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        		"    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        		"    <definition version=\"v2c\">\n" + 
        		"        <specific xmlns=\"\">192.168.1.30</specific>\n" + 
        		"        <specific xmlns=\"\">10.1.1.1</specific>\n" + 
        		"    </definition>\n" + 
        		"</snmp-config>\n" + 
        		"";

        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific xmlns=\"\">10.1.1.1</specific>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.15\" end=\"192.168.1.35\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());


        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.1.15");
        info.setLastIPAddress("192.168.1.35");

        SnmpPeerFactory.getInstance().define(info);
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);

    }
    
    /**
     * This tests the behavior of specifying an invalid range.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    @Test
    public void testInvalidRange() throws MarshalException, ValidationException, IOException {
        String snmpConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\"\n" + 
        "   read-community=\"public\" write-community=\"private\">\n" + 
        "   <definition version=\"v2c\">\n" + 
        "       <range begin=\"192.168.0.3\" end=\"192.168.0.100\"/>" + 
        "   </definition>\n" + 
        "\n" + 
        "</snmp-config>\n" + 
        "";
                
        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));

        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.0.3");
        info.setLastIPAddress("192.168.0.1");

        try {
            SnmpPeerFactory.getInstance().define(info);
            fail("Expected IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            
        }
    }
    
    /**
     * This tests the addition of a new specific definition that is the same address as the beginning of
     * a range in a current definition.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    @Test
    public void testNewSpecifcSameAsBeginInOldDef() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.0.3\" end=\"192.168.0.100\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.0.4\" end=\"192.168.0.100\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <specific xmlns=\"\">192.168.0.3</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        

        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());


        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.0.3");

        SnmpPeerFactory.getInstance().define(info);
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);

    }
    
    /**
     * This tests the merging of a new definition that contains a range of IP addresses that overlaps
     * the end of one range and the beginning of another range in a current definition.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    @Test
    public void testOverlapsTwoRanges() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        		"<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        		"    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        		"    <definition version=\"v2c\">\n" + 
        		"        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        		"            begin=\"192.168.1.10\" end=\"192.168.1.20\"/>\n" + 
        		"        <ns2:range xmlns:ns2=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        		"            begin=\"192.168.1.30\" end=\"192.168.1.40\"/>\n" + 
        		"    </definition>\n" + 
        		"</snmp-config>\n" + 
        		"";
        
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.10\" end=\"192.168.1.14\"/>\n" + 
        "        <ns2:range xmlns:ns2=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.36\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <ns3:range xmlns:ns3=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.15\" end=\"192.168.1.35\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());


        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.1.15");
        info.setLastIPAddress("192.168.1.35");

        SnmpPeerFactory.getInstance().define(info);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
    }

    public void testOverlapsTwoRangesAndCombinesThem(String firstIp, String lastIp) throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
                "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
                "    <definition version=\"v2c\">\n" + 
                "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
                "            begin=\"192.168.1.10\" end=\"192.168.1.20\"/>\n" + 
                "        <ns2:range xmlns:ns2=\"http://xmlns.opennms.org/xsd/types\"\n" + 
                "            begin=\"192.168.1.30\" end=\"192.168.1.40\"/>\n" + 
                "    </definition>\n" + 
                "</snmp-config>\n" + 
                "";
        
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.10\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());


        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress(firstIp);
        info.setLastIPAddress(lastIp);

        SnmpPeerFactory.getInstance().define(info);

        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
    }
    
    @Test
    public void testCombineOverlappingRanges() throws Exception {
        testOverlapsTwoRangesAndCombinesThem("192.168.1.15", "192.168.1.35");
        testOverlapsTwoRangesAndCombinesThem("192.168.1.10", "192.168.1.35");
        testOverlapsTwoRangesAndCombinesThem("192.168.1.20", "192.168.1.35");
        testOverlapsTwoRangesAndCombinesThem("192.168.1.21", "192.168.1.35");
        testOverlapsTwoRangesAndCombinesThem("192.168.1.15", "192.168.1.40");
        testOverlapsTwoRangesAndCombinesThem("192.168.1.21", "192.168.1.30");
        testOverlapsTwoRangesAndCombinesThem("192.168.1.21", "192.168.1.29");
        testOverlapsTwoRangesAndCombinesThem("192.168.1.10", "192.168.1.40");
    }
    /**
     * This tests moving a range that from one defintion into another defintion for which it overlaps
     * one range in the merging definition and creates 2 adjacent ranges that should be merged together.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    @Test
    public void testRecombineRanges() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.10\" end=\"192.168.1.14\"/>\n" + 
        "        <ns2:range xmlns:ns2=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.36\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <ns3:range xmlns:ns3=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.15\" end=\"192.168.1.35\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.10\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";            
        
        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());


        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress("192.168.1.15");
        info.setLastIPAddress("192.168.1.35");

        SnmpPeerFactory.getInstance().define(info);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);

    }
    
    /**
     * This tests recombing a range into an existing defintion for which the new range overlaps the current definition's 
     * range and overlaps the end of one range and the beginning of another range of the merging definition.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    @Test
    public void testRecombineRangesNonAdjacentRange() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.10\" end=\"192.168.1.14\"/>\n" + 
        "        <ns2:range xmlns:ns2=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.36\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <ns3:range xmlns:ns3=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.15\" end=\"192.168.1.35\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.1.10\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());


        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress("192.168.1.12");
        info.setLastIPAddress("192.168.1.38");

        SnmpPeerFactory.getInstance().define(info);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
    }

    /**
     * This tests moving the specific from a current defition to a new defintion.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    @Test
    public final void testAddNewSpecificToConfig() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific xmlns=\"\">192.168.0.5</specific>\n" + 
        "        <specific xmlns=\"\">192.168.0.6</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific xmlns=\"\">192.168.0.5</specific>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <specific xmlns=\"\">192.168.0.6</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());

        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.0.6");
        
        SnmpPeerFactory.getInstance().define(info);

        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);


    }

    /**
     * Test method for {@link org.opennms.netmgt.config.SnmpEventInfo#optimizeAllDefs()}.
     * @throws ValidationException 
     * @throws MarshalException 
     * @throws IOException 
     */
    @Test
    @Ignore("This is no longer really valid since we don't have to optimize in a separate pass")
    public final void testOptimizeAllDefs() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific xmlns=\"\">192.168.0.5</specific>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"opennmsrules\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.100.1\" end=\"192.168.100.254\"/>\n" + 
        "        <ns2:range xmlns:ns2=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.101.1\" end=\"192.168.101.254\"/>\n" + 
        "        <ns3:range xmlns:ns3=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.102.1\" end=\"192.168.102.254\"/>\n" + 
        "        <ns4:range xmlns:ns4=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.103.1\" end=\"192.168.103.254\"/>\n" + 
        "        <ns5:range xmlns:ns5=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.104.1\" end=\"192.168.104.254\"/>\n" + 
        "        <ns6:range xmlns:ns6=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.105.1\" end=\"192.168.105.254\"/>\n" + 
        "        <ns7:range xmlns:ns7=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.106.1\" end=\"192.168.106.254\"/>\n" + 
        "        <ns8:range xmlns:ns8=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.107.1\" end=\"192.168.107.254\"/>\n" + 
        "        <ns9:range xmlns:ns9=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.0.1\" end=\"192.168.0.10\"/>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"opennmsrules2\">\n" + 
        "        <ns10:range xmlns:ns10=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.100.0\" end=\"192.168.100.255\"/>\n" + 
        "        <ns11:range xmlns:ns11=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.101.0\" end=\"192.168.101.255\"/>\n" + 
        "        <ns12:range xmlns:ns12=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.102.0\" end=\"192.168.102.255\"/>\n" + 
        "        <ns13:range xmlns:ns13=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.103.0\" end=\"192.168.103.255\"/>\n" + 
        "        <ns14:range xmlns:ns14=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.104.0\" end=\"192.168.104.255\"/>\n" + 
        "        <ns15:range xmlns:ns15=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.105.0\" end=\"192.168.105.255\"/>\n" + 
        "        <ns16:range xmlns:ns16=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.106.0\" end=\"192.168.106.255\"/>\n" + 
        "        <ns17:range xmlns:ns17=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.107.0\" end=\"192.168.107.255\"/>\n" + 
        "        <ns18:range xmlns:ns18=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.0.1\" end=\"192.168.0.10\"/>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"splice-test\" version=\"v2c\">\n" + 
        "        <ns19:range xmlns:ns19=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"10.1.2.1\" end=\"10.1.2.100\"/>\n" + 
        "        <ns20:range xmlns:ns20=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"11.1.2.1\" end=\"11.1.2.100\"/>\n" + 
        "        <ns21:range xmlns:ns21=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"12.1.2.1\" end=\"12.1.2.100\"/>\n" + 
        "        <specific xmlns=\"\">10.1.1.1</specific>\n" + 
        "        <specific xmlns=\"\">10.1.1.2</specific>\n" + 
        "        <specific xmlns=\"\">10.1.1.3</specific>\n" + 
        "        <specific xmlns=\"\">10.1.1.5</specific>\n" + 
        "        <specific xmlns=\"\">10.1.1.6</specific>\n" + 
        "        <specific xmlns=\"\">10.1.1.10</specific>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"splice2-test\">\n" + 
        "        <ns22:range xmlns:ns22=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"10.1.1.11\" end=\"10.1.1.100\"/>\n" + 
        "        <ns23:range xmlns:ns23=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"11.1.2.1\" end=\"11.1.2.100\"/>\n" + 
        "        <ns24:range xmlns:ns24=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"12.1.2.1\" end=\"12.1.2.100\"/>\n" + 
        "        <specific xmlns=\"\">10.1.1.10</specific>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"splice3-test\">\n" + 
        "        <ns25:range xmlns:ns25=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"10.1.1.11\" end=\"10.1.1.100\"/>\n" + 
        "        <ns26:range xmlns:ns26=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"11.1.2.1\" end=\"11.1.2.1\"/>\n" + 
        "        <ns27:range xmlns:ns27=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"12.1.2.1\" end=\"12.1.2.1\"/>\n" + 
        "        <specific xmlns=\"\">10.1.1.10</specific>\n" + 
        "        <specific xmlns=\"\">10.1.1.12</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
 
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" retry=\"3\"\n" + 
        "    timeout=\"800\" read-community=\"public\" write-community=\"private\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific xmlns=\"\">192.168.0.5</specific>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"opennmsrules\">\n" + 
        "        <ns1:range xmlns:ns1=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.0.1\" end=\"192.168.0.10\"/>\n" + 
        "        <ns2:range xmlns:ns2=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.100.1\" end=\"192.168.100.254\"/>\n" + 
        "        <ns3:range xmlns:ns3=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.101.1\" end=\"192.168.101.254\"/>\n" + 
        "        <ns4:range xmlns:ns4=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.102.1\" end=\"192.168.102.254\"/>\n" + 
        "        <ns5:range xmlns:ns5=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.103.1\" end=\"192.168.103.254\"/>\n" + 
        "        <ns6:range xmlns:ns6=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.104.1\" end=\"192.168.104.254\"/>\n" + 
        "        <ns7:range xmlns:ns7=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.105.1\" end=\"192.168.105.254\"/>\n" + 
        "        <ns8:range xmlns:ns8=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.106.1\" end=\"192.168.106.254\"/>\n" + 
        "        <ns9:range xmlns:ns9=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.107.1\" end=\"192.168.107.254\"/>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"opennmsrules2\">\n" + 
        "        <ns10:range xmlns:ns10=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.0.1\" end=\"192.168.0.10\"/>\n" + 
        "        <ns11:range xmlns:ns11=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"192.168.100.0\" end=\"192.168.107.255\"/>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"splice-test\" version=\"v2c\">\n" + 
        "        <ns12:range xmlns:ns12=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"10.1.1.1\" end=\"10.1.1.3\"/>\n" + 
        "        <ns13:range xmlns:ns13=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"10.1.1.5\" end=\"10.1.1.6\"/>\n" + 
        "        <ns14:range xmlns:ns14=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"10.1.2.1\" end=\"10.1.2.100\"/>\n" + 
        "        <ns15:range xmlns:ns15=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"11.1.2.1\" end=\"11.1.2.100\"/>\n" + 
        "        <ns16:range xmlns:ns16=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"12.1.2.1\" end=\"12.1.2.100\"/>\n" + 
        "        <specific xmlns=\"\">10.1.1.10</specific>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"splice2-test\">\n" + 
        "        <ns17:range xmlns:ns17=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"10.1.1.10\" end=\"10.1.1.100\"/>\n" + 
        "        <ns18:range xmlns:ns18=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"11.1.2.1\" end=\"11.1.2.100\"/>\n" + 
        "        <ns19:range xmlns:ns19=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"12.1.2.1\" end=\"12.1.2.100\"/>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"splice3-test\">\n" + 
        "        <ns20:range xmlns:ns20=\"http://xmlns.opennms.org/xsd/types\"\n" + 
        "            begin=\"10.1.1.10\" end=\"10.1.1.100\"/>\n" + 
        "        <specific xmlns=\"\">11.1.2.1</specific>\n" + 
        "        <specific xmlns=\"\">12.1.2.1</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        Reader rdr = new StringReader(snmpConfigXml);
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());

        MergeableDefinition def;
        
        
        SnmpEventInfo info = new SnmpEventInfo();
        
        SnmpConfig SnmpConfig = SnmpPeerFactory.getSnmpConfig();
        assertEquals(6, SnmpConfig.getDefinitionCount());
        
        info.setCommunityString("opennmsrules2");
        def = mgr.findDefMatchingAttributes(info.createDef());
        assertNotNull(def);
        assertEquals(9, def.getConfigDef().getRangeCount());
        
        //do bunch more...
        
        mgr.optimizeAllDefs();
//      String config = SnmpPeerFactory.marshallConfig();
//      System.err.println(config);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);

        
        def = mgr.findDefMatchingAttributes(info.createDef());
        assertNotNull(def);
        assertEquals(2, def.getConfigDef().getRangeCount());
        

    }
    
    @Test
    //@Ignore
    public void testAddSpecificToBigFile() throws Exception {
        Resource res = new FileSystemResource("/Users/brozow/big-snmp-config.xml");
        
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(new InputStreamReader(res.getInputStream())));

        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setCommunityString("th3l04n3r");
        info.setFirstIPAddress("192.168.1.15");
        info.setLastIPAddress("192.168.1.35");
        
        SnmpPeerFactory.getInstance().define(info);

        String actualConfig = SnmpPeerFactory.marshallConfig();
        System.err.println(actualConfig);
        //assertEquals(expectedConfig, actualConfig);
        

    }


}
