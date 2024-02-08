/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.protocols.nsclient.config;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;

/**
 * JUnit tests for the configureSNMP event handling and optimization of
 * the SNMP configuration XML.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 *
 */
public class NSClientPeerFactoryTest {

    /**
     * @throws IOException 
     */
    @Test
    public final void testOneSpecific() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<nsclient-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <specific>192.168.0.5</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</nsclient-config>\n" + 
        "";

        NSClientPeerFactory factory = new NSClientPeerFactory(new ByteArrayInputStream(amiConfigXml.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, factory.getConfig().getDefinition().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinition().size());
    }

    @Test
    public final void testMetadata() throws IOException {
        final TemporaryFolder tempFolder = new TemporaryFolder();
        tempFolder.create();
        final File keystoreFile = new File(tempFolder.getRoot(), "scv.jce");
        final SecureCredentialsVault secureCredentialsVault = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "notRealPassword");
        secureCredentialsVault.setCredentials("nsclient", new Credentials("foo", "bar"));

        final String nsClientConfigXml = "<?xml version=\"1.0\"?>\n" +
                "<nsclient-config retry=\"3\" timeout=\"800\"\n" +
                "   password=\"${scv:nsclient:password}\">\n" +
                "   <definition>\n" +
                "       <specific>192.168.0.5</specific>\n" +
                "   </definition>\n" +
                "\n" +
                "</nsclient-config>\n" +
                "";

        final NSClientPeerFactory factory = new NSClientPeerFactory(new ByteArrayInputStream(nsClientConfigXml.getBytes(StandardCharsets.UTF_8)));
        factory.setSecureCredentialsVaultScope(new SecureCredentialsVaultScope(secureCredentialsVault));

        assertEquals("${scv:nsclient:password}", factory.getConfig().getPassword());
        assertEquals("bar", factory.getAgentConfig(InetAddress.getByName("192.168.0.5")).getPassword());
    }

    /**
     * This tests the merging of a new specific into a definition that already contains a specific
     * that is adjacent.  The two specifics should be converted to a single range in the definition.
     * 
     * @throws IOException 
     */
    @Test
    public final void testAddAdjacentSpecificToDef() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<nsclient-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <specific>192.168.0.5</specific>\n" + 
        "       <specific>192.168.0.6</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</nsclient-config>\n" + 
        "";

        NSClientPeerFactory factory = new NSClientPeerFactory(new ByteArrayInputStream(amiConfigXml.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(2, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getRange().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(1, factory.getConfig().getDefinition().get(0).getRange().size());
        assertEquals("192.168.0.5", factory.getConfig().getDefinition().get(0).getRange().get(0).getBegin());
        assertEquals("192.168.0.6", factory.getConfig().getDefinition().get(0).getRange().get(0).getEnd());
    }

    @Test
    public final void testAddAdjacentSpecificToDefIPv6() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<nsclient-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:fedb</specific>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:fedc</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</nsclient-config>\n" + 
        "";

        NSClientPeerFactory factory = new NSClientPeerFactory(new ByteArrayInputStream(amiConfigXml.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(2, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getRange().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(1, factory.getConfig().getDefinition().get(0).getRange().size());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedb", factory.getConfig().getDefinition().get(0).getRange().get(0).getBegin());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedc", factory.getConfig().getDefinition().get(0).getRange().get(0).getEnd());
    }

    @Test
    public final void testAddAdjacentSpecificToDefIPv6WithSameScopeId() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<nsclient-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:fedb%5</specific>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:fedc%5</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</nsclient-config>\n" + 
        "";

        NSClientPeerFactory factory = new NSClientPeerFactory(new ByteArrayInputStream(amiConfigXml.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(2, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getRange().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(1, factory.getConfig().getDefinition().get(0).getRange().size());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedb%5", factory.getConfig().getDefinition().get(0).getRange().get(0).getBegin());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedc%5", factory.getConfig().getDefinition().get(0).getRange().get(0).getEnd());
    }

    @Test
    public final void testAddAdjacentSpecificToDefIPv6WithDifferentScopeIds() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<nsclient-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:fedb%1</specific>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:fedc%2</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</nsclient-config>\n" + 
        "";

        NSClientPeerFactory factory = new NSClientPeerFactory(new ByteArrayInputStream(amiConfigXml.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(2, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getRange().size());

        factory.optimize();

        // No optimization should occur because the addresses have different scope IDs
        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(2, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getRange().size());
    }

    /**
     * This tests the ability to move a specific from one definition into a range of another definition.  The
     * results should be that the 2 ranges in the first definition are recombined into a single range based on 
     * the single IP address that was in a different existing definition that will now be removed and the definition
     * deleted.
     * 
     * @throws IOException 
     */
    @Test
    public void testRecombineSpecificIntoRange() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<nsclient-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <range begin=\"fe80:0000:0000:0000:0000:0000:0000:fed0%1\" end=\"fe80:0000:0000:0000:0000:0000:0000:fed9%1\"/>\n" + 
        "       <range begin=\"fe80:0000:0000:0000:0000:0000:0000:fedb%1\" end=\"fe80:0000:0000:0000:0000:0000:0000:fedf%1\"/>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:feda%1</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</nsclient-config>\n" + 
        "";

        NSClientPeerFactory factory = new NSClientPeerFactory(new ByteArrayInputStream(amiConfigXml.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(1, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(2, factory.getConfig().getDefinition().get(0).getRange().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(1, factory.getConfig().getDefinition().get(0).getRange().size());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fed0%1", factory.getConfig().getDefinition().get(0).getRange().get(0).getBegin());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedf%1", factory.getConfig().getDefinition().get(0).getRange().get(0).getEnd());
    }

    /**
     * This tests the ability to move a specific from one definition into a range of another definition.  The
     * results should be that the 2 ranges in the first definition are recombined into a single range based on 
     * the single IP address that was in a different existing definition that will now be removed and the definition
     * deleted.
     * 
     * @throws IOException 
     */
    @Test
    public void testRecombineSpecificIntoRangeWithDifferentScopeIds() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<nsclient-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <range begin=\"fe80:0000:0000:0000:0000:0000:0000:fed0%1\" end=\"fe80:0000:0000:0000:0000:0000:0000:fed9%1\"/>\n" + 
        "       <range begin=\"fe80:0000:0000:0000:0000:0000:0000:fedb%2\" end=\"fe80:0000:0000:0000:0000:0000:0000:fedf%2\"/>\n" + 
        "       <specific>fe80:0000:0000:0000:0000:0000:0000:feda%1</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</nsclient-config>\n" + 
        "";

        NSClientPeerFactory factory = new NSClientPeerFactory(new ByteArrayInputStream(amiConfigXml.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(1, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(2, factory.getConfig().getDefinition().get(0).getRange().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(2, factory.getConfig().getDefinition().get(0).getRange().size());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fed0%1", factory.getConfig().getDefinition().get(0).getRange().get(0).getBegin());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:feda%1", factory.getConfig().getDefinition().get(0).getRange().get(0).getEnd());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedb%2", factory.getConfig().getDefinition().get(0).getRange().get(1).getBegin());
        assertEquals("fe80:0000:0000:0000:0000:0000:0000:fedf%2", factory.getConfig().getDefinition().get(0).getRange().get(1).getEnd());
    }

    /**
     * This tests the addition of a new specific definition that is the same address as the beginning of
     * a range in a current definition.
     * 
     * @throws IOException 
     */
    @Test
    public final void testNewSpecificSameAsBeginInOldDef() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<nsclient-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <range begin=\"192.168.0.6\" end=\"192.168.0.12\"/>\n" + 
        "       <specific>192.168.0.6</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</nsclient-config>\n" + 
        "";

        NSClientPeerFactory factory = new NSClientPeerFactory(new ByteArrayInputStream(amiConfigXml.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(1, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(1, factory.getConfig().getDefinition().get(0).getRange().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(1, factory.getConfig().getDefinition().get(0).getRange().size());
        assertEquals("192.168.0.6", factory.getConfig().getDefinition().get(0).getRange().get(0).getBegin());
        assertEquals("192.168.0.12", factory.getConfig().getDefinition().get(0).getRange().get(0).getEnd());
    }

    /**
     * This tests the addition of a new specific definition that is the same address as the beginning of
     * a range in a current definition.
     * 
     * @throws IOException 
     */
    @Test
    public final void testNewSpecificSameAsEndInOldDef() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<nsclient-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <range begin=\"192.168.0.6\" end=\"192.168.0.12\"/>\n" + 
        "       <specific>192.168.0.12</specific>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</nsclient-config>\n" + 
        "";

        NSClientPeerFactory factory = new NSClientPeerFactory(new ByteArrayInputStream(amiConfigXml.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(1, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(1, factory.getConfig().getDefinition().get(0).getRange().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(1, factory.getConfig().getDefinition().get(0).getRange().size());
        assertEquals("192.168.0.6", factory.getConfig().getDefinition().get(0).getRange().get(0).getBegin());
        assertEquals("192.168.0.12", factory.getConfig().getDefinition().get(0).getRange().get(0).getEnd());
    }

    /**
     * This tests the merging of a new definition that contains a range of IP addresses that overlaps
     * the end of one range and the beginning of another range in a current definition.
     * 
     * @throws IOException 
     */
    @Test
    public void testOverlapsTwoRanges() throws IOException {

        String amiConfigXml = "<?xml version=\"1.0\"?>\n" + 
        "<nsclient-config retry=\"3\" timeout=\"800\"\n" + 
        "   password=\"password\">\n" + 
        "   <definition>\n" + 
        "       <range begin=\"192.168.0.6\" end=\"192.168.0.12\"/>\n" + 
        "       <range begin=\"192.168.0.20\" end=\"192.168.0.100\"/>\n" + 
        "       <range begin=\"192.168.0.8\" end=\"192.168.0.30\"/>\n" + 
        "   </definition>\n" + 
        "\n" + 
        "</nsclient-config>\n" + 
        "";

        NSClientPeerFactory factory = new NSClientPeerFactory(new ByteArrayInputStream(amiConfigXml.getBytes(StandardCharsets.UTF_8)));

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(3, factory.getConfig().getDefinition().get(0).getRange().size());

        factory.optimize();

        assertEquals(1, factory.getConfig().getDefinition().size());
        assertEquals(0, factory.getConfig().getDefinition().get(0).getSpecific().size());
        assertEquals(1, factory.getConfig().getDefinition().get(0).getRange().size());
        assertEquals("192.168.0.6", factory.getConfig().getDefinition().get(0).getRange().get(0).getBegin());
        assertEquals("192.168.0.100", factory.getConfig().getDefinition().get(0).getRange().get(0).getEnd());
    }
}
