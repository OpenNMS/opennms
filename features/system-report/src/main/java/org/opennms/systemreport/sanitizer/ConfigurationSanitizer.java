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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationSanitizer {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationSanitizer.class);

    private final Map<String, ConfigFileSanitizer> sanitizers;

    @Autowired
    public ConfigurationSanitizer(Collection<ConfigFileSanitizer> configFileSanitizerList) {
        Map<String, ConfigFileSanitizer> modifiableMap = new HashMap<>();

        for (ConfigFileSanitizer sanitizer : configFileSanitizerList) {
            modifiableMap.put(sanitizer.getFileName(), sanitizer);
        }

        sanitizers = Collections.unmodifiableMap(modifiableMap);
    }

    public Resource getSanitizedResource(final File file) {
        ConfigFileSanitizer fileSanitizer = getSanitizer(file.getName());

        if (fileSanitizer != null) {
            try {
                return fileSanitizer.getSanitizedResource(file);
            } catch (FileSanitizationException e) {
                LOG.error("Could not sanitize file {}: {}", file, e.getCause().getMessage(), e);

                return new ByteArrayResource(e.getMessage().getBytes());
            }
        }

        return new FileSystemResource(file);
    }

    private ConfigFileSanitizer getSanitizer(String fileName) {
        ConfigFileSanitizer fileSanitizer = null;

        if (sanitizers.containsKey(fileName)) {
            fileSanitizer = sanitizers.get(fileName);
        } else if (fileName.contains(".")) {
            String fileExtension = fileName.substring(fileName.lastIndexOf("."));
            if (sanitizers.containsKey("*" + fileExtension)) {
                fileSanitizer = sanitizers.get("*" + fileExtension);
            }
        }

        return fileSanitizer;
    }
}
