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
