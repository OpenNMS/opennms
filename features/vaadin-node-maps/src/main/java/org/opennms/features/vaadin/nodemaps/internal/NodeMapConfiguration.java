/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.Option;

import com.google.common.base.Strings;

/**
 * Configuration object introduced to address NMS-8597 and reduce possible merge conflicts.
 */
public class NodeMapConfiguration {

    private static final String URL_KEY = "gwt.openlayers.url";

    private static final String OPTIONS_KEY_PREFIX = "gwt.openlayers.options";

    public static String getTileServerUrl() {
        final String url = System.getProperty(URL_KEY);
        return sanitizeForVaadin(url);
    }

    /**
     * Returns the layer options for the tile layer defined in opennms.properties.
     * See http://leafletjs.com/reference.html#tilelayer-options for more details.
     *
     * @return the layer options for the tile layer defined in opennms.properties.
     */
    public static List<Option> getTileLayerOptions() {
        final Properties properties = System.getProperties();
        final Map<String, String> options = new HashMap<>();
        for (Object objectKey : properties.keySet()) {
            String key = (String) objectKey;
            if (key.startsWith(OPTIONS_KEY_PREFIX)) {
                final String optionsKey = key.substring(OPTIONS_KEY_PREFIX.length() + 1);
                final String optionsValue = properties.getProperty(key);
                options.put(optionsKey, sanitizeForVaadin(optionsValue));
            }
        }
        final List<Option> optionsList = new ArrayList<>();
        for (Map.Entry<String, String> eachEntry : options.entrySet()) {
            optionsList.add(new Option(eachEntry.getKey(), eachEntry.getValue()));
        }
        return optionsList;
    }

    private static String sanitizeForVaadin(String input) {
        if (Strings.isNullOrEmpty(input)) {
            return input;
        }
        // The input may contain ${variable} statements, which must be converted to {variable} in order to work
        // with the vaadin gwt abstraction.
        return input.replaceAll("\\$\\{", "{");
    }

    public static boolean isValid() {
        return !Strings.isNullOrEmpty(getTileServerUrl()) && !Strings.isNullOrEmpty(getTileLayerAttribution());

    }

    /**
     * Returns the 'attribution' tile layer option.
     * See http://leafletjs.com/reference.html#tilelayer-options for more details.
     *
     * @return the 'attribution' tile layer option.
     */
    static String getTileLayerAttribution() {
        return System.getProperty(OPTIONS_KEY_PREFIX + ".attribution");
    }
}
