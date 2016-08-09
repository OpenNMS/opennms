package org.opennms.features.topology.plugins.topo.graphml;

import com.google.common.base.Objects;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.netmgt.model.OnmsSeverity;
import java.util.Map;
import java.util.Set;

public class GraphMLEdgeStatus implements Status {

    public static final Set<String> ALLOWED_PROPERTIES = ImmutableSet.of("stroke",
                                                                         "stroke-width",
                                                                         "stroke-dasharray");

    private static final Map<String, String> EMPTY_PROPERTIES = Maps.asMap(ALLOWED_PROPERTIES, key -> null);

    private OnmsSeverity severity;
    private Map<String, String> styleProperties;

    private GraphMLEdgeStatus(final OnmsSeverity severity,
                              final Map<String, String> styleProperties) {
        checkPropertyNames(styleProperties);

        this.severity = severity;
        this.styleProperties = styleProperties;
    }

    public GraphMLEdgeStatus() {
        this(OnmsSeverity.INDETERMINATE,
             Maps.newHashMap(EMPTY_PROPERTIES));
    }

    public OnmsSeverity getSeverity() {
        return this.severity;
    }

    @Override
    public String computeStatus() {
        return this.severity.getLabel().toLowerCase();
    }

    @Override
    public Map<String, String> getStatusProperties() {
        return ImmutableMap.of("status", this.computeStatus());
    }

    @Override
    public Map<String, String> getStyleProperties() {
        return this.styleProperties;
    }

    public GraphMLEdgeStatus severity(final OnmsSeverity severity) {
        this.severity = severity;
        return this;
    }

    public GraphMLEdgeStatus style(final Map<String, String> style) {
        checkPropertyNames(style);
        this.styleProperties.putAll(style);
        return this;
    }

    public GraphMLEdgeStatus style(final String key, final String value) {
        checkPropertyName(key);
        this.styleProperties.put(key, value);
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        final GraphMLEdgeStatus that = (GraphMLEdgeStatus) o;
        return Objects.equal(this.severity, that.severity) &&
               Objects.equal(this.styleProperties, that.styleProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.severity,
                                this.styleProperties);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                      .add("severity", severity)
                      .add("styleProperties", styleProperties)
                      .toString();
    }

    private static void checkPropertyNames(final Map<String, String> styleProperties) {
        for (final String key : styleProperties.keySet()) {
            checkPropertyName(key);
        }
    }

    private static void checkPropertyName(final String key) {
        Preconditions.checkArgument(ALLOWED_PROPERTIES.contains(key),
                                    "Illegal property name: %s - allowed properties are: %s", key,
                                    Joiner.on(", ").join(ALLOWED_PROPERTIES));
    }

    public static GraphMLEdgeStatus merge(final GraphMLEdgeStatus s1,
                                          final GraphMLEdgeStatus s2) {
        final OnmsSeverity severity = s1.getSeverity().isGreaterThan(s2.getSeverity())
                                          ? s1.getSeverity()
                                          : s2.getSeverity();

        final Map<String, String> styleProperties = Maps.newHashMap(EMPTY_PROPERTIES);
        for (final Map.Entry<String, String> e : s1.styleProperties.entrySet()) {
            if (e.getValue() != null) {
                styleProperties.put(e.getKey(), e.getValue());
            }
        }
        for (final Map.Entry<String, String> e : s2.styleProperties.entrySet()) {
            if (e.getValue() != null) {
                styleProperties.put(e.getKey(), e.getValue());
            }
        }

        return new GraphMLEdgeStatus(severity, styleProperties);
    }
}
