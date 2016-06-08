package org.opennms.features.topology.plugins.topo.graphml;

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
}
