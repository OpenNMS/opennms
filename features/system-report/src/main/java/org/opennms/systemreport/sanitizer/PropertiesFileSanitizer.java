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
package org.opennms.systemreport.sanitizer;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class PropertiesFileSanitizer implements ConfigFileSanitizer {

    private static final Set<String> PROPERTIES_TO_SANITIZE = new LinkedHashSet<>(Arrays.asList("password", "pass", "authenticatePassword", "truststorePassword"));

    protected final String SANITIZED_VALUE = "***";

    @Override
    public String getFileName() {
        return "*.properties";
    }

    @Override
    public Resource getSanitizedResource(final File file) throws FileSanitizationException {
        try (InputStream fis = new FileInputStream(file)) {
            Properties properties = new Properties();
            properties.load(fis);

            sanitizeProperties(properties);

            return new ByteArrayResource(getOutput(properties).getBytes());
        } catch (Exception e) {
            throw new FileSanitizationException("Could not sanitize file", e);
        }
    }

    protected void sanitizeProperties(Properties properties) {
        properties.stringPropertyNames().forEach(propertyName -> {
            String lastPart = propertyName.substring(propertyName.lastIndexOf(".") + 1);
            if (PROPERTIES_TO_SANITIZE.contains(lastPart)) {
                properties.setProperty(propertyName, SANITIZED_VALUE);
            }
        });
    }

    protected String getOutput(Properties properties) {
        return properties.entrySet().stream()
                .map((entry) -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

}
