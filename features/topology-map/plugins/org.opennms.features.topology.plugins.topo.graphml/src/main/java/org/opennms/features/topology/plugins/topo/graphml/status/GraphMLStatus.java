/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
