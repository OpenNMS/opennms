package org.opennms.features.vaadin.nodemaps.gwt.client.openlayers;

import com.vaadin.terminal.gwt.client.UIDL;

public class NodeFeature extends GeoJSONFeature {
    protected NodeFeature() {}

    public static NodeFeature create(final UIDL node) {
        final float latitude = node.getFloatAttribute("latitude");
        final float longitude = node.getFloatAttribute("longitude");

        final NodeFeature me = GeoJSONFeature.create(latitude, longitude).cast();

        for (final String key : new String[] { "nodeLabel", "foreignSource", "foreignId", "ipAddress", "nodeId" }) {
            if (node.hasAttribute(key)) me.putProperty(key, node.getStringAttribute(key));
        }

        return me;
    }

    public final Integer getNodeId() {
        final String id = getProperty("nodeId");
        return id == null? null : Integer.valueOf(id);
    }

    public final String getNodeLabel() {
        return getProperty("nodeLabel");
    }

    public final String getForeignSource() {
        return getProperty("foreignSource");
    }

    public final String getForeignId() {
        return getProperty("foreignId");
    }
    
    public final String getIpAddress() {
        return getProperty("ipAddress");
    }
}
