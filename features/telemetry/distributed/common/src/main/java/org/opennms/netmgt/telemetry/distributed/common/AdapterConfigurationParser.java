/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.distributed.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.telemetry.config.api.Adapter;

import com.google.common.collect.Lists;

/**
 * Parses properties for multiple Adapter configurations.
 * Each adapter configuration must start with <code>adapters.n.</code> key.
 * Where n is either a number (e.g. 1,2,3, etc.) or a character (e.g. a,b,c, etc.).
 *
 * The parser also supports the legacy configuration, where no <code>adapters.n.</code> prefix was needed.
 *
 * @author mvrueden
 */
public class AdapterConfigurationParser {

    /**
     * Groups properties for the same adapter together and wraps the properties behind a {@link MapBasedAdapterDef}.
     * Each key which is starting with <code>adapters.n.</code> where 1 is any single character,
     * @param properties The properties to parse. May contain protocol properties as well.
     * @return The Adapter definitions.
     */
    public List<Adapter> parse(Map<String, String> properties) {
        // Legacy Mode
        if (!hasAdaptersKey(properties)) {
            return Lists.newArrayList(new MapBasedAdapterDef(properties));
        }

        // New Mode, get all available adapters
        final Map<String, String> adapterConfigurations = MapUtils.filterKeysByPrefix(properties, "adapters.");

        // Each adapters key should be prefixed with a number, e.g. adapters.1., adapters.2., etc.
        final Set<String> numbers = adapterConfigurations.keySet()
                .stream()
                .map(key -> key.split("\\.")[0]) // We are only interested in the number indicator, e.g. 1, 2, etc.
                .sorted()
                .collect(Collectors.toSet());
        final List<Adapter> adapters = new ArrayList<>();
        for (String eachAdapterPrefix : numbers) {
            final Map<String, String> stringStringMap = MapUtils.filterKeysByPrefix(adapterConfigurations, eachAdapterPrefix + ".");
            final MapBasedAdapterDef mapBasedAdapterDef = new MapBasedAdapterDef(stringStringMap);
            adapters.add(mapBasedAdapterDef);
        }

        return adapters;
    }

    // Returns true if any key in the properties starts with "adapters."
    private boolean hasAdaptersKey(Map<String, String> properties) {
        return properties.keySet()
                .stream()
                .filter(key -> key.startsWith("adapters."))
                .findAny().isPresent();
    }
}
