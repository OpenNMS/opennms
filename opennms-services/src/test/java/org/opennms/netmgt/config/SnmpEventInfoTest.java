/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.model.discovery.IPAddress;
import org.opennms.netmgt.model.discovery.IPAddressRange;
import org.opennms.netmgt.model.discovery.IPAddressRangeSet;
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
    
    @Test(expected=IllegalArgumentException.class)
    public void testConfigRangeCreateOutOfOrder() {
        new IPAddressRange("192.168.1.2", "192.168.1.1");
    }
    
    @Test
    public void testAddressIncr() {
        IPAddress ipA = new IPAddress("::5");
        assertEquals("::6", ipA.incr().toString());
        
        IPAddress ipB = new IPAddress("::ffff:ffff");
        assertEquals("::1:0:0", ipB.incr().toString());
        
        IPAddress ipC = new IPAddress("::ff00:ffff");
        assertEquals("::ff01:0", ipC.incr().toString());

        IPAddress ipD = new IPAddress("::ff00:7fff");
        assertEquals("::ff00:8000", ipD.incr().toString());

    }
    
    @Test
    public void testConfigAddressDecr() {
        IPAddress a = new IPAddress("::6");
        assertEquals("::5", a.decr().toString());
        
        IPAddress b = new IPAddress("::1:0:0");
        assertEquals("::ffff:ffff", b.decr().toString());
        
        IPAddress c = new IPAddress("ff::ffff:1:0");
        assertEquals("ff::ffff:0:ffff", c.decr().toString());

        IPAddress d = new IPAddress("ff::ffff:1:8000");
        assertEquals("ff::ffff:1:7fff", d.decr().toString());
    }

    @Test
    public void testContainsAddr() {
        IPAddressRange range = new IPAddressRange("192.168.1.1", "192.168.1.3");
        assertFalse(range.contains("192.168.0.1"));
        assertTrue(range.contains("192.168.1.1"));
        assertTrue(range.contains("192.168.1.2"));
        assertTrue(range.contains("192.168.1.3"));
        assertFalse(range.contains("192.168.1.4"));
    }

    @Test
    public void testFollows() {
        IPAddressRange s = new IPAddressRange("192.168.1.5", "192.168.1.6");
        IPAddressRange q = new IPAddressRange("192.168.1.1", "192.168.1.2");
        IPAddressRange r = new IPAddressRange("192.168.1.3", "192.168.1.4");
        assertTrue(r.comesAfter(q));
        assertFalse(r.comesAfter(r));
        assertFalse(r.comesAfter(s));
    }

    @Test
    public void testContainsAddrIPv6() {
        IPAddressRange r = new IPAddressRange("2001:db8::10", "2001:db8::20");
        assertFalse(r.contains("192.168.0.1"));
        assertFalse(r.contains("2001:db8::1"));
        assertTrue(r.contains("2001:db8::10"));
        assertTrue(r.contains("2001:db8::15"));
        assertTrue(r.contains("2001:db8::20"));
        assertFalse(r.contains("2001:db8::21"));
    }


    @Test
    public void testContainsRange() {
        IPAddressRange r = new IPAddressRange("192.168.1.1", "192.168.1.10");
        assertTrue(r.contains(new IPAddressRange("192.168.1.1", "192.168.1.1")));
        assertTrue(r.contains(new IPAddressRange("192.168.1.10", "192.168.1.10")));
        assertTrue(r.contains(new IPAddressRange("192.168.1.2", "192.168.1.7")));
        assertFalse(r.contains(new IPAddressRange("192.168.1.0", "192.168.1.1")));
        assertFalse(r.contains(new IPAddressRange("192.168.1.2", "192.168.1.11")));
        assertFalse(r.contains(new IPAddressRange("192.168.1.0", "192.168.1.11")));

    }

    @Test
    public void testPreceedsRange() {
        IPAddressRange r = new IPAddressRange("192.168.1.10", "192.168.1.20");
        assertTrue(r.comesBefore(new IPAddressRange("192.168.1.21", "192.168.1.30")));
        assertTrue(r.comesBefore(new IPAddressRange("192.168.1.21", "192.168.1.21")));
        assertFalse(r.comesBefore(new IPAddressRange("192.168.1.20", "192.168.1.30")));
        assertFalse(r.comesBefore(new IPAddressRange("192.168.1.7", "192.168.1.9")));
    }

    @Test
    public void testOverlapsRange() {
        IPAddressRange r = new IPAddressRange("192.168.1.10", "192.168.1.20");
        assertTrue(r.overlaps(new IPAddressRange("192.168.1.10", "192.168.1.10")));
        assertTrue(r.overlaps(new IPAddressRange("192.168.1.20", "192.168.1.20")));
        assertTrue(r.overlaps(new IPAddressRange("192.168.1.10", "192.168.1.20")));
        assertTrue(r.overlaps(new IPAddressRange("192.168.1.9", "192.168.1.22")));
        assertTrue(r.overlaps(new IPAddressRange("192.168.1.15", "192.168.1.22")));
        assertTrue(r.overlaps(new IPAddressRange("192.168.1.9", "192.168.1.15")));
        assertTrue(r.overlaps(new IPAddressRange("192.168.1.11", "192.168.1.19")));
        assertFalse(r.overlaps(new IPAddressRange("192.168.1.5", "192.168.1.9")));
        assertFalse(r.overlaps(new IPAddressRange("192.168.1.21", "192.168.1.22")));

    }

    @Test
    public void testAdjacentRange() {
        IPAddressRange r = new IPAddressRange("192.168.1.10", "192.168.1.20");
        assertTrue(r.adjoins(new IPAddressRange("192.168.1.7", "192.168.1.9")));
        assertFalse(r.adjoins(new IPAddressRange("192.168.1.7", "192.168.1.8")));
        assertFalse(r.adjoins(new IPAddressRange("192.168.1.7", "192.168.1.21")));
        assertTrue(r.adjoins(new IPAddressRange("192.168.1.21", "192.168.1.21")));
    }
    
    @Test
    public void testConfigRangeEquals() {
        IPAddressRange r = new IPAddressRange("192.168.1.10", "192.168.1.20");
        assertEquals(r, r);
        assertEquals(r, new IPAddressRange("192.168.1.10", "192.168.1.20"));
        assertFalse(r.equals(new IPAddressRange("192.168.1.10", "192.168.1.19")));
    }
    
    @Test
    public void testCombine() {
        IPAddressRange rr = new IPAddressRange("192.168.1.10", "192.168.1.20");
        assertEquals(rr, rr.combine(new IPAddressRange("192.168.1.10", "192.168.1.20")));
        assertEquals(rr, rr.combine(new IPAddressRange("192.168.1.11", "192.168.1.20")));
        assertEquals(rr, rr.combine(new IPAddressRange("192.168.1.10", "192.168.1.19")));
        assertEquals(rr, rr.combine(new IPAddressRange("192.168.1.11", "192.168.1.19")));
        assertEquals(new IPAddressRange("192.168.1.9", "192.168.1.20"), rr.combine(new IPAddressRange("192.168.1.9", "192.168.1.12")));
        assertEquals(new IPAddressRange("192.168.1.10", "192.168.1.22"), rr.combine(new IPAddressRange("192.168.1.13", "192.168.1.22")));
        assertEquals(new IPAddressRange("192.168.1.9", "192.168.1.22"), rr.combine(new IPAddressRange("192.168.1.9", "192.168.1.22")));
        assertEquals(new IPAddressRange("192.168.1.7", "192.168.1.20"), rr.combine(new IPAddressRange("192.168.1.7", "192.168.1.9")));
        assertEquals(new IPAddressRange("192.168.1.10", "192.168.1.24"), rr.combine(new IPAddressRange("192.168.1.21", "192.168.1.24")));
    }
    
    @Test
    public void testRemove() {
        IPAddressRange r = new IPAddressRange("192.168.1.10", "192.168.1.20");
        assertArrayEquals(new IPAddressRange[0], r.remove(r));
        assertArrayEquals(new IPAddressRange[0], r.remove(new IPAddressRange("192.168.1.5", "192.168.1.27")));
        assertArrayEquals(new IPAddressRange[] { r }, r.remove(new IPAddressRange("192.168.1.5", "192.168.1.7")));
        assertArrayEquals(new IPAddressRange[] { r }, r.remove(new IPAddressRange("192.168.1.22", "192.168.1.27")));
        assertArrayEquals(new IPAddressRange[] { new IPAddressRange("192.168.1.15", "192.168.1.20") }, r.remove(new IPAddressRange("192.168.1.5", "192.168.1.14")));
        assertArrayEquals(new IPAddressRange[] { new IPAddressRange("192.168.1.10", "192.168.1.14") }, r.remove(new IPAddressRange("192.168.1.15", "192.168.1.24")));
        assertArrayEquals(new IPAddressRange[] { new IPAddressRange("192.168.1.10", "192.168.1.14"), new IPAddressRange("192.168.1.16", "192.168.1.20") }, r.remove(new IPAddressRange("192.168.1.15", "192.168.1.15")));
    }
    
    @Test
    public void testListAdd() {
        IPAddressRange r = new IPAddressRange("192.168.1.10", "192.168.1.20");

        IPAddressRangeSet ranges = new IPAddressRangeSet();
        ranges.add(r);
        
        assertArrayEquals(new IPAddressRange[] { r }, ranges.toArray());
        
        IPAddressRange s = new IPAddressRange("192.168.1.30", "192.168.1.40");
        ranges.add(s);

        assertArrayEquals(new IPAddressRange[] { r, s }, ranges.toArray());

        IPAddressRange t = new IPAddressRange("192.168.1.2", "192.168.1.8");
        ranges.add(t);

        assertArrayEquals(new IPAddressRange[] { t, r, s }, ranges.toArray());

        IPAddressRange u = new IPAddressRange("192.168.1.22", "192.168.1.28");
        ranges.add(u);

        assertArrayEquals(new IPAddressRange[] { t, r, u, s }, ranges.toArray());
        
        ranges.add(new IPAddressRange("192.168.1.18", "192.168.1.24"));
        
        assertArrayEquals(new IPAddressRange[] { t, new IPAddressRange("192.168.1.10", "192.168.1.28"), s }, ranges.toArray());

        ranges.add(new IPAddressRange("192.168.1.9", "192.168.1.9"));
        
        assertArrayEquals(new IPAddressRange[] { new IPAddressRange("192.168.1.2", "192.168.1.28"), s }, ranges.toArray());

    }
    
    @Test
    public void testListRemove() {
        IPAddressRange r = new IPAddressRange("192.168.1.1", "192.168.1.100"); //{[1..100]}

        IPAddressRangeSet ranges = new IPAddressRangeSet();
        ranges.add(r);
        
        IPAddressRange s = new IPAddressRange("192.168.1.30", "192.168.1.40"); 
        ranges.remove(s); //{[1..29],[41..100]}

        assertArrayEquals(new IPAddressRange[] { new IPAddressRange("192.168.1.1", "192.168.1.29"), new IPAddressRange("192.168.1.41", "192.168.1.100") }, ranges.toArray());

        IPAddressRange t = new IPAddressRange("192.168.1.20", "192.168.1.35"); 
        ranges.remove(t); //{[1..19],[41..100]}

        assertArrayEquals(new IPAddressRange[] { new IPAddressRange("192.168.1.1", "192.168.1.19"), new IPAddressRange("192.168.1.41", "192.168.1.100") }, ranges.toArray());

        IPAddressRange u = new IPAddressRange("192.168.1.35", "192.168.1.50"); 
        ranges.remove(u); //{[1..19],[51..100]}

        assertArrayEquals(new IPAddressRange[] { new IPAddressRange("192.168.1.1", "192.168.1.19"), new IPAddressRange("192.168.1.51", "192.168.1.100") }, ranges.toArray());
        
        ranges.remove(new IPAddressRange("192.168.1.60", "192.168.1.70")); //{1..19],[51..59],[71..100]}
        
        assertArrayEquals(new IPAddressRange[] { new IPAddressRange("192.168.1.1", "192.168.1.19"), new IPAddressRange("192.168.1.51", "192.168.1.59"), new IPAddressRange("192.168.1.71", "192.168.1.100") }, ranges.toArray());

        ranges.remove(new IPAddressRange("192.168.1.10", "192.168.1.80")); //{1..9],[81..100}
        
        assertArrayEquals(new IPAddressRange[] { new IPAddressRange("192.168.1.1", "192.168.1.9"), new IPAddressRange("192.168.1.81", "192.168.1.100") }, ranges.toArray());

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
    public final void testModifySpecificInDef() throws IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific>192.168.0.5</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig = "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition read-community=\"abc\">\n" + 
        "        <specific>192.168.0.5</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" +
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());
        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setCommunityString("abc");
        info.setFirstIPAddress("192.168.0.5");
        
        SnmpPeerFactory.getInstance().define(info);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
        
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
    public final void testModifySpecificInDefIPv6() throws IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific>2001:db8::10</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig = "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition read-community=\"abc\">\n" + 
        "        <specific>2001:db8::10</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" +
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());
        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setCommunityString("abc");
        info.setFirstIPAddress("2001:db8::10");
        
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
    public final void testAddAdjacentSpecificToDef() throws IOException {

        String snmpConfigXml = "" +
        		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        		"<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        		"    <definition version=\"v2c\">\n" + 
        		"        <specific>192.168.0.5</specific>\n" + 
        		"    </definition>\n" + 
        		"</snmp-config>\n" +
        		"";

        String expectedConfig = "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.0.5\" end=\"192.168.0.6\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" +
        "";


        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
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
    public final void testAddAdjacentSpecificToDefIPv6() throws IOException {

        String snmpConfigXml = "" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
                "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
                "    <definition version=\"v2c\">\n" + 
                "        <specific>2001:db8::10</specific>\n" + 
                "    </definition>\n" + 
                "</snmp-config>\n" +
                "";

        String expectedConfig = "" +
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"2001:db8::10\" end=\"2001:db8::11\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" +
        "";


        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());
        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress("2001:db8::11");
        
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
    public final void testAddSpecificBetweenAdjacentsSpecifics() throws IOException {

        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific>192.168.0.5</specific>\n" + 
        "        <specific>192.168.0.7</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.0.5\" end=\"192.168.0.7\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
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
    public final void testAddSpecificBetweenAdjacentsSpecificsMostlyZeros() throws IOException {

        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific>0.0.0.5</specific>\n" + 
        "        <specific>0.0.0.7</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"0.0.0.5\" end=\"0.0.0.7\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());
        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress("0.0.0.6");
        
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
    public final void testAddSpecificBetweenAdjacentsSpecificsIPv6() throws IOException {

        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific>::5</specific>\n" + 
        "        <specific>::7</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"::5\" end=\"::7\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());
        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress("::6");
        
        SnmpPeerFactory.getInstance().define(info);

        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
        
    }
    @Test
    public final void testAddSpecificBetweenAdjacentSpecificAndRange() throws IOException {

        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.0.7\" end=\"192.168.0.9\"/>\n" + 
        "        <specific>192.168.0.5</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.0.5\" end=\"192.168.0.9\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
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
    public void testSplitRange() throws IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.100\" end=\"192.168.1.200\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.100\" end=\"192.168.1.119\"/>\n" + 
        "        <range begin=\"192.168.1.131\" end=\"192.168.1.200\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <range begin=\"192.168.1.120\" end=\"192.168.1.130\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
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
    public void testRemoveSpecificFromRange() throws IOException {
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.100\" end=\"192.168.1.200\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" +
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.100\" end=\"192.168.1.119\"/>\n" + 
        "        <range begin=\"192.168.1.121\" end=\"192.168.1.200\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <specific>192.168.1.120</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
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
    
    @Test
    public void testRemoveSpecificNearEndOfRange() throws IOException {
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.100\" end=\"192.168.1.200\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" +
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.100\" end=\"192.168.1.198\"/>\n" + 
        "        <specific>192.168.1.200</specific>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <specific>192.168.1.199</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());


        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.1.199");

        SnmpPeerFactory.getInstance().define(info);
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
        
    }

    @Test
    public void testRemoveSpecificAtEndOfRange() throws IOException {
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.100\" end=\"192.168.1.200\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        String expectedConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" +
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.100\" end=\"192.168.1.199\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <specific>192.168.1.200</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());


        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.1.200");

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
    public void testRecombineSpecificIntoRange() throws IOException {
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.10\" end=\"192.168.1.14\"/>\n" + 
        "        <range begin=\"192.168.1.16\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <specific>192.168.1.15</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.10\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";    

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
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
    public void testRemoveSpecificInSeparateDefWithNewRange() throws IOException {
        
        String snmpConfigXml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        		"<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        		"    <definition version=\"v2c\">\n" + 
        		"        <specific>192.168.1.30</specific>\n" + 
        		"        <specific>10.1.1.1</specific>\n" + 
        		"    </definition>\n" + 
        		"</snmp-config>\n" + 
        		"";

        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific>10.1.1.1</specific>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <range begin=\"192.168.1.15\" end=\"192.168.1.35\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
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
    
    @Test
    public void testRemoveTrivialEntry() throws IOException {
        
        String snmpConfigXml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
                "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
                "    <definition version=\"v2c\">\n" + 
                "        <specific>192.168.1.30</specific>\n" + 
                "        <specific>10.1.1.1</specific>\n" + 
                "    </definition>\n" + 
                "</snmp-config>\n" + 
                "";

        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific>10.1.1.1</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());


        SnmpEventInfo info = new SnmpEventInfo();
        info.setFirstIPAddress("192.168.1.30");

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
    public void testInvalidRange() throws IOException {
        String snmpConfigXml = "<?xml version=\"1.0\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "   <definition version=\"v2c\">\n" + 
        "       <range begin=\"192.168.0.3\" end=\"192.168.0.100\"/>" + 
        "   </definition>\n" + 
        "\n" + 
        "</snmp-config>\n" + 
        "";
                
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));

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
    public void testNewSpecifcSameAsBeginInOldDef() throws IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.0.3\" end=\"192.168.0.100\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.0.4\" end=\"192.168.0.100\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <specific>192.168.0.3</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
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
    public void testOverlapsTwoRanges() throws IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        		"<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        		"    <definition version=\"v2c\">\n" + 
        		"        <range begin=\"192.168.1.10\" end=\"192.168.1.20\"/>\n" + 
        		"        <range begin=\"192.168.1.30\" end=\"192.168.1.40\"/>\n" + 
        		"    </definition>\n" + 
        		"</snmp-config>\n" + 
        		"";
        
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.10\" end=\"192.168.1.14\"/>\n" + 
        "        <range begin=\"192.168.1.36\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <range begin=\"192.168.1.15\" end=\"192.168.1.35\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());


        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.1.15");
        info.setLastIPAddress("192.168.1.35");

        SnmpPeerFactory.getInstance().define(info);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
    }

    public void testOverlapsTwoRangesAndCombinesThem(String firstIp, String lastIp) throws IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
                "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
                "    <definition version=\"v2c\">\n" + 
                "        <range begin=\"192.168.1.10\" end=\"192.168.1.20\"/>\n" + 
                "        <range begin=\"192.168.1.30\" end=\"192.168.1.40\"/>\n" + 
                "    </definition>\n" + 
                "</snmp-config>\n" + 
                "";
        
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.10\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
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
    public void testRecombineRanges() throws IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.10\" end=\"192.168.1.14\"/>\n" + 
        "        <range begin=\"192.168.1.36\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <range begin=\"192.168.1.15\" end=\"192.168.1.35\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.10\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";            
        
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
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
    public void testRecombineRangesNonAdjacentRange() throws IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.10\" end=\"192.168.1.14\"/>\n" + 
        "        <range begin=\"192.168.1.36\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <range begin=\"192.168.1.15\" end=\"192.168.1.35\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <range begin=\"192.168.1.10\" end=\"192.168.1.40\"/>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
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
    public final void testAddNewSpecificToConfig() throws IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific>192.168.0.5</specific>\n" + 
        "        <specific>192.168.0.6</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
        String expectedConfig =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific>192.168.0.5</specific>\n" + 
        "    </definition>\n" + 
        "    <definition version=\"v1\">\n" + 
        "        <specific>192.168.0.6</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
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
    public final void testOptimizeAllDefs() throws IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific>192.168.0.5</specific>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"opennmsrules\">\n" + 
        "        <range begin=\"192.168.100.1\" end=\"192.168.100.254\"/>\n" + 
        "        <range begin=\"192.168.101.1\" end=\"192.168.101.254\"/>\n" + 
        "        <range begin=\"192.168.102.1\" end=\"192.168.102.254\"/>\n" + 
        "        <range begin=\"192.168.103.1\" end=\"192.168.103.254\"/>\n" + 
        "        <range begin=\"192.168.104.1\" end=\"192.168.104.254\"/>\n" + 
        "        <range begin=\"192.168.105.1\" end=\"192.168.105.254\"/>\n" + 
        "        <range begin=\"192.168.106.1\" end=\"192.168.106.254\"/>\n" + 
        "        <range begin=\"192.168.107.1\" end=\"192.168.107.254\"/>\n" + 
        "        <range begin=\"192.168.0.1\" end=\"192.168.0.10\"/>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"opennmsrules2\">\n" + 
        "        <range begin=\"192.168.100.0\" end=\"192.168.100.255\"/>\n" + 
        "        <range begin=\"192.168.101.0\" end=\"192.168.101.255\"/>\n" + 
        "        <range begin=\"192.168.102.0\" end=\"192.168.102.255\"/>\n" + 
        "        <range begin=\"192.168.103.0\" end=\"192.168.103.255\"/>\n" + 
        "        <range begin=\"192.168.104.0\" end=\"192.168.104.255\"/>\n" + 
        "        <range begin=\"192.168.105.0\" end=\"192.168.105.255\"/>\n" + 
        "        <range begin=\"192.168.106.0\" end=\"192.168.106.255\"/>\n" + 
        "        <range begin=\"192.168.107.0\" end=\"192.168.107.255\"/>\n" + 
        "        <range begin=\"192.168.0.1\" end=\"192.168.0.10\"/>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"splice-test\" version=\"v2c\">\n" + 
        "        <range begin=\"10.1.2.1\" end=\"10.1.2.100\"/>\n" + 
        "        <range begin=\"11.1.2.1\" end=\"11.1.2.100\"/>\n" + 
        "        <range begin=\"12.1.2.1\" end=\"12.1.2.100\"/>\n" + 
        "        <specific>10.1.1.1</specific>\n" + 
        "        <specific>10.1.1.2</specific>\n" + 
        "        <specific>10.1.1.3</specific>\n" + 
        "        <specific>10.1.1.5</specific>\n" + 
        "        <specific>10.1.1.6</specific>\n" + 
        "        <specific>10.1.1.10</specific>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"splice2-test\">\n" + 
        "        <range begin=\"10.1.1.11\" end=\"10.1.1.100\"/>\n" + 
        "        <range begin=\"11.1.2.1\" end=\"11.1.2.100\"/>\n" + 
        "        <range begin=\"12.1.2.1\" end=\"12.1.2.100\"/>\n" + 
        "        <specific>10.1.1.10</specific>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"splice3-test\">\n" + 
        "        <range begin=\"10.1.1.11\" end=\"10.1.1.100\"/>\n" + 
        "        <range begin=\"11.1.2.1\" end=\"11.1.2.1\"/>\n" + 
        "        <range begin=\"12.1.2.1\" end=\"12.1.2.1\"/>\n" + 
        "        <specific>10.1.1.10</specific>\n" + 
        "        <specific>10.1.1.12</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";
        
 
        String expectedConfig = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?> standalone=\"yes\"\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\" read-community=\"public\" write-community=\"private\" xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\">\n" + 
        "    <definition version=\"v2c\">\n" + 
        "        <specific>192.168.0.5</specific>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"opennmsrules\">\n" + 
        "        <range begin=\"192.168.0.1\" end=\"192.168.0.10\"/>\n" + 
        "        <range begin=\"192.168.100.1\" end=\"192.168.100.254\"/>\n" + 
        "        <range begin=\"192.168.101.1\" end=\"192.168.101.254\"/>\n" + 
        "        <range begin=\"192.168.102.1\" end=\"192.168.102.254\"/>\n" + 
        "        <range begin=\"192.168.103.1\" end=\"192.168.103.254\"/>\n" + 
        "        <range begin=\"192.168.104.1\" end=\"192.168.104.254\"/>\n" + 
        "        <range begin=\"192.168.105.1\" end=\"192.168.105.254\"/>\n" + 
        "        <range begin=\"192.168.106.1\" end=\"192.168.106.254\"/>\n" + 
        "        <range begin=\"192.168.107.1\" end=\"192.168.107.254\"/>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"opennmsrules2\">\n" + 
        "        <range begin=\"192.168.0.1\" end=\"192.168.0.10\"/>\n" + 
        "        <range begin=\"192.168.100.0\" end=\"192.168.107.255\"/>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"splice-test\" version=\"v2c\">\n" + 
        "        <range begin=\"10.1.1.1\" end=\"10.1.1.3\"/>\n" + 
        "        <range begin=\"10.1.1.5\" end=\"10.1.1.6\"/>\n" + 
        "        <range begin=\"10.1.2.1\" end=\"10.1.2.100\"/>\n" + 
        "        <range begin=\"11.1.2.1\" end=\"11.1.2.100\"/>\n" + 
        "        <range begin=\"12.1.2.1\" end=\"12.1.2.100\"/>\n" + 
        "        <specific>10.1.1.10</specific>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"splice2-test\">\n" + 
        "        <range begin=\"10.1.1.10\" end=\"10.1.1.100\"/>\n" + 
        "        <range begin=\"11.1.2.1\" end=\"11.1.2.100\"/>\n" + 
        "        <range begin=\"12.1.2.1\" end=\"12.1.2.100\"/>\n" + 
        "    </definition>\n" + 
        "    <definition read-community=\"splice3-test\">\n" + 
        "        <range begin=\"10.1.1.10\" end=\"10.1.1.100\"/>\n" + 
        "        <specific>11.1.2.1</specific>\n" + 
        "        <specific>12.1.2.1</specific>\n" + 
        "    </definition>\n" + 
        "</snmp-config>\n" + 
        "";

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(snmpConfigXml));
        
        assertEquals(snmpConfigXml, SnmpPeerFactory.marshallConfig());

        SnmpEventInfo info = new SnmpEventInfo();
        
        SnmpConfig config = SnmpPeerFactory.getSnmpConfig();
        assertEquals(6, config.getDefinitionCount());
        
        info.setCommunityString("opennmsrules2");
        
        //do bunch more...
        
        new SnmpConfigManager(config).optimizeAllDefs();
//      String config = SnmpPeerFactory.marshallConfig();
//      System.err.println(config);
        
        String actualConfig = SnmpPeerFactory.marshallConfig();
        assertEquals(expectedConfig, actualConfig);
        

    }
    
    @Test
    @Ignore
    public void testAddSpecificToBigFile() throws Exception {
        Resource res = new FileSystemResource("/Users/brozow/big-snmp-config.xml");
        
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(res));

        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setCommunityString("th3l04n3r");
        info.setFirstIPAddress("192.168.1.15");
        info.setLastIPAddress("192.168.1.35");
        
        SnmpPeerFactory.getInstance().define(info);

        String actualConfig = SnmpPeerFactory.marshallConfig();
        System.err.println(actualConfig);
        //assertEquals(expectedConfig, actualConfig);
        

    }

    @Test
    @Ignore
    public void testConfigTheHeckOutOfIt() throws Exception {
        Resource configResource = new FileSystemResource("/Users/brozow/big-snmp-config.xml");
        Resource events = new FileSystemResource("/Users/brozow/support/NEN/palin/wave-events.txt");
        File configDir = new File("/Users/brozow/support/NEN/palin/configs");
        configDir.mkdirs();
        
        SnmpEventInfo[] updates = readEventInfo(events.getInputStream());
        assertNotNull(updates);

        SnmpPeerFactory.setInstance(new SnmpPeerFactory(configResource));

        
        int index = 0;
        for (SnmpEventInfo update : updates) {
            try {
                File dir = new File(configDir, String.format("%03d", index));
                dir.mkdirs();
                SnmpPeerFactory.saveToFile(new File(dir, "pre-config.xml"));
                SnmpPeerFactory.getInstance().define(update);
                File saveUpdate = new File(dir, "update");
                FileUtils.writeStringToFile(saveUpdate, String.format("%s %s %s\n", update.getFirstIPAddress(), update.getLastIPAddress(), update.getCommunityString()));
                SnmpPeerFactory.saveToFile(new File(dir, "post-config.xml"));
            } catch (Exception e) {
                e.printStackTrace();
                fail(String.format("Applying update with index %d change community string for %s  to %s failed.", index, update.getFirstIPAddress(), update.getCommunityString()));
            }
            index++;
        }

        String actualConfig = SnmpPeerFactory.marshallConfig();
        System.err.println(actualConfig);
        //assertEquals(expectedConfig, actualConfig);
        

    }
    
    private SnmpEventInfo[] readEventInfo(InputStream in) throws UnknownHostException {
        List<SnmpEventInfo> updates = new ArrayList<SnmpEventInfo>(500);
        Scanner s = new Scanner(in);
        int lineCount = 0;
        while(s.hasNextLine()) {
            lineCount++;
            s.findInLine("\\s*firstIPAddress=([0-9.]+) communityString=([^ ]*) lastIPAddress=([0-9.]+)\\s*");
            MatchResult result = s.match();
            System.out.printf("%d: %s - %s: %s\n", lineCount, result.group(1), result.group(3), result.group(2));
            SnmpEventInfo info = new SnmpEventInfo();
            info.setFirstIPAddress(result.group(1));
            info.setLastIPAddress(result.group(3));
            info.setCommunityString(result.group(2));
            updates.add(info);
            s.nextLine();
        }
        s.close();
        return updates.toArray(new SnmpEventInfo[0]);

    }

}
