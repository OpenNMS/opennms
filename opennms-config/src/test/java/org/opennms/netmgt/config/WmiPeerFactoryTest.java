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

package org.opennms.netmgt.config;

import java.io.IOException;
import java.net.InetAddress;

import junit.framework.TestCase;

import org.opennms.core.test.ConfigurationTestUtils;

/**
 * JUnit tests for the configureSNMP event handling and optimization of
 * the SNMP configuration XML.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 *
 */
public class WmiPeerFactoryTest extends TestCase {
    private WmiPeerFactory getFactory(String amiConfigXml) throws IOException {
        WmiPeerFactory factory = new WmiPeerFactory(ConfigurationTestUtils.getResourceForConfigWithReplacements(amiConfigXml));
        factory.afterPropertiesSet();
        return factory;
    }

    /**
     * @throws IOException 
     */
    public final void testOneSpecific() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <specific>192.168.0.5</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinitions().size());
    }

    /**
     * This tests the merging of a new specific into a definition that already contains a specific
     * that is adjacent.  The two specifics should be converted to a single range in the definition.
     *
     * @throws IOException 
     */
    public final void testAddAdjacentSpecificToDef() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <specific>192.168.0.5</specific>\n" + 
        "       <specific>192.168.0.6</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(2, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getRanges().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(1, factory.getConfig().getDefinitions().get(0).getRanges().size());
        assertEquals("192.168.0.5", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getBegin());
        assertEquals("192.168.0.6", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getEnd());
    }

    public final void testAddAdjacentSpecificToDefIPv6() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:fedb</specific>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:fedc</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(2, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getRanges().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(1, factory.getConfig().getDefinitions().get(0).getRanges().size());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedb", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getBegin());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedc", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getEnd());
    }

    public final void testAddAdjacentSpecificToDefIPv6WithSameScopeId() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:fedb%5</specific>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:fedc%5</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(2, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getRanges().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(1, factory.getConfig().getDefinitions().get(0).getRanges().size());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedb%5", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getBegin());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedc%5", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getEnd());
    }

    public final void testAddAdjacentSpecificToDefIPv6WithDifferentScopeIds() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:fedb%1</specific>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:fedc%2</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(2, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getRanges().size());

        factory.optimize();

        // No optimization should occur because the addresses have different scope IDs
        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(2, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getRanges().size());
    }

    /**
     * This tests the ability to move a specific from one definition into a range of another definition.  The
     * results should be that the 2 ranges in the first definition are recombined into a single range based on 
     * the single IP address that was in a different existing definition that will now be removed and the definition
     * deleted.
     *
     * @throws IOException 
     */
    public void testRecombineSpecificIntoRange() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <range begin=\"fe80:0000:0000:0000:0000:0000:0000:fed0%1\" end=\"fe80:0000:0000:0000:0000:0000:0000:fed9%1\"/>\n" + 
        "       <range begin=\"fe80:0000:0000:0000:0000:0000:0000:fedb%1\" end=\"fe80:0000:0000:0000:0000:0000:0000:fedf%1\"/>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:feda%1</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(1, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(2, factory.getConfig().getDefinitions().get(0).getRanges().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(1, factory.getConfig().getDefinitions().get(0).getRanges().size());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fed0%1", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getBegin());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedf%1", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getEnd());
    }

    /**
     * This tests the ability to move a specific from one definition into a range of another definition.  The
     * results should be that the 2 ranges in the first definition are recombined into a single range based on 
     * the single IP address that was in a different existing definition that will now be removed and the definition
     * deleted.
     *
     * @throws IOException 
     */
    public void testRecombineSpecificIntoRangeWithDifferentScopeIds() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <range begin=\"fe80:0000:0000:0000:0000:0000:0000:fed0%1\" end=\"fe80:0000:0000:0000:0000:0000:0000:fed9%1\"/>\n" + 
        "       <range begin=\"fe80:0000:0000:0000:0000:0000:0000:fedb%2\" end=\"fe80:0000:0000:0000:0000:0000:0000:fedf%2\"/>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:feda%1</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(1, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(2, factory.getConfig().getDefinitions().get(0).getRanges().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(2, factory.getConfig().getDefinitions().get(0).getRanges().size());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fed0%1", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getBegin());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:feda%1", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getEnd());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedb%2", factory.getConfig().getDefinitions().get(0).getRanges().get(1).getBegin());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedf%2", factory.getConfig().getDefinitions().get(0).getRanges().get(1).getEnd());
    }

    /**
     * This tests the addition of a new specific definition that is the same address as the beginning of
     * a range in a current definition.
     *
     * @throws IOException 
     */
    public final void testNewSpecificSameAsBeginInOldDef() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <range begin=\"192.168.0.6\" end=\"192.168.0.12\"/>\n" + 
        "       <specific>192.168.0.6</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(1, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(1, factory.getConfig().getDefinitions().get(0).getRanges().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(1, factory.getConfig().getDefinitions().get(0).getRanges().size());
        assertEquals("192.168.0.6", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getBegin());
        assertEquals("192.168.0.12", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getEnd());
    }

    /**
     * This tests the addition of a new specific definition that is the same address as the beginning of
     * a range in a current definition.
     *
     * @throws IOException 
     */
    public final void testNewSpecificSameAsEndInOldDef() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <range begin=\"192.168.0.6\" end=\"192.168.0.12\"/>\n" + 
        "       <specific>192.168.0.12</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(1, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(1, factory.getConfig().getDefinitions().get(0).getRanges().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(1, factory.getConfig().getDefinitions().get(0).getRanges().size());
        assertEquals("192.168.0.6", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getBegin());
        assertEquals("192.168.0.12", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getEnd());
    }

    /**
     * This tests the merging of a new definition that contains a range of IP addresses that overlaps
     * the end of one range and the beginning of another range in a current definition.
     *
     * @throws IOException 
     */
    public void testOverlapsTwoRanges() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <range begin=\"192.168.0.6\" end=\"192.168.0.12\"/>\n" + 
        "       <range begin=\"192.168.0.20\" end=\"192.168.0.100\"/>\n" + 
        "       <range begin=\"192.168.0.8\" end=\"192.168.0.30\"/>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(3, factory.getConfig().getDefinitions().get(0).getRanges().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinitions().size());
        assertEquals(0, factory.getConfig().getDefinitions().get(0).getSpecifics().size());
        assertEquals(1, factory.getConfig().getDefinitions().get(0).getRanges().size());
        assertEquals("192.168.0.6", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getBegin());
        assertEquals("192.168.0.100", factory.getConfig().getDefinitions().get(0).getRanges().get(0).getEnd());
    }

    /**
     * @throws IOException 
     */
    public final void testEncodedPassDefault() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"b2JzY3VyaXR5RlRXIQ===\">\n" + 
        "   <definition>\n" + 
        "       <specific>192.168.0.5</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinitions().size());
        
        assertEquals("obscurityFTW!", factory.getAgentConfig(InetAddress.getByName("1.1.1.1")).getPassword());
    }

    /**
     * @throws IOException 
     */
    public final void testEncodedPassDefinition() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"b2JzY3VyaXR5RlRXIQ===\">\n" + 
        "   <definition password=\"b2JzY3VyZSE9c2VjdXJl===\">\n" + 
        "       <specific>192.168.0.5</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinitions().size());
        
        assertEquals("obscure!=secure", factory.getAgentConfig(InetAddress.getByName("192.168.0.5")).getPassword());
    }
    
    /**
     * @throws IOException 
     */
    public final void testUnencodedPassDefault() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"clarityFTW!\">\n" + 
        "   <definition>\n" + 
        "       <specific>192.168.0.5</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinitions().size());
        
        assertEquals("clarityFTW!", factory.getAgentConfig(InetAddress.getByName("1.1.1.1")).getPassword());
    }

    /**
     * @throws IOException 
     */
    public final void testUnencodedPassDefinition() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<wmi-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"clarityFTW!\">\n" + 
        "   <definition password=\"aVerySecureOne\">\n" + 
        "       <specific>192.168.0.5</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</wmi-config>\n" + 
        "";

        WmiPeerFactory factory = getFactory(amiConfigXml);

        assertEquals(1, factory.getConfig().getDefinitions().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinitions().size());
        
        assertEquals("aVerySecureOne", factory.getAgentConfig(InetAddress.getByName("192.168.0.5")).getPassword());
    }

}
