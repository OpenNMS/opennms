package org.opennms.features.vaadin.nodemaps.gwt.client.openlayers;

public class NodeFeature extends GeoJSONFeature {
    protected NodeFeature() {}

    protected native final String getAttribute(final String key) /*-{
        return this[key];
    }-*/;

    protected native final void setAttribute(final String key, final String value) /*-{
        this[key] = value;
    }-*/;

    public final Integer getNodeId() {
        final String id = getAttribute("nodeId");
        return id == null? null : Integer.valueOf(id);
    }

    public final String getNodeLabel() {
        return getAttribute("nodeLabel");
    }

    public final String getForeignSource() {
        return getAttribute("foreignSource");
    }

    public final String getForeignId() {
        return getAttribute("foreignId");
    }
    
    public final String getIpAddress() {
        return getAttribute("ipAddress");
    }

    public final String getSeverityLabel() {
        return getAttribute("severityLabel");
    }

    public final Integer getSeverity() {
        final String severity = getAttribute("severity");
        return severity == null? null : Integer.valueOf(severity);
    }

    public final Integer getUnackedCount() {
        final String count = getAttribute("unackedCount");
        return count == null? 0 : Integer.valueOf(count);
    }
}
