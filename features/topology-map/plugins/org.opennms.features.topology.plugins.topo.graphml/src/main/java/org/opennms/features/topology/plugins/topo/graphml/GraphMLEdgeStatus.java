package org.opennms.features.topology.plugins.topo.graphml;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.netmgt.model.OnmsSeverity;

import java.util.HashMap;
import java.util.Map;

public class GraphMLEdgeStatus implements Status {

    private OnmsSeverity severity;
    private Map<String, String> styleProperties;

    public GraphMLEdgeStatus(final OnmsSeverity severity,
                             final Map<String, String> styleProperties) {
        this.severity = severity;
        this.styleProperties = styleProperties;
    }

    public GraphMLEdgeStatus() {
        this.severity = OnmsSeverity.INDETERMINATE;
        this.styleProperties = new HashMap<>();
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
        this.styleProperties.putAll(style);
        return this;
    }

    public GraphMLEdgeStatus style(final String key, final String value) {
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
}
