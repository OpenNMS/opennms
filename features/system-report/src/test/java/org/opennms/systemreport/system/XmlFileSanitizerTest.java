/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.systemreport.system;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.systemreport.sanitizer.FileSanitizationException;
import org.opennms.systemreport.sanitizer.XmlFileSanitizer;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

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
        String content = new String(result.getInputStream().readAllBytes());
        assertFalse(content.contains("secretValue"));
    }

    @Test
    public void testSanitizesPasswordElements() throws FileSanitizationException, IOException {
        File file = new File("target/test-classes/mock/password-elements.xml");
        Resource result = xmlFileSanitizer.getSanitizedResource(file);
        String content = new String(result.getInputStream().readAllBytes());
        assertFalse(content.contains("secretValue"));
    }

    @Test
    public void testSanitizesPasswordParams() throws FileSanitizationException, IOException {
        File file = new File("target/test-classes/mock/password-params.xml");
        Resource result = xmlFileSanitizer.getSanitizedResource(file);
        String content = new String(result.getInputStream().readAllBytes());
        assertFalse(content.contains("secretValue"));
    }

    @Test
    public void testSanitizesSnmpConfig() throws FileSanitizationException, IOException {
        File file = new File("target/test-classes/mock/snmp-config.xml");
        Resource result = xmlFileSanitizer.getSanitizedResource(file);
        String content = new String(result.getInputStream().readAllBytes());
        assertFalse(content.contains("secretValue"));
    }

    @Test
    public void testSanitizesTrapdConfiguration() throws FileSanitizationException, IOException {
        File file = new File("target/test-classes/mock/trapd-configuration.xml");
        Resource result = xmlFileSanitizer.getSanitizedResource(file);
        String content = new String(result.getInputStream().readAllBytes());
        assertFalse(content.contains("secretValue"));
    }

    @Test
    public void testSanitizesHttpDatacollectionConfig() throws FileSanitizationException, IOException {
        File file = new File("target/test-classes/mock/http-datacollection-config.xml");
        Resource result = xmlFileSanitizer.getSanitizedResource(file);
        String content = new String(result.getInputStream().readAllBytes());
        assertFalse(content.contains("secretValue"));
    }

    @Test(expected = FileSanitizationException.class)
    public void testInvalidXmlThrowsException() throws FileSanitizationException {
        File file = new File("target/test-classes/mock/invalid.xml");
        xmlFileSanitizer.getSanitizedResource(file);
    }
}
