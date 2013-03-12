package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.discotools.gwt.leaflet.client.popup.Popup;
import org.discotools.gwt.leaflet.client.popup.PopupImpl;
import org.discotools.gwt.leaflet.client.popup.PopupOptions;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.Map;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.MarkerCluster;

import com.google.gwt.core.client.JsArrayString;
import com.vaadin.terminal.gwt.client.VConsole;

public class NodeMarkerClusterCallback implements MarkerClusterEventCallback {
    private static final class NodeMarkerComparator implements Comparator<NodeMarker> {
        final static int BEFORE = -1;
        final static int EQUAL = 0;
        final static int AFTER = 1;

        @Override
        public int compare(final NodeMarker left, final NodeMarker right) {
            if (left == right) return EQUAL;
            if (left.getSeverity() != right.getSeverity()) {
                return left.getSeverity() > right.getSeverity()? BEFORE : AFTER;
            }
            if (left.getNodeLabel() != right.getNodeLabel()) {
                if (left.getNodeLabel() == null) return AFTER;
                if (right.getNodeLabel() == null) return BEFORE;
                return left.getNodeLabel().toLowerCase().compareTo(right.getNodeLabel().toLowerCase());
            }
            if (left.getNodeId() != right.getNodeId()) {
                if (left.getNodeId() == null) return AFTER;
                if (right.getNodeId() == null) return BEFORE;
                return left.getNodeId().compareTo(right.getNodeId());
            }
            return EQUAL;
        }
    }

    public NodeMarkerClusterCallback() {
    }

    @Override
    public final void run(final MarkerClusterEvent event) {
        final StringBuilder sb = new StringBuilder();
        final MarkerCluster cluster = event.getMarkerCluster();
        final List<NodeMarker> markers = (List<NodeMarker>)cluster.getAllChildMarkers();
        VConsole.log("Clicked, processing " + markers.size() + " markers.");
        Collections.sort(markers, new NodeMarkerComparator());

        if (markers.size() == 1) {
            final NodeMarker marker = markers.get(0);
            sb.append(getPopupTextForMarker(marker));
        } else {
            final StringBuilder nodeBuilder = new StringBuilder();
            int unacked = 0;
            for (final NodeMarker marker : markers) {
                unacked += marker.getUnackedCount();
                nodeBuilder.append("<li>");
                nodeBuilder.append("<a class=\"node\" href=\"/opennms/element/node.jsp?node=").append(marker.getNodeId()).append("\" target=\"_blank\">").append(marker.getNodeLabel()).append("</a> ");
                nodeBuilder.append("(").append(marker.getIpAddress()).append(")").append(": ");
                nodeBuilder.append("<a href=\"/opennms/alarm/list.htm?sortby=id&acktype=unack&limit=20&filter=node%3D").append(marker.getNodeId()).append("\" target=\"_blank\">").append(marker.getSeverityLabel()).append("</a>");
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
        
        /*
        final Element element = popup.getJSObject().cast();
        DomEvent.addListener(new DomEventCallback("keydown", null) {
            @Override protected void onEvent(final NativeEvent event) {
                VConsole.log("marker cluster popup keydown event");
            }
        }, element);
        */
        final Map map = new Map(cluster.getGroup().getMapObject());
        VConsole.log("current zoom: " + map.getZoom() + ", max zoom: " + map.getMaxZoom());

        if (map.getZoom() == map.getMaxZoom()) {
            VConsole.log("at max zoom, skipping popup");
        } else {
            PopupImpl.openOn(popup.getJSObject(), cluster.getGroup().getMapObject());
        }
    }

    public static String getPopupTextForMarker(final NodeMarker marker) {
        // TODO: THIS IS AWFUL
        final StringBuilder sb = new StringBuilder();
        sb.append("<h2>Node <a class=\"node\" href=\"/opennms/element/node.jsp?node=").append(marker.getNodeId()).append("\" target=\"_blank\">").append(marker.getNodeLabel()).append("</a></h2>");
        sb.append("<p>");
        sb.append("Description: ").append(marker.getDescription()).append("<br/>");
        sb.append("Maint.&nbsp;Contract: ").append(marker.getMaintContract()).append("<br/>");
        sb.append("IP Address: ").append(marker.getIpAddress()).append("<br/>");
        sb.append("Severity: ").append("<a href=\"/opennms/alarm/list.htm?sortby=id&acktype=unack&limit=20&filter=node%3D").append(marker.getNodeId()).append("\" target=\"_blank\">").append(marker.getSeverityLabel()).append("</a>");
        final JsArrayString categories = marker.getCategories();
        if (categories.length() > 0) {
            sb.append("<br/>");
            if (categories.length() == 1) {
                sb.append("Category: ");
            } else {
                sb.append("Categories: ");
            }
            for (int i = 0; i < categories.length(); i++) {
                sb.append(categories.get(i));
                if (i != (categories.length() - 1)) {
                    sb.append(", ");
                }
            }
        }
        sb.append("</p>");
        return sb.toString();
    }
}
