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
package org.opennms.systemreport.system;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.test.MockLogAppender;
import org.opennms.systemreport.sanitizer.FileSanitizationException;
import org.opennms.systemreport.sanitizer.XmlFileSanitizer;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Properties;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class XmlFileSanitizerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private XmlFileSanitizer xmlFileSanitizer;

    public XmlFileSanitizerTest() {
        final Properties props = new Properties();
        props.put("log4j.logger.org.opennms.systemreport.system", "DEBUG");
        MockLogAppender.setupLogging(true, "DEBUG", props);
    }

    @Before
    public void setUp() throws Exception {
        xmlFileSanitizer = new XmlFileSanitizer();
    }

    @Test
    public void testSanitizesPasswordAttributes() throws FileSanitizationException, IOException {
        File file = new File("target/test-classes/mock/password-attributes.xml");
        Resource result = xmlFileSanitizer.getSanitizedResource(file);
        String content = new BufferedReader(new InputStreamReader(result.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        assertFalse(content.contains("secretValue"));
    }

    @Test
    public void testSanitizesPasswordElements() throws FileSanitizationException, IOException {
        File file = new File("target/test-classes/mock/password-elements.xml");
        Resource result = xmlFileSanitizer.getSanitizedResource(file);
        String content = new BufferedReader(new InputStreamReader(result.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        assertFalse(content.contains("secretValue"));
    }

    @Test
    public void testSanitizesPasswordParams() throws FileSanitizationException, IOException {
        File file = new File("target/test-classes/mock/password-params.xml");
        Resource result = xmlFileSanitizer.getSanitizedResource(file);
        String content = new BufferedReader(new InputStreamReader(result.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        assertFalse(content.contains("secretValue"));
    }

    @Test
    public void testSanitizesSnmpConfig() throws FileSanitizationException, IOException {
        File file = new File("target/test-classes/mock/snmp-config.xml");
        Resource result = xmlFileSanitizer.getSanitizedResource(file);
        String content = new BufferedReader(new InputStreamReader(result.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        assertFalse(content.contains("secretValue"));
    }

    @Test
    public void testSanitizesTrapdConfiguration() throws FileSanitizationException, IOException {
        File file = new File("target/test-classes/mock/trapd-configuration.xml");
        Resource result = xmlFileSanitizer.getSanitizedResource(file);
        String content = new BufferedReader(new InputStreamReader(result.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        assertFalse(content.contains("secretValue"));
    }

    @Test
    public void testSanitizesHttpDatacollectionConfig() throws FileSanitizationException, IOException {
        File file = new File("target/test-classes/mock/http-datacollection-config.xml");
        Resource result = xmlFileSanitizer.getSanitizedResource(file);
        String content = new BufferedReader(new InputStreamReader(result.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        assertFalse(content.contains("secretValue"));
    }

    @Test(expected = FileSanitizationException.class)
    public void testInvalidXmlThrowsException() throws FileSanitizationException {
        File file = new File("target/test-classes/mock/invalid.xml");
        xmlFileSanitizer.getSanitizedResource(file);
    }

    @Test
    public void testExternalEntityIsNotResolved() throws Exception {
        final File secret = temporaryFolder.newFile("secret.txt");
        Files.writeString(secret.toPath(), "secretFileContent");
        final File file = writeXml("<!DOCTYPE root [<!ENTITY xxe SYSTEM \"" + secret.toURI() + "\">]>"
                + "<root><description>&xxe;</description></root>");
        final String content = sanitize(file);
        assertFalse(content.contains("secretFileContent"));
    }

    @Test
    public void testJettyDoctypeIsAccepted() throws Exception {
        final File file = writeXml("<!DOCTYPE Configure PUBLIC \"-//Jetty//Configure//EN\" \"http://www.eclipse.org/jetty/configure.dtd\">"
                + "<Configure id=\"Server\"/>");
        final String content = sanitize(file);
        assertTrue(content.contains("<Configure id=\"Server\""));
    }

    @Test
    public void testPasswordInInternalEntityIsSanitized() throws Exception {
        final File file = writeXml("<!DOCTYPE root [<!ENTITY creds \"<password>secretValue</password>\">]>"
                + "<root>&creds;</root>");
        final String content = sanitize(file);
        assertFalse(content.contains("secretValue"));
    }

    @Test
    public void testEntityExpansionIsLimited() throws Exception {
        // 10^10 expansions of entity a0. The parser must stop at its expansion limit.
        final File file = writeXml("<!DOCTYPE root ["
                + "<!ENTITY a0 \"aaaaaaaaaa\">"
                + "<!ENTITY a1 \"&a0;&a0;&a0;&a0;&a0;&a0;&a0;&a0;&a0;&a0;\">"
                + "<!ENTITY a2 \"&a1;&a1;&a1;&a1;&a1;&a1;&a1;&a1;&a1;&a1;\">"
                + "<!ENTITY a3 \"&a2;&a2;&a2;&a2;&a2;&a2;&a2;&a2;&a2;&a2;\">"
                + "<!ENTITY a4 \"&a3;&a3;&a3;&a3;&a3;&a3;&a3;&a3;&a3;&a3;\">"
                + "<!ENTITY a5 \"&a4;&a4;&a4;&a4;&a4;&a4;&a4;&a4;&a4;&a4;\">"
                + "<!ENTITY a6 \"&a5;&a5;&a5;&a5;&a5;&a5;&a5;&a5;&a5;&a5;\">"
                + "<!ENTITY a7 \"&a6;&a6;&a6;&a6;&a6;&a6;&a6;&a6;&a6;&a6;\">"
                + "<!ENTITY a8 \"&a7;&a7;&a7;&a7;&a7;&a7;&a7;&a7;&a7;&a7;\">"
                + "<!ENTITY a9 \"&a8;&a8;&a8;&a8;&a8;&a8;&a8;&a8;&a8;&a8;\">"
                + "]><root>&a9;</root>");
        try {
            xmlFileSanitizer.getSanitizedResource(file);
            fail("The sanitizer must reject a file that exceeds the entity expansion limit");
        } catch (FileSanitizationException e) {
            assertTrue(e.getCause().getMessage(), e.getCause().getMessage().contains("entity expansions"));
        }
    }

    private File writeXml(final String xml) throws IOException {
        final File file = temporaryFolder.newFile("test.xml");
        Files.writeString(file.toPath(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xml);
        return file;
    }

    private String sanitize(final File file) throws FileSanitizationException, IOException {
        final Resource result = xmlFileSanitizer.getSanitizedResource(file);
        return new BufferedReader(new InputStreamReader(result.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
    }
}
