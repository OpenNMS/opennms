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
import org.mockito.Mockito;
import org.opennms.core.test.MockLogAppender;
import org.opennms.systemreport.sanitizer.ConfigFileSanitizer;
import org.opennms.systemreport.sanitizer.ConfigurationSanitizer;
import org.opennms.systemreport.sanitizer.FileSanitizationException;
import org.springframework.core.io.ByteArrayResource;

import java.io.File;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;

public class ConfigurationSanitizerTest {
    private ConfigurationSanitizer configurationSanitizer;
    private ConfigFileSanitizer xmlFileSanitizer;
    private ConfigFileSanitizer propertiesFileSanitizer;

    public ConfigurationSanitizerTest() {
        final Properties props = new Properties();
        props.put("log4j.logger.org.opennms.systemreport.system", "DEBUG");
        MockLogAppender.setupLogging(true, "DEBUG", props);
    }

    @Before
    public void setUp() throws Exception {
        xmlFileSanitizer = Mockito.mock(ConfigFileSanitizer.class);
        Mockito.when(xmlFileSanitizer.getFileType()).thenReturn("xml");
        Mockito.when(xmlFileSanitizer.getSanitizedResource(any())).thenReturn(new ByteArrayResource(new byte[0]));

        propertiesFileSanitizer = Mockito.mock(ConfigFileSanitizer.class);
        Mockito.when(propertiesFileSanitizer.getFileType()).thenReturn("properties");
        Mockito.when(propertiesFileSanitizer.getSanitizedResource(any())).thenReturn(new ByteArrayResource(new byte[0]));

        configurationSanitizer = new ConfigurationSanitizer(xmlFileSanitizer, propertiesFileSanitizer);
    }

    @Test
    public void testSanitizesXml() throws FileSanitizationException {
        File file = new File("mock/password-attributes.xml");
        configurationSanitizer.getSanitizedResource(file);
        Mockito.verify(xmlFileSanitizer).getSanitizedResource(file);
    }

    @Test
    public void testSanitizesProperties() throws FileSanitizationException {
        File file = new File("mock/password-properties.properties");
        configurationSanitizer.getSanitizedResource(file);
        Mockito.verify(propertiesFileSanitizer).getSanitizedResource(file);
    }
}
