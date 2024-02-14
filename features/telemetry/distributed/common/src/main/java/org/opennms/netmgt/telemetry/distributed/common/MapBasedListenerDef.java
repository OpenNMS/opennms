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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.netmgt.telemetry.config.api.ListenerDefinition;
import org.opennms.netmgt.telemetry.config.api.ParserDefinition;

public class MapBasedListenerDef implements ListenerDefinition {
    private final String name;
    private final String className;
    private final Map<String, String> parameters;
    private final List<MapBasedParserDef> parsers;

    public MapBasedListenerDef(final PropertyTree definition) {
        this.name = definition.getRequiredString("name");
        this.className = definition.getRequiredString("class-name");

        this.parameters = definition.getMap("parameters");

        this.parsers = definition.getSubTrees("parsers").values().stream()
                .map(parserDefinition -> new MapBasedParserDef(this.name, parserDefinition))
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public Map<String, String> getParameterMap() {
        return parameters;
    }

    @Override
    public List<MapBasedParserDef> getParsers() {
        return this.parsers;
    }

}
