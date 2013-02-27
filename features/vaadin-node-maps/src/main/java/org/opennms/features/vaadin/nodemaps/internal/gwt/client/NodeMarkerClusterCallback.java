package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.List;

import org.discotools.gwt.leaflet.client.popup.Popup;
import org.discotools.gwt.leaflet.client.popup.PopupImpl;
import org.discotools.gwt.leaflet.client.popup.PopupOptions;

import com.vaadin.terminal.gwt.client.VConsole;

final class NodeMarkerClusterCallback implements MarkerClusterEventCallback {
    NodeMarkerClusterCallback() {
    }

    @Override
    public final void run(final MarkerClusterEvent event) {
        final StringBuilder sb = new StringBuilder();
        final MarkerCluster cluster = event.getMarkerCluster();
        @SuppressWarnings("unchecked")
        final List<NodeMarker> markers = (List<NodeMarker>)cluster.getAllChildMarkers();
        VConsole.log("Clicked, processing " + markers.size() + " markers.");
        if (markers.size() == 1) {
            final NodeMarker marker = markers.get(0);
            sb.append(getPopupTextForMarker(marker));
        } else {
            final StringBuilder nodeBuilder = new StringBuilder();
            int unacked = 0;
            for (final NodeMarker marker : markers) {
                unacked += marker.getUnackedCount();
                nodeBuilder.append("<li>");
                nodeBuilder.append(marker.getNodeLabel()).append(" ");
                nodeBuilder.append("(").append(marker.getIpAddress()).append(")").append(": ");
                nodeBuilder.append(marker.getSeverityLabel());
                nodeBuilder.append("</li>");
            }
            sb.append("<h2># of nodes: ").append(markers.size()).append(" ");
            sb.append("(").append(unacked).append(" Unacknowledged Alarms)");
            sb.append("</h2>");
            sb.append("<ul>").append(nodeBuilder).append("</ul>");
        }
        final PopupOptions options = new PopupOptions();
        options.setMaxWidth(500);
        options.setProperty("maxHeight", 250);
        final Popup popup = new Popup(options);
        popup.setContent(sb.toString());
        popup.setLatLng(cluster.getLatLng());
        VConsole.log("html = " + sb.toString());
        PopupImpl.openOn(popup.getJSObject(), cluster.getGroup().getMapObject());
    }

    public static String getPopupTextForMarker(final NodeMarker marker) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<h2>Node ").append(marker.getNodeLabel()).append("</h2>");
        sb.append("<p>");
        sb.append("Node ID: ").append(marker.getNodeId()).append("<br/>");
        sb.append("Foreign Source: ").append(marker.getForeignSource()).append("<br/>");
        sb.append("Foreign ID: ").append(marker.getForeignId()).append("<br/>");
        sb.append("IP Address: ").append(marker.getIpAddress()).append("<br/>");
        sb.append("Severity: ").append(marker.getSeverityLabel());
        sb.append("</p>");
        return sb.toString();
    }
}