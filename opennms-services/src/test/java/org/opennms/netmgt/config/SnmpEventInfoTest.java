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

import java.io.IOException;
import java.net.InetAddress;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.test.ConfigurationTestUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 * JUnit tests for the configureSNMP event handling and optimization of
 * the SNMP configuration XML.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 *
 */
public class SnmpEventInfoTest extends TestCase {
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        Resource rdr = ConfigurationTestUtils.getSpringResourceForResource(this, "snmp-config-snmpEventInfoTest.xml");
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
    }
    
    /**
     * This tests the ability of a configureSNMP event to change the community
     * string of a specific address.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    public final void testModifySpecificInDef() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\"\n" + 
        "   read-community=\"public\" write-community=\"private\">\n" + 
        "   <definition version=\"v2c\">\n" + 
        "       <specific>192.168.0.5</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</snmp-config>\n" + 
        "";

        Resource rdr = new ByteArrayResource(snmpConfigXml.getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpEventInfo info = new SnmpEventInfo();
        info.setCommunityString("abc");
        info.setFirstIPAddress("192.168.0.5");
        
        MergeableDefinition configDef = new MergeableDefinition(SnmpPeerFactory.getSnmpConfig().getDefinition(0));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());
        MergeableDefinition matchingDef = mgr.findDefMatchingAttributes(info.createDef());
        assertNull(matchingDef);
        assertTrue(configDef.hasMatchingSpecific(info.getFirstIPAddress()));
        assertNull(configDef.getConfigDef().getReadCommunity());
        
        mgr.mergeIntoConfig(info.createDef());
        
//      String config = SnmpPeerFactory.marshallConfig();
//      System.err.println(config);
        
        matchingDef = mgr.findDefMatchingAttributes(info.createDef());
        assertNotNull(matchingDef);
        assertFalse(configDef.hasMatchingSpecific(info.getFirstIPAddress()));
        assertTrue(matchingDef.hasMatchingSpecific(info.getFirstIPAddress()));
        assertEquals(InetAddressUtils.toIpAddrLong(InetAddress.getByName("192.168.0.5")), InetAddressUtils.toIpAddrLong(InetAddress.getByName(matchingDef.getConfigDef().getSpecific(0))));
        assertEquals("abc", matchingDef.getConfigDef().getReadCommunity());
        assertEquals(1, SnmpPeerFactory.getSnmpConfig().getDefinitionCount());
    }

    /**
     * This tests the merging of a new specific into a definition that already contains a specific
     * that is adjacent.  The two specifics should be converted to a single range in the definition.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    public final void testAddAdjacentSpecificToDef() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\"\n" + 
        "   read-community=\"public\" write-community=\"private\">\n" + 
        "   <definition version=\"v2c\">\n" + 
        "       <specific>192.168.0.5</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</snmp-config>\n" + 
        "";

        Resource rdr = new ByteArrayResource(snmpConfigXml.getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());
        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress("192.168.0.6");
        
        MergeableDefinition configDef = new MergeableDefinition(SnmpPeerFactory.getSnmpConfig().getDefinition(0));
        MergeableDefinition matchingDef = mgr.findDefMatchingAttributes(info.createDef());
        assertNotNull(matchingDef);
        assertFalse(matchingDef.hasMatchingSpecific(info.getFirstIPAddress()));
        assertEquals(1, matchingDef.getConfigDef().getSpecificCount());
        assertEquals(0, matchingDef.getConfigDef().getRangeCount());
        assertNull(configDef.getConfigDef().getReadCommunity());
        
        mgr.mergeIntoConfig(info.createDef());
//      String config = SnmpPeerFactory.marshallConfig();
//      System.err.println(config);
        
        assertEquals(matchingDef.getConfigDef(), mgr.findDefMatchingAttributes(info.createDef()).getConfigDef());
        assertFalse(matchingDef.hasMatchingSpecific(info.getFirstIPAddress()));
        assertEquals(0, matchingDef.getConfigDef().getSpecificCount());
        assertEquals(1, matchingDef.getConfigDef().getRangeCount());
        assertEquals("192.168.0.5", matchingDef.getConfigDef().getRange(0).getBegin());
        assertEquals("192.168.0.6", matchingDef.getConfigDef().getRange(0).getEnd());
        assertNull(configDef.getConfigDef().getReadCommunity());
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
    public void testSplitRange() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\"\n" + 
        "   read-community=\"public\" write-community=\"private\">\n" + 
        "   <definition version=\"v2c\">\n" + 
        "       <range begin=\"192.168.1.100\" end=\"192.168.1.200\"/>" + 
        "   </definition>\n" + 
        "\n" + 
        "</snmp-config>\n" + 
        "";

        Resource rdr = new ByteArrayResource(snmpConfigXml.getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());

        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.1.120");
        info.setLastIPAddress("192.168.1.130");

        mgr.mergeIntoConfig(info.createDef());
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        assertEquals(2, mgr.getConfig().getDefinitionCount());
        assertEquals("192.168.1.100", mgr.getConfig().getDefinition(0).getRange(0).getBegin());
        assertEquals("192.168.1.119", mgr.getConfig().getDefinition(0).getRange(0).getEnd());
        
        assertEquals("192.168.1.131", mgr.getConfig().getDefinition(0).getRange(1).getBegin());
        assertEquals("192.168.1.200", mgr.getConfig().getDefinition(0).getRange(1).getEnd());
        
        assertEquals("192.168.1.120", mgr.getConfig().getDefinition(1).getRange(0).getBegin());
        assertEquals("192.168.1.130", mgr.getConfig().getDefinition(1).getRange(0).getEnd());
        
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
    public void testRemoveSpecificFromRange() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\"\n" + 
        "   read-community=\"public\" write-community=\"private\">\n" + 
        "   <definition version=\"v2c\">\n" + 
        "       <range begin=\"192.168.1.100\" end=\"192.168.1.200\"/>" + 
        "   </definition>\n" + 
        "\n" + 
        "</snmp-config>\n" + 
        "";

        Resource rdr = new ByteArrayResource(snmpConfigXml.getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());

        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.1.120");

        mgr.mergeIntoConfig(info.createDef());
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        assertEquals(2, mgr.getConfig().getDefinitionCount());
        
        assertEquals(2, mgr.getConfig().getDefinition(0).getRangeCount());
        assertEquals(0, mgr.getConfig().getDefinition(0).getSpecificCount());
        
        assertEquals("192.168.1.100", mgr.getConfig().getDefinition(0).getRange(0).getBegin());
        assertEquals("192.168.1.119", mgr.getConfig().getDefinition(0).getRange(0).getEnd());
        
        assertEquals("192.168.1.121", mgr.getConfig().getDefinition(0).getRange(1).getBegin());
        assertEquals("192.168.1.200", mgr.getConfig().getDefinition(0).getRange(1).getEnd());
        
        assertEquals(0, mgr.getConfig().getDefinition(1).getRangeCount());
        assertEquals(1, mgr.getConfig().getDefinition(1).getSpecificCount());
        
        assertEquals("192.168.1.120", mgr.getConfig().getDefinition(1).getSpecific(0));
        
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
    public void testRecombineSpecificIntoRange() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\"\n" + 
        "   read-community=\"public\" write-community=\"private\">\n" + 
        "   <definition version=\"v2c\">\n" + 
        "       <range begin=\"192.168.1.10\" end=\"192.168.1.14\"/>" + 
        "       <range begin=\"192.168.1.16\" end=\"192.168.1.40\"/>" + 
        "   </definition>\n" + 
        "   <definition version=\"v1\">\n" + 
        "       <specific>192.168.1.15</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</snmp-config>\n" + 
        "";

        Resource rdr = new ByteArrayResource(snmpConfigXml.getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());

        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress("192.168.1.15");

        mgr.mergeIntoConfig(info.createDef());
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        assertEquals(1, mgr.getConfig().getDefinitionCount());
        
        assertEquals(1, mgr.getConfig().getDefinition(0).getRangeCount());
        assertEquals(0, mgr.getConfig().getDefinition(0).getSpecificCount());
        
        assertEquals("192.168.1.10", mgr.getConfig().getDefinition(0).getRange(0).getBegin());
        assertEquals("192.168.1.40", mgr.getConfig().getDefinition(0).getRange(0).getEnd());
        
    }
    
    /**
     * This tests the ability to remove a specific IP from one definition with a newly specified range.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    public void testRemoveSpecificInSeparateDefWithNewRange() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\"\n" + 
        "   read-community=\"public\" write-community=\"private\">\n" + 
        "   <definition version=\"v2c\">\n" +
        "       <specific>192.168.1.30</specific>" +
        "       <specific>10.1.1.1</specific>" + 
        "   </definition>\n" + 
        "\n" + 
        "</snmp-config>\n" + 
        "";

        Resource rdr = new ByteArrayResource(snmpConfigXml.getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());

        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.1.15");
        info.setLastIPAddress("192.168.1.35");

        mgr.mergeIntoConfig(info.createDef());
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        assertEquals(2, mgr.getConfig().getDefinitionCount());
        assertEquals(0, mgr.getConfig().getDefinition(0).getRangeCount());
        assertEquals(1, mgr.getConfig().getDefinition(0).getSpecificCount());
        assertEquals(1, mgr.getConfig().getDefinition(1).getRangeCount());
        assertEquals(0, mgr.getConfig().getDefinition(1).getSpecificCount());
        
        assertEquals("10.1.1.1", mgr.getConfig().getDefinition(0).getSpecific(0));
        
        assertEquals("192.168.1.15", mgr.getConfig().getDefinition(1).getRange(0).getBegin());
        assertEquals("192.168.1.35", mgr.getConfig().getDefinition(1).getRange(0).getEnd());
        
    }
    
    /**
     * This tests the behavior of specifying an invalid range.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
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
        
        Resource rdr = new ByteArrayResource(snmpConfigXml.getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());

        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.0.3");
        info.setLastIPAddress("192.168.0.1");

        try {
            mgr.mergeIntoConfig(info.createDef());
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
    public void testNewSpecifcSameAsBeginInOldDef() throws MarshalException, ValidationException, IOException {
        String snmpConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\"\n" + 
        "   read-community=\"public\" write-community=\"private\">\n" + 
        "   <definition version=\"v2c\">\n" + 
        "       <range begin=\"192.168.0.3\" end=\"192.168.0.100\"/>" + 
        "   </definition>\n" + 
        "\n" + 
        "</snmp-config>\n" + 
        "";
        
        Resource rdr = new ByteArrayResource(snmpConfigXml.getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());

        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.0.3");

        mgr.mergeIntoConfig(info.createDef());
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        assertEquals(2, mgr.getConfig().getDefinitionCount());
        assertEquals(1, mgr.getConfig().getDefinition(0).getRangeCount());
        assertEquals(0, mgr.getConfig().getDefinition(0).getSpecificCount());
        
        assertEquals(0, mgr.getConfig().getDefinition(1).getRangeCount());
        assertEquals(1, mgr.getConfig().getDefinition(1).getSpecificCount());
        
        assertEquals("192.168.0.4", mgr.getConfig().getDefinition(0).getRange(0).getBegin());
        assertEquals("192.168.0.100", mgr.getConfig().getDefinition(0).getRange(0).getEnd());
        
        assertEquals("192.168.0.3", mgr.getConfig().getDefinition(1).getSpecific(0));

    }
    
    /**
     * This tests the merging of a new definition that contains a range of IP addresses that overlaps
     * the end of one range and the beginning of another range in a current definition.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    public void testOverlapsTwoRanges() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\"\n" + 
        "   read-community=\"public\" write-community=\"private\">\n" + 
        "   <definition version=\"v2c\">\n" + 
        "       <range begin=\"192.168.1.10\" end=\"192.168.1.20\"/>" + 
        "       <range begin=\"192.168.1.30\" end=\"192.168.1.40\"/>" + 
        "   </definition>\n" + 
        "\n" + 
        "</snmp-config>\n" + 
        "";

        Resource rdr = new ByteArrayResource(snmpConfigXml.getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());

        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.1.15");
        info.setLastIPAddress("192.168.1.35");

        mgr.mergeIntoConfig(info.createDef());
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        
        assertEquals(2, mgr.getConfig().getDefinitionCount());
        
        assertEquals("192.168.1.10", mgr.getConfig().getDefinition(0).getRange(0).getBegin());
        assertEquals("192.168.1.14", mgr.getConfig().getDefinition(0).getRange(0).getEnd());
        
        assertEquals("192.168.1.36", mgr.getConfig().getDefinition(0).getRange(1).getBegin());
        assertEquals("192.168.1.40", mgr.getConfig().getDefinition(0).getRange(1).getEnd());
        
        assertEquals("192.168.1.15", mgr.getConfig().getDefinition(1).getRange(0).getBegin());
        assertEquals("192.168.1.35", mgr.getConfig().getDefinition(1).getRange(0).getEnd());
        
    }

    /**
     * This tests moving a range that from one defintion into another defintion for which it overlaps
     * one range in the merging definition and creates 2 adjacent ranges that should be merged together.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    public void testRecombineRanges() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\"\n" + 
        "   read-community=\"public\" write-community=\"private\">\n" + 
        "   <definition version=\"v2c\">\n" + 
        "       <range begin=\"192.168.1.10\" end=\"192.168.1.14\"/>" + 
        "       <range begin=\"192.168.1.36\" end=\"192.168.1.40\"/>" + 
        "   </definition>\n" + 
        "   <definition version=\"v1\">\n" + 
        "       <range begin=\"192.168.1.15\" end=\"192.168.1.35\"/>" + 
        "   </definition>\n" + 
        "\n" + 
        "</snmp-config>\n" + 
        "";

        Resource rdr = new ByteArrayResource(snmpConfigXml.getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());

        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress("192.168.1.15");
        info.setLastIPAddress("192.168.1.35");

        mgr.mergeIntoConfig(info.createDef());
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        
        assertEquals(1, mgr.getConfig().getDefinitionCount());
        
        assertEquals("192.168.1.10", mgr.getConfig().getDefinition(0).getRange(0).getBegin());
        assertEquals("192.168.1.40", mgr.getConfig().getDefinition(0).getRange(0).getEnd());
        
        
    }
    
    /**
     * This tests recombing a range into an existing defintion for which the new range overlaps the current definition's 
     * range and overlaps the end of one range and the beginning of another range of the merging definition.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    public void testRecombineRangesNonAdjacentRange() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\"\n" + 
        "   read-community=\"public\" write-community=\"private\">\n" + 
        "   <definition version=\"v2c\">\n" + 
        "       <range begin=\"192.168.1.10\" end=\"192.168.1.14\"/>" + 
        "       <range begin=\"192.168.1.36\" end=\"192.168.1.40\"/>" + 
        "   </definition>\n" + 
        "   <definition version=\"v1\">\n" + 
        "       <range begin=\"192.168.1.15\" end=\"192.168.1.35\"/>" + 
        "   </definition>\n" + 
        "\n" + 
        "</snmp-config>\n" + 
        "";

        Resource rdr = new ByteArrayResource(snmpConfigXml.getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());

        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v2c");
        info.setFirstIPAddress("192.168.1.12");
        info.setLastIPAddress("192.168.1.38");

        mgr.mergeIntoConfig(info.createDef());
        
//        String config = SnmpPeerFactory.marshallConfig();
//        System.err.println(config);
        
        
        assertEquals(1, mgr.getConfig().getDefinitionCount());
        
        assertEquals("192.168.1.10", mgr.getConfig().getDefinition(0).getRange(0).getBegin());
        assertEquals("192.168.1.40", mgr.getConfig().getDefinition(0).getRange(0).getEnd());
        
        
    }
    
    /**
     * This tests moving the specific from a current defition to a new defintion.
     * 
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException 
     */
    public final void testAddNewSpecificToConfig() throws MarshalException, ValidationException, IOException {
        
        String snmpConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<snmp-config retry=\"3\" timeout=\"800\"\n" + 
        "   read-community=\"public\" write-community=\"private\">\n" + 
        "   <definition version=\"v2c\">\n" + 
        "       <specific>192.168.0.5</specific>\n" + 
        "       <specific>192.168.0.6</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</snmp-config>\n" + 
        "";

        Resource rdr = new ByteArrayResource(snmpConfigXml.getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());
        
        SnmpEventInfo info = new SnmpEventInfo();
        info.setVersion("v1");
        info.setFirstIPAddress("192.168.0.6");
        
        MergeableDefinition configDef = new MergeableDefinition(SnmpPeerFactory.getSnmpConfig().getDefinition(0));
        MergeableDefinition matchingDef = mgr.findDefMatchingAttributes(info.createDef());
        assertNull(matchingDef);
        assertTrue(configDef.hasMatchingSpecific(info.getFirstIPAddress()));
        assertEquals(2, configDef.getConfigDef().getSpecificCount());
        assertEquals(0, configDef.getConfigDef().getRangeCount());
        assertNull(configDef.getConfigDef().getReadCommunity());
        assertEquals("v2c", configDef.getConfigDef().getVersion());
        
        mgr.mergeIntoConfig(info.createDef());
//      String config = SnmpPeerFactory.marshallConfig();
//      System.err.println(config);
        
        matchingDef = mgr.findDefMatchingAttributes(info.createDef());
        assertNotNull(matchingDef);
        assertFalse(configDef.hasMatchingSpecific(info.getFirstIPAddress()));
        assertEquals(1, configDef.getConfigDef().getSpecificCount());
        assertEquals(0, configDef.getConfigDef().getRangeCount());
        assertEquals("v2c", configDef.getConfigDef().getVersion());
        
        assertEquals(1, matchingDef.getConfigDef().getSpecificCount());
        assertEquals(0, matchingDef.getConfigDef().getRangeCount());
        assertTrue(matchingDef.hasMatchingSpecific(info.getFirstIPAddress()));
        assertNull(matchingDef.getConfigDef().getReadCommunity());
        assertEquals("v1", matchingDef.getConfigDef().getVersion());
        
        assertTrue(matchingDef.getConfigDef() != configDef.getConfigDef());
        assertEquals(2, SnmpPeerFactory.getSnmpConfig().getDefinitionCount());
        
    }

    /**
     * Test method for {@link org.opennms.netmgt.config.SnmpEventInfo#optimizeAllDefs()}.
     * @throws ValidationException 
     * @throws MarshalException 
     * @throws IOException 
     */
    public final void testOptimizeAllDefs() throws MarshalException, ValidationException, IOException {

        MergeableDefinition def;
        
        SnmpConfigManager mgr = new SnmpConfigManager(SnmpPeerFactory.getSnmpConfig());
        
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
        
        def = mgr.findDefMatchingAttributes(info.createDef());
        assertNotNull(def);
        assertEquals(2, def.getConfigDef().getRangeCount());
        

    }

}
