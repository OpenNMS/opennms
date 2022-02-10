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
import org.opennms.systemreport.sanitizer.PropertiesFileSanitizer;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertFalse;

public class PropertiesFileSanitizerTest {
    private PropertiesFileSanitizer propertiesFileSanitizer;

    public PropertiesFileSanitizerTest() {
        final Properties props = new Properties();
        props.put("log4j.logger.org.opennms.systemreport.system", "DEBUG");
        MockLogAppender.setupLogging(true, "DEBUG", props);
    }

    @Before
    public void setUp() {
        propertiesFileSanitizer = new PropertiesFileSanitizer();
    }

    @Test
    public void testSanitizesPasswords() throws FileSanitizationException, IOException {
        File file = new File("target/test-classes/mock/password-properties.properties");
        Resource result = propertiesFileSanitizer.getSanitizedResource(file);
        String content = new BufferedReader(new InputStreamReader(result.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        assertFalse(content.contains("secretValue"));
    }

}
