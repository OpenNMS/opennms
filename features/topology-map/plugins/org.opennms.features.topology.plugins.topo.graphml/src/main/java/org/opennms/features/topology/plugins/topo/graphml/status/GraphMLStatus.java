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
package org.opennms.features.topology.plugins.topo.graphml.status;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.netmgt.model.OnmsSeverity;

import java.util.Map;
import java.util.Set;

public abstract class GraphMLStatus implements Status {
    private OnmsSeverity severity;
    private Map<String, String> styleProperties;

    public GraphMLStatus(final OnmsSeverity severity,
                         final Map<String, String> styleProperties) {
        this.severity = severity;
        this.styleProperties = this.checkStylePropertyNames(styleProperties);
    }

    public GraphMLStatus(final OnmsSeverity severity) {
        this.severity = severity;
        this.styleProperties = Maps.newHashMap(Maps.asMap(this.getAllowedStyleProperties(), key -> null));
    }

    protected abstract Set<String> getAllowedStyleProperties();

    public final OnmsSeverity getSeverity() {
        return this.severity;
    }

    public final String computeStatus() {
        return this.severity.getLabel().toLowerCase();
    }

    public Map<String, String> getStatusProperties() {
        return ImmutableMap.of("status", this.computeStatus());
    }

    public final Map<String, String> getStyleProperties() {
        return this.styleProperties;
    }

    public final GraphMLStatus severity(final OnmsSeverity severity) {
        this.severity = severity;
        return this;
    }

    public final GraphMLStatus style(final Map<String, String> style) {
        this.styleProperties.putAll(this.checkStylePropertyNames(style));
        return this;
    }

    public final GraphMLStatus style(final String key, final String value) {
        this.styleProperties.put(this.checkStylePropertyName(key), value);
        return this;
    }

    protected final Map<String, String> checkStylePropertyNames(final Map<String, String> styleProperties) {
        for (final String key : styleProperties.keySet()) {
            this.checkStylePropertyName(key);
        }
        return styleProperties;
    }

    private String checkStylePropertyName(final String key) {
        Preconditions.checkArgument(this.getAllowedStyleProperties().contains(key),
                                    "Illegal property name: %s - allowed properties are: %s", key,
                                    Joiner.on(", ").join(this.getAllowedStyleProperties()));
        return key;
    }

    protected static OnmsSeverity mergeSeverity(final GraphMLStatus s1,
                                                final GraphMLStatus s2) {
        return s1.getSeverity().isGreaterThan(s2.getSeverity())
               ? s1.getSeverity()
               : s2.getSeverity();
    }

    protected static Map<String, String> mergeStyleProperties(final GraphMLStatus s1,
                                                              final GraphMLStatus s2) {
        final Map<String, String> styleProperties = Maps.newHashMap();

        for (final Map.Entry<String, String> e : s1.getStyleProperties().entrySet()) {
            if (e.getValue() != null) {
                styleProperties.put(e.getKey(), e.getValue());
            }
        }
        for (final Map.Entry<String, String> e : s2.getStyleProperties().entrySet()) {
            if (e.getValue() != null) {
                styleProperties.put(e.getKey(), e.getValue());
            }
        }
        for (final String key : s1.getAllowedStyleProperties()) {
            styleProperties.putIfAbsent(key, null);
        }
        for (final String key : s2.getAllowedStyleProperties()) {
            styleProperties.putIfAbsent(key, null);
        }

        return styleProperties;
    }
}
