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

import static org.junit.Assert.assertNotEquals;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.core.test.Level;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LocationUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
import org.opennms.netmgt.config.snmp.Configuration;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;

import junit.framework.TestCase;

public class SnmpPeerFactoryTest extends TestCase {

    private int m_version;

    @Override
    protected void setUp() throws Exception {
        final TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();
        final File keystoreFile = new File(temporaryFolder.getRoot(), "scv.jce");

        final SecureCredentialsVault secureCredentialsVault = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "notRealPassword");
        secureCredentialsVault.setCredentials("myv1community", new Credentials("username", "specificv1"));
        secureCredentialsVault.setCredentials("myv2community", new Credentials("username", "specificv2c"));
        secureCredentialsVault.setCredentials("myCredentials-profile1", new Credentials("securityName-profile1", "authPassphrase-profile1"));
        secureCredentialsVault.setCredentials("myCredentials-profile2", new Credentials("securityName-profile2", "authPassphrase-profile2"));
        SnmpPeerFactory.setSecureCredentialsVaultScope(new SecureCredentialsVaultScope(secureCredentialsVault));
        setVersion(SnmpAgentConfig.VERSION2C);
        SnmpPeerFactory.setResource(new ByteArrayResource(getSnmpConfig().getBytes()));
        MockLogAppender.setupLogging(true);
    }

    public void setVersion(int version) {
        m_version = version;
    }

    /**
     * String representing snmp-config.xml
     */
    public String getSnmpConfig() {
        return "<?xml version=\"1.0\"?>\n" + 
                "<snmp-config "+ 
                " retry=\"3\" timeout=\"3000\"\n" + 
                " read-community=\"public\"" +
                " write-community=\"private\"\n" + 
                " port=\"161\"\n" +
                " max-vars-per-pdu = \"23\" " +
                " version=\"v1\">\n" +
                "\n" +
                "   <definition port=\"9161\" version=\""+myVersion()+"\" " +
                "       security-name=\"opennmsUser\" \n" + 
                "       auth-passphrase=\"0p3nNMSv3\" \n" +
                "       privacy-passphrase=\"0p3nNMSv3\" >\n" +
                "       <specific>"+myLocalHost()+"</specific>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v1\" read-community=\"${scv:myv1community:password}\">\n" +
                "       <specific>10.0.0.1</specific>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v1\" read-community=\"${scv:myv1community:password}\" max-request-size=\"484\">\n" +
                "       <specific>10.0.0.2</specific>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v1\" read-community=\"${scv:myv1community:password}\" proxy-host=\""+myLocalHost()+"\">\n" +
                "       <specific>10.0.0.3</specific>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v3\" " +
                "       security-name=\"opennmsUser20\" \n" +
                "       auth-passphrase=\"0p3nNMSv3_20\" >\n" +
                "       <specific>20.20.20.20</specific>\n" +
                "   </definition>\n" + 
                "   <definition version=\"v3\" " +
                "       security-name=\"opennmsRangeUser\" \n" + 
                "       auth-passphrase=\"0p3nNMSv3\" >\n" +
                "       <range begin=\"1.1.1.1\" end=\"1.1.1.100\"/>\n" +
                "   </definition>\n" + 
                "   <definition version=\"v3\" " +
                "       security-name=\"opennmsContextUser\" \n" + 
                "       context-name=\"testContext\" \n" +
                "       engine-id=\"testEngineId\" \n" +
                "       context-engine-id=\"testContextEngineId\" >\n" +
                "       <specific>1.1.1.101</specific>" +
                "   </definition>\n" + 
                "   <definition version=\"v3\" " +
                "       security-name=\"opennmsuser1\" \n" + 
                "       context-name=\"VF:2\" >\n" +
                "       <specific>10.11.12.13</specific>\n" +
                "   </definition>\n" + 
                "   <definition version=\"v3\" " +
                "       security-name=\"opennmsuser2\" \n" + 
                "       context-name=\"VF:3\" auth-passphrase=\"\" auth-protocol=\"MD5\" privacy-passphrase=\"\" privacy-protocol=\"DES\">\n" +
                "       <specific>10.11.12.14</specific>\n" +
                "   </definition>\n" + 
                "   <definition version=\"v1\" read-community=\"rangev1\" max-vars-per-pdu=\"55\"> \n" + 
                "       <range begin=\"10.0.0.101\" end=\"10.0.0.200\"/>\n" +
                "   </definition>\n" + 
                "\n" + 
                "   <definition version=\"v2c\" read-community=\"rangev2c\">\n" + 
                "       <range begin=\"10.0.1.100\" end=\"10.0.5.100\"/>\n" +
                "       <range begin=\"10.7.20.100\" end=\"10.7.25.100\"/>\n" +
                "   </definition>\n" + 
                "\n" +
                "   <definition version=\"v2c\" read-community=\"${scv:myv2community:password}\">\n" +
                "       <specific>192.168.0.50</specific>\n" +
                "   </definition>\n" + 
                "\n" +
                "   <definition version=\"v2c\" location=\"MINION\" timeout=\"2500\">\n" + 
                "       <specific>192.168.0.50</specific>\n" +
                "   </definition>\n" +
                "\n" +
                "   <definition version=\"v2c\" location=\"OPENNMS\" timeout=\"2000\">\n" + 
                "       <specific>192.160.0.50</specific>\n" +
                "   </definition>\n" + 
                "\n" +
                "   <definition version=\"v2c\" location=\"MINION\" timeout=\"2200\">\n" + 
                "       <specific>192.167.0.50</specific>\n" +
                "   </definition>\n" + 
                "\n" +
                "   <definition version=\"v2c\" location=\"MINION\" timeout=\"2300\">\n" + 
                "       <specific>192.166.0.50</specific>\n" +
                "   </definition>\n" + 
                "\n" +
                "   <definition version=\"v2c\" location=\"AUSTIN\" timeout=\"2100\">\n" + 
                "       <specific>192.164.0.50</specific>\n" +
                "   </definition>\n" +
                "\n" +
                "   <definition version=\"v3\"  timeout=\"2400\">\n" + 
                "       <specific>192.164.0.50</specific>\n" +
                "   </definition>\n" +
                "\n" +
                "   <definition version=\"v2c\" read-community=\"rangev2\">\n" + 
                "       <specific>192.167.0.50</specific>\n" +
                "   </definition>\n" + 
                "\n" +
                "   <definition version=\"v2c\" read-community=\"ipmatch\" max-vars-per-pdu=\"128\" max-repetitions=\"7\" >\n" + 
                "       <ip-match>77.5-12,15.1-255.255</ip-match>\n" +
                "   </definition>\n" + 
                "\n" +
                "   <definition version=\"v1\" read-community=\"ipmatch-location-a\" max-vars-per-pdu=\"64\" location=\"Location-A\" >\n" +
                "       <ip-match>88.10-20.*.1-50</ip-match>\n" +
                "   </definition>\n" +
                "\n" +
                "   <definition version=\"v1\" read-community=\"ipmatch-location-b\" max-vars-per-pdu=\"32\" location=\"Location-B\" >\n" +
                "       <ip-match>88.10-20.*.1-50</ip-match>\n" +
                "   </definition>\n" +
                "\n" +
                    "<profiles>"
                        +"<profile " +  "retry=\"2\" "
                        + "  timeout=\"100\" "
                        + "  read-community=\"public\" "
                        + "  proxy-host=\""+myLocalHost()+"\""
                        + "  version=\"v2c\" "
                        + "  max-vars-per-pdu=\"4\" "
                        + "  max-repetitions=\"5\" "
                        + "  max-request-size=\"484\" "
                        + "  security-name=\"${scv:myCredentials-profile2:username}\" "
                        + "  auth-passphrase=\"${scv:myCredentials-profile2:password}\" "
                        + "  security-level=\"3\" "
                        + "  auth-protocol=\"MD5\" "
                        + "  engine-id=\"engineId\" "
                        + "  context-engine-id=\"contextEngineId\" "
                        + "  context-name=\"profileContext\" "
                        + "  enterprise-id=\"enterpriseId\">"
                        + "<label>profile2</label>"
                        + "<filter>*.opennms.com</filter>"
                        + "</profile>"
                        + "<profile "  + "retry=\"5\" "
                        + "  timeout=\"300\" "
                        + "  write-community=\"private\" "
                        + "  proxy-host=\""+myLocalHost()+"\""
                        + "  version=\"v3\" "
                        + "  security-name=\"${scv:myCredentials-profile1:username}\" "
                        + "  auth-passphrase=\"${scv:myCredentials-profile1:password}\" "
                        + "  max-vars-per-pdu=\"4\" "
                        + "  max-repetitions=\"5\" "
                        + "  max-request-size=\"484\" "
                        + "  security-level=\"3\" "
                        + "  auth-protocol=\"MD5\" "
                        + "  engine-id=\"engineId\" "
                        + "  context-engine-id=\"contextEngineId\" "
                        + "  context-name=\"profileContext\" "
                        //+ "  privacy-protocol=\"DES\" "
                        + "  enterprise-id=\"enterpriseId\">"
                        + "<label>profile1</label>"
                        + "<filter>*.opennms.org</filter>"
                        + "</profile>"
                    + "</profiles>"
                + "</snmp-config>";
    }

    /**
     * String representing snmp-config.xml
     */
    public String getBadRangeSnmpConfig() {
        return "<?xml version=\"1.0\"?>\n" + 
                "<snmp-config "+ 
                " retry=\"3\" timeout=\"3000\"\n" + 
                " read-community=\"public\"" +
                " write-community=\"private\"\n" + 
                " port=\"161\"\n" +
                " max-vars-per-pdu = \"23\" " +
                " version=\"v1\">\n" +
                "\n" +
                "   <definition version=\"v2c\" read-community=\"rangev2c\">\n" + 
                "       <range begin=\"10.0.5.100\" end=\"10.0.1.100\"/>\n" +
                "       <range begin=\"10.7.25.100\" end=\"10.7.20.100\"/>\n" +
                "   </definition>\n" + 
                "\n" + 
                "</snmp-config>";
    }

    protected String myLocalHost() {

        //      try {
        //          return InetAddressUtils.str(InetAddress.getLocalHost());
        //      } catch (UnknownHostException e) {
        //          e.printStackTrace();
        //          fail("Exception getting localhost");
        //      }
        //      
        //      return null;

        return "127.0.0.1";
    }

    private String myVersion() {
        return switch (m_version) {
            case SnmpAgentConfig.VERSION1 -> "v1";
            case SnmpAgentConfig.VERSION2C -> "v2c";
            case SnmpAgentConfig.VERSION3 -> "v3";
            default -> "v1";
        };
    }

    @Override
    protected void tearDown() {

    }

    public void testProxiedAgent() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("10.0.0.3"));
        assertEquals("10.0.0.3", InetAddressUtils.str(agentConfig.getProxyFor()));
        assertEquals("127.0.0.1", InetAddressUtils.str(agentConfig.getAddress()));
        assertTrue(agentConfig.toString().contains(", ProxyForAddress: 10.0.0.3"));
    }

    public void testDefaultMaxRequestSize() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("10.0.0.1"));
        assertEquals(SnmpAgentConfig.DEFAULT_MAX_REQUEST_SIZE, agentConfig.getMaxRequestSize());

        agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("10.0.0.2"));
        assertEquals(484, agentConfig.getMaxRequestSize());
    }

    public void testDefaultMaxVarsPerPdu() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(myLocalHost()));
        assertEquals(23, agentConfig.getMaxVarsPerPdu());
    }

    public void testConfigureDefaultMaxVarsPerPdu() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("10.0.0.150"));
        assertEquals(55, agentConfig.getMaxVarsPerPdu());
    }

    public void testGetMaxRepetitions() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("77.5.5.255"));
        assertEquals("ipmatch", agentConfig.getReadCommunity());
        assertEquals(7, agentConfig.getMaxRepetitions());

        agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("10.0.0.1"));
        assertEquals("specificv1", agentConfig.getReadCommunity());
        assertEquals(2, agentConfig.getMaxRepetitions());
    }

    public void testGetTargetFromPatterns() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("77.5.5.255"));
        assertEquals("ipmatch", agentConfig.getReadCommunity());
        assertEquals(128, agentConfig.getMaxVarsPerPdu());

        agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("77.15.80.255"));
        assertEquals("ipmatch", agentConfig.getReadCommunity());
        assertEquals(7, agentConfig.getMaxRepetitions());

        // should be default community "public" because of 4
        agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("77.4.5.255"));
        assertEquals("public", agentConfig.getReadCommunity());

        // should be default community because of 0
        agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("77.6.0.255"));
        assertEquals("public", agentConfig.getReadCommunity());
    }

    public void testGetSnmpAgentConfig() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(myLocalHost()));
        assertEquals(SnmpAgentConfig.VERSION2C, agentConfig.getVersion());
    }

    /**
     * This tests getting an SnmpAgentConfig
     * @throws UnknownHostException
     */
    public void testGetConfig() throws UnknownHostException {
        assertNotNull(SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getLocalHost()));
    }

    /**
     * This tests for ranges configured for a v2 node and community string
     * @throws UnknownHostException
     */
    public void testGetv2cInRange() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("10.7.23.100"));
        assertNotNull(agentConfig);
        assertEquals(SnmpAgentConfig.VERSION2C, agentConfig.getVersion());
        assertEquals("rangev2c", agentConfig.getReadCommunity());
    }

    /**
     * This tests for ranges configured for v3 node and security name
     * @throws UnknownHostException 
     */
    public void testGetv3ConfigInRange() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("1.1.1.50"));
        assertNotNull(agentConfig);
        assertEquals(SnmpAgentConfig.VERSION3, agentConfig.getVersion());
        assertEquals("opennmsRangeUser", agentConfig.getSecurityName());
    }

    /**
     * This tests for context-name configured for v3 node
     * @throws UnknownHostException 
     */
    public void testGetv3ConfigWithContextNameAndMore() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("1.1.1.101"));
        assertNotNull(agentConfig);
        assertEquals(SnmpAgentConfig.VERSION3, agentConfig.getVersion());
        assertEquals("opennmsContextUser", agentConfig.getSecurityName());
        assertEquals("testContext", agentConfig.getContextName());
        assertEquals("testEngineId", agentConfig.getEngineId());
        assertEquals("testContextEngineId", agentConfig.getContextEngineId());
    }
    /**
     * This tests getting a v1 config
     * @throws UnknownHostException
     */
    public void testGetV1Config() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("10.0.0.1"));
        assertNotNull(agentConfig);
        assertEquals(SnmpAgentConfig.VERSION1, agentConfig.getVersion());
        assertEquals("specificv1", agentConfig.getReadCommunity());
    }

    /**
     * This tests for a specifically defined v2c agentConfig
     * 
     * @throws UnknownHostException
     */
    public void testGetV2cConfig() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance()
                .getAgentConfig(InetAddressUtils.addr("192.168.0.50"));
        assertNotNull(agentConfig);
        assertEquals(SnmpAgentConfig.VERSION2C, agentConfig.getVersion());
        assertEquals("specificv2c", agentConfig.getReadCommunity());
    }

    /**
     * Below tests for a valid/invalid location match
     * 
     * @throws UnknownHostException
     */
    public void testGetConfigWithValidLocation() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance()
                .getAgentConfig(InetAddressUtils.addr("192.168.0.50"), "MINION");
        assertNotNull(agentConfig);
        assertEquals(SnmpAgentConfig.VERSION2C, agentConfig.getVersion());
        assertEquals(2500, agentConfig.getTimeout());
    }

    public void testGetConfigWithInvalidLocation() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance()
                .getAgentConfig(InetAddressUtils.addr("192.168.0.51"), "AUSTIN");
        assertNotNull(agentConfig);
        assertEquals(SnmpAgentConfig.VERSION1, agentConfig.getVersion());
        assertEquals("public", agentConfig.getReadCommunity());
    }

    public void testGetV2cConfigWithoutLocation() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance()
                .getAgentConfig(InetAddressUtils.addr("192.167.0.50"), LocationUtils.DEFAULT_LOCATION_NAME);
        assertNotNull(agentConfig);
        assertEquals(SnmpAgentConfig.VERSION2C, agentConfig.getVersion());
        assertEquals("rangev2", agentConfig.getReadCommunity());
    }

    public void testGetV2cConfigWithDifferentLocation() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance()
                .getAgentConfig(InetAddressUtils.addr("192.166.0.50"));
        assertNotNull(agentConfig);
        assertEquals(SnmpAgentConfig.VERSION1, agentConfig.getVersion());
        assertEquals("public", agentConfig.getReadCommunity());
    }


    public void testGetV3ConfigWithoutLocation() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance()
                .getAgentConfig(InetAddressUtils.addr("192.164.0.50"), LocationUtils.DEFAULT_LOCATION_NAME);
        assertNotNull(agentConfig);
        assertEquals(SnmpAgentConfig.VERSION3, agentConfig.getVersion());
        assertEquals(2400, agentConfig.getTimeout());
    }

    public void testNoMatchedDefinitionWithLocationMatch() throws UnknownHostException {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance()
                .getAgentConfig(InetAddressUtils.addr("192.160.0.50"), "MINION");
        assertNotNull(agentConfig);
        assertEquals(SnmpAgentConfig.VERSION1, agentConfig.getVersion());
        assertEquals("public", agentConfig.getReadCommunity());
    }

    public void testFallbackToDefaultLocation() {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(
                InetAddressUtils.addr("10.7.23.100"), "SOME-LOCATION");
        assertNotNull(agentConfig);
        assertEquals(SnmpAgentConfig.VERSION2C, agentConfig.getVersion());
        assertEquals("rangev2c", agentConfig.getReadCommunity());
    }

    /**
     * This tests for ranges configured for a v2 node and community string
     * 
     * @throws UnknownHostException
     */
    public void testReversedRange() throws UnknownHostException {
        SnmpPeerFactory.setResource(new ByteArrayResource(getBadRangeSnmpConfig().getBytes()));

        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("10.7.23.100"));
        assertNotNull(agentConfig);
        assertEquals(SnmpAgentConfig.VERSION2C, agentConfig.getVersion());
        assertEquals("rangev2c", agentConfig.getReadCommunity());
    }

    public void testSnmpV3WithNoAuthNoPriv() throws Exception {
        SnmpPeerFactory.setResource(new ByteArrayResource(getSnmpConfig().getBytes()));
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr("10.11.12.13"));
        assertEquals("opennmsuser1", agentConfig.getSecurityName());
        assertEquals("VF:2", agentConfig.getContextName());
        assertNull(agentConfig.getAuthProtocol());
        assertNull(agentConfig.getPrivProtocol());
        assertNull(agentConfig.getAuthPassPhrase());
        assertNull(agentConfig.getPrivPassPhrase());
        assertEquals(1, agentConfig.getSecurityLevel());

        agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(myLocalHost()));
        assertEquals(3, agentConfig.getSecurityLevel());
    }

    public void testSnmpProfile() {
        SnmpPeerFactory.setResource(new ByteArrayResource(getSnmpConfig().getBytes()));
        List<SnmpProfile> profiles = SnmpPeerFactory.getInstance().getProfiles();
        assertEquals(2, profiles.size());

        for (SnmpProfile snmpProfile : profiles) {
            SnmpAgentConfig snmpAgentConfig = SnmpPeerFactory.getInstance().
                    getAgentConfigFromProfile(snmpProfile, InetAddressUtils.addr("10.1.12.1"));

            assertEquals("profileContext", snmpAgentConfig.getContextName());
            // Even if read-community/write-community is not specified, should use defaults.
            assertEquals("public", snmpAgentConfig.getReadCommunity());
            assertEquals("private", snmpAgentConfig.getWriteCommunity());
            assertEquals("securityName-" + snmpProfile.getLabel(), snmpAgentConfig.getSecurityName());
            assertEquals("authPassphrase-" + snmpProfile.getLabel(), snmpAgentConfig.getAuthPassPhrase());
            assertNull(snmpAgentConfig.getPrivProtocol());
            assertThat(snmpAgentConfig.getVersionAsString(), Matchers.isOneOf("v2c", "v3"));
        }
    }

    public void testMergingWithMetadata() throws Exception {
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        final var file = new File(temporaryFolder.getRoot(), "snmp-config.xml");

        try (var filewriter = new FileWriter(file)) {
            IOUtils.write("<snmp-config xmlns=\"http://xmlns.opennms.org/xsd/config/snmp\" version=\"v2c\" read-community=\"minion\" timeout=\"1800\" retry=\"1\"/>", filewriter);
        }

        final URL url = file.toURI().toURL();

        try (final InputStream inputStream = url.openStream()) {
            SnmpPeerFactory.setResource(new InputStreamResource(inputStream));
            final SnmpPeerFactory snmpPeerFactory = SnmpPeerFactory.getInstance();

            final Definition defA = new Definition();
            defA.setRanges(List.of(new Range("192.168.30.1", "192.168.30.10")));
            defA.setReadCommunity("${scv:myCommunity:password}");
            defA.setWriteCommunity("private");
            defA.setAuthPassphrase("${scv:myAuthPassphrase:password}");
            defA.setPrivacyPassphrase("${scv:myPrivacyPassphrase:password}");
            snmpPeerFactory.saveDefinition(defA, true);

            final Definition defB = new Definition();
            defB.setRanges(List.of(new Range("192.168.30.11", "192.168.30.30")));
            defB.setReadCommunity("${scv:myCommunity:password}");
            defB.setWriteCommunity("private");
            defB.setAuthPassphrase("${scv:myAuthPassphrase:password}");
            defB.setPrivacyPassphrase("${scv:myPrivacyPassphrase:password}");
            snmpPeerFactory.saveDefinition(defB, true);

            final SnmpConfig snmpConfig1 = JaxbUtils.unmarshal(SnmpConfig.class, snmpPeerFactory.getSnmpConfigAsString());

            assertEquals(1, snmpConfig1.getDefinitions().size());
            assertEquals("${scv:myAuthPassphrase:password}", snmpConfig1.getDefinitions().get(0).getAuthPassphrase());
            assertEquals("${scv:myPrivacyPassphrase:password}", snmpConfig1.getDefinitions().get(0).getPrivacyPassphrase());
            assertEquals("${scv:myCommunity:password}", snmpConfig1.getDefinitions().get(0).getReadCommunity());
            assertEquals("private", snmpConfig1.getDefinitions().get(0).getWriteCommunity());

            final Definition defC = new Definition();
            defC.setRanges(List.of(new Range("192.168.30.31", "192.168.30.35")));
            // this should not match
            defC.setReadCommunity("${scv:anotherCommunity:password}");
            defC.setWriteCommunity("private");
            defC.setAuthPassphrase("${scv:myAuthPassphrase:password}");
            defC.setPrivacyPassphrase("${scv:myPrivacyPassphrase:password}");
            snmpPeerFactory.saveDefinition(defC, true);

            final SnmpConfig snmpConfig2 = JaxbUtils.unmarshal(SnmpConfig.class, snmpPeerFactory.getSnmpConfigAsString());
            assertEquals(2, snmpConfig2.getDefinitions().size());
        }
    }

    public void testRemoveSpecificIpAddress() {
        // confirm entry exists
        InetAddress addr = InetAddressUtils.addr("20.20.20.20");

        SnmpConfig snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertTrue(snmpConfig.getDefinitions().stream().anyMatch(d -> d.getSpecifics().contains("20.20.20.20")));

        SnmpAgentConfig config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(3, config.getVersion());
        assertEquals("opennmsUser20", config.getSecurityName());
        assertEquals("0p3nNMSv3_20", config.getAuthPassPhrase());

        // now delete it
        assertTrue(SnmpPeerFactory.getInstance().removeRangesFromDefinition(null, List.of("20.20.20.20"), null, "Default", "unit test"));

        final String expectedLogMessage =
                String.format("Removed %d ranges, %d specifics, %d ipMatches from definitions at location %s by module %s",
                        0, 1, 0, "Default", "unit test");

        MockLogAppender.assertLogMatched(Level.INFO, expectedLogMessage);

        snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertTrue(snmpConfig.getDefinitions().stream().noneMatch(d -> d.getSpecifics().contains("20.20.20.20")));

        // config should have reverted to defaults
        config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(1, config.getVersion());
        assertEquals("opennmsUser", config.getSecurityName());
        assertNull(config.getAuthPassPhrase());
        assertEquals("public", config.getReadCommunity());
        assertEquals("private", config.getWriteCommunity());
    }

    public void testRemoveSpecificIPv6Address() {
        // add an IPv6 specific entry
        final String ipv6Address = "2266:25::12:0:ad12";
        InetAddress addr = InetAddressUtils.addr(ipv6Address);

        SnmpPeerFactory.getInstance().saveDefinition(new Definition() {{
            setSpecifics(List.of(ipv6Address));
            setReadCommunity("read-ipv6-21");
            setVersion("v2c");
        }}, true);

        SnmpConfig snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();

        assertTrue(snmpConfig.getDefinitions().stream().anyMatch(d -> d.getSpecifics().contains(ipv6Address)));

        SnmpAgentConfig config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(2, config.getVersion());
        assertEquals("read-ipv6-21", config.getReadCommunity());

        // now delete it
        assertTrue(SnmpPeerFactory.getInstance().removeRangesFromDefinition(null, List.of(ipv6Address), null, "Default", "unit test"));

        final String expectedLogMessage =
                String.format("Removed %d ranges, %d specifics, %d ipMatches from definitions at location %s by module %s",
                        0, 1, 0, "Default", "unit test");

        MockLogAppender.assertLogMatched(Level.INFO, expectedLogMessage);

        snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertTrue(snmpConfig.getDefinitions().stream().noneMatch(d -> d.getSpecifics().contains(ipv6Address)));

        // config should have reverted to defaults
        config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(1, config.getVersion());
        assertEquals("public", config.getReadCommunity());
        assertEquals("private", config.getWriteCommunity());
    }

    /** Add compressed, remove using full/expanded notation. */
    public void testRemoveSpecificIPv6AddressAddCompressedRemoveFull() {
        final String compressedAddress = "2266:25::12:0:ad12";
        final String fullAddress = "2266:25:0:0:0:12:0:ad12";
        InetAddress addr = InetAddressUtils.addr(compressedAddress);

        SnmpPeerFactory.getInstance().saveDefinition(new Definition() {{
            setSpecifics(List.of(compressedAddress));
            setReadCommunity("read-ipv6-compressed");
            setVersion("v2c");
        }}, true);

        SnmpConfig snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertTrue(snmpConfig.getDefinitions().stream().anyMatch(d -> d.getSpecifics().contains(compressedAddress)));

        SnmpAgentConfig config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(2, config.getVersion());
        assertEquals("read-ipv6-compressed", config.getReadCommunity());

        // remove using full notation
        assertTrue(SnmpPeerFactory.getInstance().removeRangesFromDefinition(null, List.of(fullAddress), null, "Default", "unit test"));

        final String expectedLogMessage =
                String.format("Removed %d ranges, %d specifics, %d ipMatches from definitions at location %s by module %s",
                        0, 1, 0, "Default", "unit test");
        MockLogAppender.assertLogMatched(Level.INFO, expectedLogMessage);

        snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertTrue(snmpConfig.getDefinitions().stream().noneMatch(d -> d.getSpecifics().contains(compressedAddress)));

        config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(1, config.getVersion());
        assertEquals("public", config.getReadCommunity());
        assertEquals("private", config.getWriteCommunity());
    }

    /** Add full/expanded notation, remove using compressed notation. */
    public void testRemoveSpecificIPv6AddressAddFullRemoveCompressed() {
        final String fullAddress = "2266:25:0:0:0:12:0:ad12";
        final String compressedAddress = "2266:25::12:0:ad12";
        InetAddress addr = InetAddressUtils.addr(fullAddress);

        SnmpPeerFactory.getInstance().saveDefinition(new Definition() {{
            setSpecifics(List.of(fullAddress));
            setReadCommunity("read-ipv6-full");
            setVersion("v2c");
        }}, true);

        SnmpConfig snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertTrue(snmpConfig.getDefinitions().stream().anyMatch(d -> d.getSpecifics().contains(fullAddress)));

        SnmpAgentConfig config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(2, config.getVersion());
        assertEquals("read-ipv6-full", config.getReadCommunity());

        // remove using compressed notation
        assertTrue(SnmpPeerFactory.getInstance().removeRangesFromDefinition(null, List.of(compressedAddress), null, "Default", "unit test"));

        final String expectedLogMessage =
                String.format("Removed %d ranges, %d specifics, %d ipMatches from definitions at location %s by module %s",
                        0, 1, 0, "Default", "unit test");
        MockLogAppender.assertLogMatched(Level.INFO, expectedLogMessage);

        snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertTrue(snmpConfig.getDefinitions().stream().noneMatch(d -> d.getSpecifics().contains(fullAddress)));

        config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(1, config.getVersion());
        assertEquals("public", config.getReadCommunity());
        assertEquals("private", config.getWriteCommunity());
    }

    public void testRemoveSpecificIPv6LongAddress() {
        // add an IPv6 specific entry
        final String ipv6Address = "2001:db8:1:2:3:4:5:1";
        InetAddress addr = InetAddressUtils.addr(ipv6Address);

        SnmpPeerFactory.getInstance().saveDefinition(new Definition() {{
            setSpecifics(List.of(ipv6Address));
            setReadCommunity("read-ipv6-21");
            setVersion("v2c");
        }}, true);

        SnmpConfig snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertTrue(snmpConfig.getDefinitions().stream().anyMatch(d -> d.getSpecifics().contains(ipv6Address)));

        SnmpAgentConfig config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(2, config.getVersion());
        assertEquals("read-ipv6-21", config.getReadCommunity());

        // now delete it
        assertTrue(SnmpPeerFactory.getInstance().removeRangesFromDefinition(null, List.of(ipv6Address), null, "Default", "unit test"));

        final String expectedLogMessage =
                String.format("Removed %d ranges, %d specifics, %d ipMatches from definitions at location %s by module %s",
                        0, 1, 0, "Default", "unit test");

        MockLogAppender.assertLogMatched(Level.INFO, expectedLogMessage);

        snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertTrue(snmpConfig.getDefinitions().stream().noneMatch(d -> d.getSpecifics().contains(ipv6Address)));

        // config should have reverted to defaults
        config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(1, config.getVersion());
        assertEquals("public", config.getReadCommunity());
        assertEquals("private", config.getWriteCommunity());
    }

    public void testRemoveIpAddressRange() {
        // confirm definition with range exists
        SnmpConfig snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertTrue(
            snmpConfig.getDefinitions().stream()
                .filter(d -> d != null && d.getRanges() != null)
                .anyMatch(d -> d.getRanges().stream().anyMatch(r -> r.getBegin().equals("10.0.0.101") && r.getEnd().equals("10.0.0.200")))
        );

        // confirm entry exists
        InetAddress addr = InetAddressUtils.addr("10.0.0.101");

        SnmpAgentConfig config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(1, config.getVersion());
        assertEquals("rangev1", config.getReadCommunity());
        assertEquals(55, config.getMaxVarsPerPdu());

        // now delete the range
        List<Range> rangesToDelete = List.of(new Range("10.0.0.101", "10.0.0.200"));
        assertTrue(SnmpPeerFactory.getInstance().removeRangesFromDefinition( rangesToDelete, null, null, "Default", "unit test"));

        final String expectedLogMessage =
                String.format("Removed %d ranges, %d specifics, %d ipMatches from definitions at location %s by module %s",
                        1, 0, 0, "Default", "unit test");

        MockLogAppender.assertLogMatched(Level.INFO, expectedLogMessage);

        snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();

        assertFalse(
            snmpConfig.getDefinitions().stream()
                .filter(d -> d != null && d.getRanges() != null)
                .anyMatch(d -> d.getRanges().stream().anyMatch(r -> r.getBegin().equals("10.0.0.101") && r.getEnd().equals("10.0.0.200")))
        );

        // config should have reverted to defaults
        config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(1, config.getVersion());
        assertEquals("public", config.getReadCommunity());
        // note, this is set in the config defaults
        assertEquals(23, config.getMaxVarsPerPdu());
    }

    public void testRemoveRangeDefinitionThatDoesNotExist() {
        SnmpConfig originalConfig = SnmpPeerFactory.cloneConfig(SnmpPeerFactory.getInstance().getSnmpConfig());

        List<Range> rangesToDelete = List.of(new Range("99.0.0.1", "99.0.0.99"));
        assertFalse(SnmpPeerFactory.getInstance().removeRangesFromDefinition( rangesToDelete, null, null, "Default", "unit test"));
        SnmpConfig configAfterDelete = SnmpPeerFactory.getInstance().getSnmpConfig();

        // config should not have changed
        assertEquals(originalConfig, configAfterDelete);

        final String expectedLogMessage =
                String.format("No matching items found to remove for location %s by module %s", "Default", "unit test");

        MockLogAppender.assertLogMatched(Level.INFO, expectedLogMessage);
    }

    public void testRemoveRangeDefinitionsThatPartiallyExist() {
        SnmpConfig originalConfig = SnmpPeerFactory.cloneConfig(SnmpPeerFactory.getInstance().getSnmpConfig());

        // one range exists, the other does not
        // the one that does not exist should be ignored and the one that exists should be deleted
        List<Range> rangesToDelete = List.of(new Range("10.0.0.101", "10.0.0.200"), new Range("99.0.0.1", "99.0.0.99"));
        assertTrue(SnmpPeerFactory.getInstance().removeRangesFromDefinition( rangesToDelete, null, null, "Default", "unit test"));

        SnmpConfig configAfterDelete = SnmpPeerFactory.getInstance().getSnmpConfig();

        // config should have changed
        assertNotEquals(originalConfig, configAfterDelete);

        final String expectedLogMessage =
                String.format("Removed %d ranges, %d specifics, %d ipMatches from definitions at location %s by module %s",
                        1, 0, 0, "Default", "unit test");

        MockLogAppender.assertLogMatched(Level.INFO, expectedLogMessage);

        // the range that existed should have been deleted
        assertFalse(
            configAfterDelete.getDefinitions().stream()
                .anyMatch(d -> d.getRanges().stream().anyMatch(r -> r.getBegin().equals("10.0.0.101") && r.getEnd().equals("10.0.0.200")))
        );
    }

    public void testRemoveSpecificAndRangeDefinitionsThatPartiallyExist() {
        SnmpConfig originalConfig = SnmpPeerFactory.cloneConfig(SnmpPeerFactory.getInstance().getSnmpConfig());

        // one range exists, the other does not. specific exists
        List<Range> rangesToDelete = List.of(new Range("10.0.0.101", "10.0.0.200"), new Range("99.0.0.1", "99.0.0.99"));
        List<String> specificsToDelete = List.of("20.20.20.20");
        assertTrue(SnmpPeerFactory.getInstance().removeRangesFromDefinition( rangesToDelete, specificsToDelete, null, "Default", "unit test"));
        SnmpConfig configAfterDelete = SnmpPeerFactory.getInstance().getSnmpConfig();

        // config should have changed
        assertNotEquals(originalConfig, configAfterDelete);

        final String expectedLogMessage =
                String.format("Removed %d ranges, %d specifics, %d ipMatches from definitions at location %s by module %s",
                        1, 1, 0, "Default", "unit test");

        MockLogAppender.assertLogMatched(Level.INFO, expectedLogMessage);

        // the range that existed should have been deleted
        assertFalse(
                configAfterDelete.getDefinitions().stream()
                        .anyMatch(d -> d.getRanges().stream().anyMatch(r -> r.getBegin().equals("10.0.0.101") && r.getEnd().equals("10.0.0.200")))
        );

        // the specific that existed should have been deleted
        assertFalse(
                configAfterDelete.getDefinitions().stream()
                        .anyMatch(d -> d.getSpecifics().stream().anyMatch(s -> s.equals("20.20.20.20")))
        );
    }

    public void testRemoveIpMatchDefinition() {
        // Confirm ipMatch definition exists in Default location
        SnmpConfig snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertTrue(
            snmpConfig.getDefinitions().stream()
                .anyMatch(d -> d.getIpMatches() != null && d.getIpMatches().contains("77.5-12,15.1-255.255"))
        );

        // Verify configuration before removal
        InetAddress addr = InetAddressUtils.addr("77.10.1.255");
        SnmpAgentConfig config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(2, config.getVersion()); // v2c
        assertEquals("ipmatch", config.getReadCommunity());
        assertEquals(128, config.getMaxVarsPerPdu());

        // Now delete the ipMatch definition
        List<String> ipMatchesToDelete = List.of("77.5-12,15.1-255.255");
        assertTrue(SnmpPeerFactory.getInstance().removeRangesFromDefinition(null, null, ipMatchesToDelete, "Default", "unit test"));

        final String expectedLogMessage =
                String.format("Removed %d ranges, %d specifics, %d ipMatches from definitions at location %s by module %s",
                        0, 0, 1, "Default", "unit test");

        MockLogAppender.assertLogMatched(Level.INFO, expectedLogMessage);

        // Confirm ipMatch definition has been removed
        snmpConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertFalse(
            snmpConfig.getDefinitions().stream()
                .anyMatch(d -> d.getIpMatches() != null && d.getIpMatches().contains("77.5-12,15.1-255.255"))
        );

        // Config should have reverted to defaults
        config = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Default");
        assertNotNull(config);
        assertEquals(1, config.getVersion()); // reverts to v1 default
        assertEquals("public", config.getReadCommunity()); // reverts to default
        assertEquals(23, config.getMaxVarsPerPdu()); // reverts to config default
    }

    public void testRemoveIpMatchDefinitionThatDoesNotExist() {
        SnmpConfig originalConfig = SnmpPeerFactory.cloneConfig(SnmpPeerFactory.getInstance().getSnmpConfig());

        // Try to delete an ipMatch that doesn't exist
        List<String> ipMatchesToDelete = List.of("99.99.99.99");
        assertFalse(SnmpPeerFactory.getInstance().removeRangesFromDefinition(null, null, ipMatchesToDelete, "Default", "unit test"));

        SnmpConfig configAfterDelete = SnmpPeerFactory.getInstance().getSnmpConfig();

        // Config should not have changed
        assertEquals(originalConfig, configAfterDelete);

        final String expectedLogMessage =
                String.format("No matching items found to remove for location %s by module %s", "Default", "unit test");

        MockLogAppender.assertLogMatched(Level.INFO, expectedLogMessage);
    }

    public void testRemoveIpMatchDefinitionWithWrongLocation() {
        SnmpConfig originalConfig = SnmpPeerFactory.cloneConfig(SnmpPeerFactory.getInstance().getSnmpConfig());

        // Confirm ipMatch exists in Location-A
        assertTrue(
            originalConfig.getDefinitions().stream()
                .anyMatch(d -> "Location-A".equals(d.getLocation())
                    && d.getIpMatches() != null
                    && d.getIpMatches().contains("88.10-20.*.1-50"))
        );

        // Try to delete ipMatch from Location-A, but specify wrong location
        List<String> ipMatchesToDelete = List.of("88.10-20.*.1-50");
        assertFalse(SnmpPeerFactory.getInstance().removeRangesFromDefinition(null, null, ipMatchesToDelete, "Location-C", "unit test"));

        SnmpConfig configAfterDelete = SnmpPeerFactory.getInstance().getSnmpConfig();

        // Config should not have changed - ipMatch should still exist in Location-A
        assertTrue(
            configAfterDelete.getDefinitions().stream()
                .anyMatch(d -> "Location-A".equals(d.getLocation())
                    && d.getIpMatches() != null
                    && d.getIpMatches().contains("88.10-20.*.1-50"))
        );

        final String expectedLogMessage =
                String.format("No matching items found to remove for location %s by module %s", "Location-C", "unit test");

        MockLogAppender.assertLogMatched(Level.INFO, expectedLogMessage);
    }

    public void testRemoveIpMatchFromSpecificLocationOnly() {
        SnmpConfig originalConfig = SnmpPeerFactory.cloneConfig(SnmpPeerFactory.getInstance().getSnmpConfig());

        // Confirm same ipMatch exists in both Location-A and Location-B
        assertTrue(
            originalConfig.getDefinitions().stream()
                .anyMatch(d -> "Location-A".equals(d.getLocation())
                    && d.getIpMatches() != null
                    && d.getIpMatches().contains("88.10-20.*.1-50"))
        );
        assertTrue(
            originalConfig.getDefinitions().stream()
                .anyMatch(d -> "Location-B".equals(d.getLocation())
                    && d.getIpMatches() != null
                    && d.getIpMatches().contains("88.10-20.*.1-50"))
        );

        // Verify config in Location-A before removal
        InetAddress addr = InetAddressUtils.addr("88.15.100.25");
        SnmpAgentConfig configLocationA = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Location-A");
        assertEquals("ipmatch-location-a", configLocationA.getReadCommunity());
        assertEquals(64, configLocationA.getMaxVarsPerPdu());

        // Remove ipMatch from Location-A only
        List<String> ipMatchesToDelete = List.of("88.10-20.*.1-50");
        assertTrue(SnmpPeerFactory.getInstance().removeRangesFromDefinition(null, null, ipMatchesToDelete, "Location-A", "unit test"));

        final String expectedLogMessage =
                String.format("Removed %d ranges, %d specifics, %d ipMatches from definitions at location %s by module %s",
                        0, 0, 1, "Location-A", "unit test");

        MockLogAppender.assertLogMatched(Level.INFO, expectedLogMessage);

        SnmpConfig configAfterDelete = SnmpPeerFactory.getInstance().getSnmpConfig();

        // ipMatch should be removed from Location-A
        assertFalse(
            configAfterDelete.getDefinitions().stream()
                .anyMatch(d -> "Location-A".equals(d.getLocation())
                    && d.getIpMatches() != null
                    && d.getIpMatches().contains("88.10-20.*.1-50"))
        );

        // ipMatch should STILL exist in Location-B
        assertTrue(
            configAfterDelete.getDefinitions().stream()
                .anyMatch(d -> "Location-B".equals(d.getLocation())
                    && d.getIpMatches() != null
                    && d.getIpMatches().contains("88.10-20.*.1-50"))
        );

        // Verify config in Location-A has reverted to defaults after removal
        SnmpAgentConfig configAfterRemoval = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Location-A");
        assertEquals("public", configAfterRemoval.getReadCommunity()); // reverted to default
        assertEquals(23, configAfterRemoval.getMaxVarsPerPdu()); // reverted to config default

        // Verify config in Location-B is unchanged
        SnmpAgentConfig configLocationB = SnmpPeerFactory.getInstance().getAgentConfig(addr, "Location-B");
        assertEquals("ipmatch-location-b", configLocationB.getReadCommunity());
        assertEquals(32, configLocationB.getMaxVarsPerPdu());
    }

    public void testSaveNewProfile() {
        SnmpPeerFactory.setResource(new ByteArrayResource(getSnmpConfig().getBytes()));
        List<SnmpProfile> profiles = SnmpPeerFactory.getInstance().getProfiles();

        // confirm initial conditions - 2 profiles
        assertEquals(2, profiles.size());
        assertEquals(1, profiles.stream().filter(p -> p.getLabel().equals("profile1")).count());
        assertEquals(1, profiles.stream().filter(p -> p.getLabel().equals("profile2")).count());

        // Save a new profile
        final String newProfileLabel = "newProfile";
        final Integer newProfilePort = 199;
        final String newProfileReadCommunity = "read3";
        final String newProfileWriteCommunity = "write3";
        final String newProfileFilter = "filter3";

        final SnmpProfile newProfile = new SnmpProfile(
            newProfilePort,
            null,
            null,
            newProfileReadCommunity,
            newProfileWriteCommunity,
            "",
            "v2c",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            newProfileLabel,
            newProfileFilter
        );

        SnmpPeerFactory.getInstance().saveProfile(newProfile);
        profiles = SnmpPeerFactory.getInstance().getProfiles();

        // make sure profile was added and did not replace others
        assertEquals(3, profiles.size());
        assertEquals(1, profiles.stream().filter(p -> p.getLabel().equals("profile1")).count());
        assertEquals(1, profiles.stream().filter(p -> p.getLabel().equals("profile2")).count());
        assertEquals(1, profiles.stream().filter(p -> p.getLabel().equals(newProfileLabel)).count());

        // check new profile for correct info
        final SnmpProfile addedProfile = profiles.stream().filter(p -> p.getLabel().equals(newProfileLabel)).findFirst().orElse(null);

        assertNotNull(addedProfile);
        assertEquals(newProfileLabel, addedProfile.getLabel());
        assertEquals(newProfileFilter, addedProfile.getFilterExpression());

        assertNotNull(addedProfile.getPort());
        assertEquals(newProfilePort.intValue(), addedProfile.getPort().intValue());

        assertEquals(newProfileReadCommunity, addedProfile.getReadCommunity());
        assertEquals(newProfileWriteCommunity, addedProfile.getWriteCommunity());
    }

    public void testSaveUpdatedProfile() {
        SnmpPeerFactory.setResource(new ByteArrayResource(getSnmpConfig().getBytes()));
        List<SnmpProfile> profiles = SnmpPeerFactory.getInstance().getProfiles();

        // confirm initial conditions
        assertEquals(2, profiles.size());
        assertEquals(1, profiles.stream().filter(p -> p.getLabel().equals("profile1")).count());
        assertEquals(1, profiles.stream().filter(p -> p.getLabel().equals("profile2")).count());

        // Save an updated profile
        final String updatedProfileLabel = "profile2";
        final Integer updatedProfilePort = 199;
        final String updatedProfileReadCommunity = "read222";
        final String updatedProfileWriteCommunity = "write222";
        final String updatedProfileFilter = "filter222";

        final SnmpProfile updatedProfile = new SnmpProfile(
                updatedProfilePort,
                null,
                null,
                updatedProfileReadCommunity,
                updatedProfileWriteCommunity,
                "",
                "v2c",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                updatedProfileLabel,
                updatedProfileFilter
        );

        SnmpPeerFactory.getInstance().saveProfile(updatedProfile);
        profiles = SnmpPeerFactory.getInstance().getProfiles();

        // make sure profile replaced existing one
        assertEquals(2, profiles.size());
        assertEquals(1, profiles.stream().filter(p -> p.getLabel().equals("profile1")).count());
        assertEquals(1, profiles.stream().filter(p -> p.getLabel().equals("profile2")).count());

        // check updated profile for correct info
        final SnmpProfile profile = profiles.stream().filter(p -> p.getLabel().equals(updatedProfileLabel)).findFirst().orElse(null);

        assertNotNull(profile);
        assertEquals(updatedProfileLabel, profile.getLabel());
        assertEquals(updatedProfileFilter, profile.getFilterExpression());

        assertNotNull(profile.getPort());
        assertEquals(updatedProfilePort.intValue(), profile.getPort().intValue());

        assertEquals(updatedProfileReadCommunity, profile.getReadCommunity());
        assertEquals(updatedProfileWriteCommunity, profile.getWriteCommunity());
    }

    public void testRemoveProfile() {
        SnmpPeerFactory.setResource(new ByteArrayResource(getSnmpConfig().getBytes()));
        List<SnmpProfile> profiles = SnmpPeerFactory.getInstance().getProfiles();

        // confirm initial conditions
        assertEquals(2, profiles.size());
        assertEquals(1, profiles.stream().filter(p -> p.getLabel().equals("profile1")).count());
        assertEquals(1, profiles.stream().filter(p -> p.getLabel().equals("profile2")).count());

        final String profileToRemove = "profile2";
        boolean succeeded = SnmpPeerFactory.getInstance().removeProfile(profileToRemove);

        assertTrue(succeeded);

        profiles = SnmpPeerFactory.getInstance().getProfiles();

        // make sure profile was removed
        assertEquals(1, profiles.size());
        assertEquals(1, profiles.stream().filter(p -> p.getLabel().equals("profile1")).count());
        assertEquals(0, profiles.stream().filter(p -> p.getLabel().equals("profile2")).count());

        final String nonExistentProfile = "profile999";
        succeeded = SnmpPeerFactory.getInstance().removeProfile(nonExistentProfile);

        assertFalse(succeeded);

        profiles = SnmpPeerFactory.getInstance().getProfiles();

        // make sure nothing was changed
        assertEquals(1, profiles.size());
        assertEquals(1, profiles.stream().filter(p -> p.getLabel().equals("profile1")).count());
        assertEquals(0, profiles.stream().filter(p -> p.getLabel().equals("profile2")).count());
    }

    public void testSaveDefaultOverrides_ReplaceExistingValues() {
        SnmpPeerFactory.setResource(new ByteArrayResource(getSnmpConfig().getBytes()));

        // Get initial config values
        SnmpConfig initialConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertEquals("public", initialConfig.getReadCommunity());
        assertEquals("private", initialConfig.getWriteCommunity());
        assertEquals(Integer.valueOf(3), initialConfig.getRetry());
        assertEquals(Integer.valueOf(3000), initialConfig.getTimeout());
        assertEquals(Integer.valueOf(161), initialConfig.getPort());
        assertEquals("v1", initialConfig.getVersion());

        // Create new configuration with different values
        Configuration newConfig = new Configuration(
                9161,          // port
                5,             // retry
                5000,          // timeout
                "newPublic",   // readCommunity
                "newPrivate",  // writeCommunity
                "proxy.example.com", // proxyHost
                "v2c",         // version
                15,            // maxVarsPerPdu
                3,             // maxRepetitions
                1000,          // maxRequestSize
                "newSecName",  // securityName
                3,             // securityLevel
                "newAuthPass", // authPassphrase
                "SHA",         // authProtocol
                "newEngineId", // engineId
                "newContextEngineId", // contextEngineId
                "newContext",  // contextName
                "newPrivPass", // privacyPassphrase
                "AES",         // privacyProtocol
                "newEntId"     // enterpriseId
        );

        newConfig.setTTL(20L);

        // Save the overrides
        SnmpPeerFactory.getInstance().saveDefaultOverrides(newConfig);

        // Verify all values were updated
        SnmpConfig updatedConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertEquals(Integer.valueOf(9161), updatedConfig.getPort());
        assertEquals(Integer.valueOf(5), updatedConfig.getRetry());
        assertEquals(Integer.valueOf(5000), updatedConfig.getTimeout());
        assertEquals("newPublic", updatedConfig.getReadCommunity());
        assertEquals("newPrivate", updatedConfig.getWriteCommunity());
        assertEquals("proxy.example.com", updatedConfig.getProxyHost());
        assertEquals("v2c", updatedConfig.getVersion());
        assertEquals(Integer.valueOf(15), updatedConfig.getMaxVarsPerPdu());
        assertEquals(Integer.valueOf(3), updatedConfig.getMaxRepetitions());
        assertEquals(Integer.valueOf(1000), updatedConfig.getMaxRequestSize());
        assertEquals("newSecName", updatedConfig.getSecurityName());
        assertEquals(Integer.valueOf(3), updatedConfig.getSecurityLevel());
        assertEquals("newAuthPass", updatedConfig.getAuthPassphrase());
        assertEquals("SHA", updatedConfig.getAuthProtocol());
        assertEquals("newEngineId", updatedConfig.getEngineId());
        assertEquals("newContextEngineId", updatedConfig.getContextEngineId());
        assertEquals("newContext", updatedConfig.getContextName());
        assertEquals("newPrivPass", updatedConfig.getPrivacyPassphrase());
        assertEquals("AES", updatedConfig.getPrivacyProtocol());
        assertEquals("newEntId", updatedConfig.getEnterpriseId());
        assertEquals(20L, updatedConfig.getTTL().longValue());
    }

    public void testSaveDefaultOverrides_SetExistingValuesToNull() {
        SnmpPeerFactory.setResource(new ByteArrayResource(getSnmpConfig().getBytes()));

        // Get initial config values - verify they're not null
        SnmpConfig initialConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertNotNull(initialConfig.getReadCommunity());
        assertNotNull(initialConfig.getWriteCommunity());
        assertNotNull(initialConfig.getRetry());
        assertNotNull(initialConfig.getTimeout());

        // Create new configuration with null values
        Configuration newConfig = new Configuration(
                null,  // port
                null,  // retry
                null,  // timeout
                null,  // readCommunity
                null,  // writeCommunity
                null,  // proxyHost
                null,  // version
                null,  // maxVarsPerPdu
                null,  // maxRepetitions
                null,  // maxRequestSize
                null,  // securityName
                null,  // securityLevel
                null,  // authPassphrase
                null,  // authProtocol
                null,  // engineId
                null,  // contextEngineId
                null,  // contextName
                null,  // privacyPassphrase
                null,  // privacyProtocol
                null   // enterpriseId
        );

        // Save the overrides
        SnmpPeerFactory.getInstance().saveDefaultOverrides(newConfig);

        // Verify all nullable values were set to null
        SnmpConfig updatedConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertNull(updatedConfig.getReadCommunity());
        assertNull(updatedConfig.getWriteCommunity());
        assertNull(updatedConfig.getProxyHost());
        assertNull(updatedConfig.getVersion());
        assertNull(updatedConfig.getSecurityName());
        assertNull(updatedConfig.getAuthPassphrase());
        assertNull(updatedConfig.getAuthProtocol());
        assertNull(updatedConfig.getEngineId());
        assertNull(updatedConfig.getContextEngineId());
        assertNull(updatedConfig.getContextName());
        assertNull(updatedConfig.getPrivacyPassphrase());
        assertNull(updatedConfig.getPrivacyProtocol());
        assertNull(updatedConfig.getEnterpriseId());
        assertNull(updatedConfig.getTTL());
    }

    public void testSaveDefaultOverrides_MixNullAndNonNull() {
        SnmpPeerFactory.setResource(new ByteArrayResource(getSnmpConfig().getBytes()));

        // Get initial config values
        SnmpConfig initialConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertEquals("public", initialConfig.getReadCommunity());
        assertEquals("private", initialConfig.getWriteCommunity());
        assertEquals(Integer.valueOf(3), initialConfig.getRetry());

        // Create configuration with mix of null and non-null values
        Configuration newConfig = new Configuration(
                8161,          // port - non-null
                null,          // retry - null
                4000,          // timeout - non-null
                "updatedRead", // readCommunity - non-null
                null,          // writeCommunity - null
                null,          // proxyHost - null
                "v3",          // version - non-null
                null,          // maxVarsPerPdu - null
                5,             // maxRepetitions - non-null
                null,          // maxRequestSize - null
                "someSecName", // securityName - non-null
                null,          // securityLevel - null
                null,          // authPassphrase - null
                "MD5",         // authProtocol - non-null
                null,          // engineId - null
                null,          // contextEngineId - null
                "ctx1",        // contextName - non-null
                null,          // privacyPassphrase - null
                null,          // privacyProtocol - null
                null           // enterpriseId - null
        );

        // Save the overrides
        SnmpPeerFactory.getInstance().saveDefaultOverrides(newConfig);

        // Verify mixed values
        SnmpConfig updatedConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertEquals(Integer.valueOf(8161), updatedConfig.getPort());
        assertEquals(Integer.valueOf(0), updatedConfig.getRetry()); // getRetry() returns 0 when null
        assertEquals(Integer.valueOf(4000), updatedConfig.getTimeout());
        assertEquals("updatedRead", updatedConfig.getReadCommunity());
        assertNull(updatedConfig.getWriteCommunity());
        assertNull(updatedConfig.getProxyHost());
        assertEquals("v3", updatedConfig.getVersion());
        assertEquals(Integer.valueOf(5), updatedConfig.getMaxRepetitions());
        assertEquals("someSecName", updatedConfig.getSecurityName());
        assertNull(updatedConfig.getSecurityLevel());
        assertNull(updatedConfig.getAuthPassphrase());
        assertEquals("MD5", updatedConfig.getAuthProtocol());
        assertNull(updatedConfig.getEngineId());
        assertNull(updatedConfig.getContextEngineId());
        assertEquals("ctx1", updatedConfig.getContextName());
        assertNull(updatedConfig.getPrivacyPassphrase());
        assertNull(updatedConfig.getPrivacyProtocol());
        assertNull(updatedConfig.getEnterpriseId());
        assertNull(updatedConfig.getTTL());
    }

    public void testSaveDefaultOverrides_ReplaceNullWithNonNull() {
        SnmpPeerFactory.setResource(new ByteArrayResource(getSnmpConfig().getBytes()));

        // First set everything to null
        Configuration nullConfig = new Configuration(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
        );
        SnmpPeerFactory.getInstance().saveDefaultOverrides(nullConfig);

        // Verify nulls
        SnmpConfig configWithNulls = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertNull(configWithNulls.getReadCommunity());
        assertNull(configWithNulls.getVersion());
        assertNull(configWithNulls.getSecurityName());

        // Now replace null values with non-null values
        Configuration nonNullConfig = new Configuration(
                162,           // port
                4,             // retry
                4000,          // timeout
                "newRead",     // readCommunity
                "newWrite",    // writeCommunity
                "newProxy",    // proxyHost
                "v2c",         // version
                20,            // maxVarsPerPdu
                4,             // maxRepetitions
                2000,          // maxRequestSize
                "secName",     // securityName
                2,             // securityLevel
                "authPass",    // authPassphrase
                "SHA",         // authProtocol
                "engineId",    // engineId
                "ctxEngineId", // contextEngineId
                "ctxName",     // contextName
                "privPass",    // privacyPassphrase
                "DES",         // privacyProtocol
                "entId"        // enterpriseId
        );

        nonNullConfig.setTTL(20L);

        SnmpPeerFactory.getInstance().saveDefaultOverrides(nonNullConfig);

        // Verify all values are now set
        SnmpConfig updatedConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertEquals(Integer.valueOf(162), updatedConfig.getPort());
        assertEquals(Integer.valueOf(4), updatedConfig.getRetry());
        assertEquals(Integer.valueOf(4000), updatedConfig.getTimeout());
        assertEquals("newRead", updatedConfig.getReadCommunity());
        assertEquals("newWrite", updatedConfig.getWriteCommunity());
        assertEquals("newProxy", updatedConfig.getProxyHost());
        assertEquals("v2c", updatedConfig.getVersion());
        assertEquals(Integer.valueOf(20), updatedConfig.getMaxVarsPerPdu());
        assertEquals(Integer.valueOf(4), updatedConfig.getMaxRepetitions());
        assertEquals(Integer.valueOf(2000), updatedConfig.getMaxRequestSize());
        assertEquals("secName", updatedConfig.getSecurityName());
        assertEquals(Integer.valueOf(2), updatedConfig.getSecurityLevel());
        assertEquals("authPass", updatedConfig.getAuthPassphrase());
        assertEquals("SHA", updatedConfig.getAuthProtocol());
        assertEquals("engineId", updatedConfig.getEngineId());
        assertEquals("ctxEngineId", updatedConfig.getContextEngineId());
        assertEquals("ctxName", updatedConfig.getContextName());
        assertEquals("privPass", updatedConfig.getPrivacyPassphrase());
        assertEquals("DES", updatedConfig.getPrivacyProtocol());
        assertEquals("entId", updatedConfig.getEnterpriseId());
        assertEquals(20L, updatedConfig.getTTL().longValue());
    }

    public void testSaveDefaultOverrides_PreservesDefinitions() {
        SnmpPeerFactory.setResource(new ByteArrayResource(getSnmpConfig().getBytes()));

        // Get initial config and count definitions
        SnmpConfig initialConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        int initialDefinitionCount = initialConfig.getDefinitions().size();
        assertTrue(initialDefinitionCount > 0);

        // Save new default overrides
        Configuration newConfig = new Configuration(
                9999,        // port
                10,          // retry
                10000,       // timeout
                "testRead",  // readCommunity
                "testWrite", // writeCommunity
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        SnmpPeerFactory.getInstance().saveDefaultOverrides(newConfig);

        // Verify defaults were updated but definitions remain
        SnmpConfig updatedConfig = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertEquals(Integer.valueOf(9999), updatedConfig.getPort());
        assertEquals(Integer.valueOf(10), updatedConfig.getRetry());
        assertEquals(Integer.valueOf(10000), updatedConfig.getTimeout());
        assertEquals("testRead", updatedConfig.getReadCommunity());
        assertEquals("testWrite", updatedConfig.getWriteCommunity());

        // Definitions should be preserved
        assertEquals(initialDefinitionCount, updatedConfig.getDefinitions().size());
    }

    public void testSaveDefaultOverrides_MultipleUpdates() {
        SnmpPeerFactory.setResource(new ByteArrayResource(getSnmpConfig().getBytes()));

        // First update
        Configuration config1 = new Configuration(
                100, 1, 1000, "read1", "write1",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        SnmpPeerFactory.getInstance().saveDefaultOverrides(config1);
        SnmpConfig updatedConfig1 = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertEquals("read1", updatedConfig1.getReadCommunity());
        assertEquals(Integer.valueOf(100), updatedConfig1.getPort());

        // Second update
        Configuration config2 = new Configuration(
                200, 2, 2000, "read2", "write2",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        SnmpPeerFactory.getInstance().saveDefaultOverrides(config2);
        SnmpConfig updatedConfig2 = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertEquals("read2", updatedConfig2.getReadCommunity());
        assertEquals(Integer.valueOf(200), updatedConfig2.getPort());
        assertEquals(Integer.valueOf(2), updatedConfig2.getRetry());

        // Third update - set some to null
        Configuration config3 = new Configuration(
                300, null, 3000, null, "write3",
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        SnmpPeerFactory.getInstance().saveDefaultOverrides(config3);
        SnmpConfig updatedConfig3 = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertNull(updatedConfig3.getReadCommunity());
        assertEquals(Integer.valueOf(0), updatedConfig3.getRetry()); // getRetry() returns 0 when null
        assertEquals("write3", updatedConfig3.getWriteCommunity());
        assertEquals(Integer.valueOf(300), updatedConfig3.getPort());
        assertEquals(Integer.valueOf(3000), updatedConfig3.getTimeout());
    }

    public void testSaveDefaultOverrides_NullConfig() {
        SnmpPeerFactory.setResource(new ByteArrayResource(getSnmpConfig().getBytes()));
        SnmpConfig originalConfig = SnmpPeerFactory.cloneConfig(SnmpPeerFactory.getInstance().getSnmpConfig());

        SnmpPeerFactory.getInstance().saveDefaultOverrides(null);

        SnmpConfig updatedConfig1 = SnmpPeerFactory.getInstance().getSnmpConfig();
        assertEquals(originalConfig, updatedConfig1); // config should not have changed
    }

    // NOTE: There is no test for saveDefaultOverrides when it receives a configuration that does not pass
    // schema validation because it is overly complex to mock SnmpPeerFactory.snmpConfigDao just for testing,
    // since we would have to add additional logic to SnmpPeerFactory.setResource() to allow injecting a mock dao that
    // throws a fake ValidationException.
    // However, this functionality *is* covered by the SnmpPeerFactoryTest.testSaveDefaultOverrides_InvalidConfig test.
}
