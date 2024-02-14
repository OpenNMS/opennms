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
     * @param queueName Name of the queue the adapter is attached to.
     * @param propertyTree The properties to parse. May contain queue properties as well.
     * @return The Adapter definitions.
     */
    public List<AdapterDefinition> parse(final String queueName, final PropertyTree propertyTree) {
        Objects.requireNonNull(propertyTree);

        // Legacy Mode
        if (propertyTree.getMap("adapters").isEmpty()) {
            return Lists.newArrayList(new MapBasedAdapterDef(queueName, propertyTree));
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
            final MapBasedAdapterDef mapBasedAdapterDef = new MapBasedAdapterDef(queueName, adapterconfig);
            adapters.add(mapBasedAdapterDef);
        }

        return adapters;
    }
}
