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
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.systemreport.sanitizer.FileSanitizationException;
import org.opennms.systemreport.sanitizer.XmlFileSanitizer;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertFalse;

public class XmlFileSanitizerTest {
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
}
