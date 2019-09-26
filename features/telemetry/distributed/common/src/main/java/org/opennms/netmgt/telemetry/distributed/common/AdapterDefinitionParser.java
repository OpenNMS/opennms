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
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;

import com.google.common.collect.Lists;

/**
 * Parses properties for multiple Adapter configurations.
 * Each adapter definition must start with <code>adapters.n.</code> key.
 * Where n is either a number (e.g. 1,2,3, etc.) or a character (e.g. a,b,c, etc.).
 *
 * The parser also supports the legacy configuration, where no <code>adapters.n.</code> prefix was needed.
 *
 * @author mvrueden
 */
public class AdapterDefinitionParser {

    /**
     * Groups properties for the same adapter together and wraps the properties behind a {@link AdapterDefinition}.
     * Each key which is starting with <code>adapters.n.</code> where 1 is any single character.
     *
     * @param propertyTree The properties to parse. May contain queue properties as well.
     * @return The Adapter definitions.
     */
    public List<AdapterDefinition> parse(PropertyTree propertyTree) {
        Objects.requireNonNull(propertyTree);

        // Legacy Mode
        if (propertyTree.getMap("adapters").isEmpty()) {
            return Lists.newArrayList(new MapBasedAdapterDef(propertyTree));
        }

        // New Mode, get all available adapters
        final Map<String, PropertyTree> adapterConfigurations = propertyTree.getSubTrees("adapters");

        // Each adapters key should be prefixed with a number, e.g. adapters.1., adapters.2., etc.
        final List<String> keys = adapterConfigurations.keySet()
                .stream()
                .sorted()
                .collect(Collectors.toList());
        final List<AdapterDefinition> adapters = new ArrayList<>();
        for (String eachAdapterPrefix : keys) {
            final PropertyTree adapterconfig = adapterConfigurations.get(eachAdapterPrefix);
            final MapBasedAdapterDef mapBasedAdapterDef = new MapBasedAdapterDef(adapterconfig);
            adapters.add(mapBasedAdapterDef);
        }

        return adapters;
    }

    protected List<AdapterDefinition> parse(Map<String, String> properties) {
        return parse(PropertyTree.from(properties));
    }
}
