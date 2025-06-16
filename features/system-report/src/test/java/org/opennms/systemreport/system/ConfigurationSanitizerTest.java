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
import org.mockito.Mockito;
import org.opennms.core.test.MockLogAppender;
import org.opennms.systemreport.sanitizer.ConfigFileSanitizer;
import org.opennms.systemreport.sanitizer.ConfigurationSanitizer;
import org.opennms.systemreport.sanitizer.FileSanitizationException;
import org.springframework.core.io.ByteArrayResource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mockito.Matchers.any;

public class ConfigurationSanitizerTest {
    private ConfigurationSanitizer configurationSanitizer;
    private ConfigFileSanitizer xmlFileSanitizer;
    private ConfigFileSanitizer usersPropertiesFileSanitizer;

    public ConfigurationSanitizerTest() {
        final Properties props = new Properties();
        props.put("log4j.logger.org.opennms.systemreport.system", "DEBUG");
        MockLogAppender.setupLogging(true, "DEBUG", props);
    }

    @Before
    public void setUp() throws Exception {
        xmlFileSanitizer = Mockito.mock(ConfigFileSanitizer.class);
        Mockito.when(xmlFileSanitizer.getFileName()).thenReturn("*.xml");
        Mockito.when(xmlFileSanitizer.getSanitizedResource(any())).thenReturn(new ByteArrayResource(new byte[0]));

        usersPropertiesFileSanitizer = Mockito.mock(ConfigFileSanitizer.class);
        Mockito.when(usersPropertiesFileSanitizer.getFileName()).thenReturn("users.properties");
        Mockito.when(usersPropertiesFileSanitizer.getSanitizedResource(any())).thenReturn(new ByteArrayResource(new byte[0]));

        List<ConfigFileSanitizer> sanitizerList = new ArrayList<>();
        sanitizerList.add(xmlFileSanitizer);
        sanitizerList.add(usersPropertiesFileSanitizer);

        configurationSanitizer = new ConfigurationSanitizer(sanitizerList);
    }

    @Test
    public void testSanitizesExtension() throws FileSanitizationException {
        File file = new File("target/test-classes/mock/password-attributes.xml");
        configurationSanitizer.getSanitizedResource(file);
        Mockito.verify(xmlFileSanitizer).getSanitizedResource(file);
    }

    @Test
    public void testSanitizesSpecificFile() throws FileSanitizationException {
        File file = new File("target/test-classes/mock/users.properties");
        configurationSanitizer.getSanitizedResource(file);
        Mockito.verify(usersPropertiesFileSanitizer).getSanitizedResource(file);
    }
}
