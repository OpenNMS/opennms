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
package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

import org.junit.rules.TemporaryFolder;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;

import junit.framework.TestCase;
import org.opennms.core.spring.ContextRegistry;

/**
 * JUnit tests for the configureSNMP event handling and optimization of
 * the SNMP configuration XML.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 *
 */
public class WmiPeerFactoryTest extends TestCase {
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Reload contexts to pick up test-specific beanRefContext.xml
        ContextRegistry.getInstance().reloadContexts();
    }
    private WmiPeerFactory getFactory(String amiConfigXml) throws IOException {
        WmiPeerFactory factory = new WmiPeerFactory(ConfigurationTestUtils.getResourceForConfigWithReplacements(amiConfigXml));
        factory.afterPropertiesSet();
        return factory;
    }

    public void testMetadata() throws IOException {
        final TemporaryFolder tempFolder = new TemporaryFolder();
        tempFolder.create();
        final File keystoreFile = new File(tempFolder.getRoot(), "scv.jce");
        final SecureCredentialsVault secureCredentialsVault = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "notRealPassword");
        secureCredentialsVault.setCredentials("wmi", new Credentials("foo", "bar"));

        final String wmiConfigXml = "<?xml version=\"1.0\"?>\n" +
                "<wmi-config retry=\"3\" timeout=\"800\"\n" +
                "   username=\"${scv:wmi:username}\" password=\"${scv:wmi:password}\">\n" +
                "   <definition>\n" +
                "       <range begin=\"192.168.0.6\" end=\"192.168.0.10\"/>\n" +
                "       <range begin=\"fe80:0000:0000:0000:0000:0000:0000:0006\" end=\"fe80:0000:0000:0000:0000:0000:0000:0010\"/>\n" +
                "       <specific>192.168.0.5</specific>\n" +
                "       <specific>fe80:0000:0000:0000:0000:0000:0000:0005</specific>\n" +
                "   </definition>\n" +
                "\n" +
                "</wmi-config>\n" +
                "";

        final WmiPeerFactory factory = getFactory(wmiConfigXml);
        factory.setSecureCredentialsVaultScope(new SecureCredentialsVaultScope(secureCredentialsVault));

        assertEquals(Optional.of("${scv:wmi:username}"), factory.getConfig().getUsername());
        assertEquals(Optional.of("${scv:wmi:password}"), factory.getConfig().getPassword());
        assertEquals("foo", factory.getAgentConfig(InetAddress.getByName("192.168.0.5")).getUsername());
        assertEquals("bar", factory.getAgentConfig(InetAddress.getByName("192.168.0.5")).getPassword());
        assertEquals("foo", factory.getAgentConfig(InetAddress.getByName("192.168.0.6")).getUsername());
        assertEquals("bar", factory.getAgentConfig(InetAddress.getByName("192.168.0.6")).getPassword());
        assertEquals("foo", factory.getAgentConfig(InetAddress.getByName("fe80:0000:0000:0000:0000:0000:0000:0005")).getUsername());
        assertEquals("bar", factory.getAgentConfig(InetAddress.getByName("fe80:0000:0000:0000:0000:0000:0000:0005")).getPassword());
        assertEquals("foo", factory.getAgentConfig(InetAddress.getByName("fe80:0000:0000:0000:0000:0000:0000:0006")).getUsername());
        assertEquals("bar", factory.getAgentConfig(InetAddress.getByName("fe80:0000:0000:0000:0000:0000:0000:0006")).getPassword());
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
