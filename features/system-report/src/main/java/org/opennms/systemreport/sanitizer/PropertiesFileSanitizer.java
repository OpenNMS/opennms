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

package org.opennms.systemreport.sanitizer;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class PropertiesFileSanitizer implements ConfigFileSanitizer {

    private static final Set<String> PROPERTIES_TO_SANITIZE = new LinkedHashSet<>(Arrays.asList("password", "pass", "trustStorePassword"));

    private final String SANITIZED_VALUE = "***";

    @Override
    public String getFileType() {
        return "properties";
    }

    public Resource getSanitizedResource(final File file) throws FileSanitizationException {
        try {
            return sanitizeProperties(file);
        } catch (Exception e) {
            throw new FileSanitizationException("Could not sanitize file", e);
        }
    }

    private Resource sanitizeProperties(final File file) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));

        properties.stringPropertyNames().forEach(propertyName -> {
            String lastPart = propertyName.substring(propertyName.lastIndexOf(".") + 1);
            if (PROPERTIES_TO_SANITIZE.contains(lastPart)) {
                properties.setProperty(propertyName, SANITIZED_VALUE);
            }
        });

        String result = properties.entrySet().stream()
                .map((entry) -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n"));

        return new ByteArrayResource(result.getBytes());
    }


}
