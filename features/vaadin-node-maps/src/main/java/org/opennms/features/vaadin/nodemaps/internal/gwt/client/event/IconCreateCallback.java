package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import java.util.List;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.jsobject.JSObjectWrapper;
import org.discotools.gwt.leaflet.client.types.DivIcon;
import org.discotools.gwt.leaflet.client.types.DivIconOptions;
import org.discotools.gwt.leaflet.client.types.Point;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.MarkerCluster;

public class IconCreateCallback extends JSObjectWrapper {

    protected IconCreateCallback(final JSObject jsObject) {
        super(jsObject);
    }

    public IconCreateCallback() {
        super(JSObject.createJSFunction());
        setJSObject(getCallbackFunction());
    }

    public JSObject createIcon(final MarkerCluster cluster) {
        final DivIconOptions options = new DivIconOptions();
        options.setHtml("<div><span>" + cluster.getChildCount() + "</span></div>");
        options.setIconSize(new Point(40, 40));

        int severity = 0;
        String severityLabel = "Normal";
        for (final NodeMarker marker : (List<NodeMarker>)cluster.getAllChildMarkers()) {
            final int nodeSeverity = marker.getSeverity();
            if (nodeSeverity > severity) {
                severity = nodeSeverity;
                severityLabel = marker.getSeverityLabel();
            }
            if (severity == 7) break;
        }

        options.setClassName("marker-cluster marker-cluster-" + severityLabel);

        return new DivIcon(options).getJSObject();
    }

    public native final JSObject getCallbackFunction() /*-{
        var self = this;
        return function(cluster) {
            return self.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.IconCreateCallback::createIcon(Lorg/opennms/features/vaadin/nodemaps/internal/gwt/client/ui/MarkerCluster;)(cluster);
        };
    }-*/;
}
