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

import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.telemetry.config.api.ParserDefinition;

public class MapBasedParserDef extends MapBasedQueueDef implements ParserDefinition {
    private final String listenerName;
    private final String className;
    private final Map<String, String> parameters;

    public MapBasedParserDef(final String listenerName, final PropertyTree definition) {
        super(definition);
        this.listenerName = Objects.requireNonNull(listenerName);
        this.className = definition.getRequiredString("class-name");
        this.parameters = definition.getMap("parameters");
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getQueueName() {
        // We use the name of the parser on Minion side to reference the queue
        return getName();
    }

    @Override
    public String getFullName() {
        return String.format("%s.%s", this.listenerName, this.getName());
    }

    @Override
    public Map<String, String> getParameterMap() {
        return parameters;
    }
}
