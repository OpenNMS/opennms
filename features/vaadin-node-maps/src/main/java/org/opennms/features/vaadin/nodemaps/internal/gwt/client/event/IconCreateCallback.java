package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.jsobject.JSObjectWrapper;
import org.discotools.gwt.leaflet.client.types.DivIcon;
import org.discotools.gwt.leaflet.client.types.DivIconOptions;
import org.discotools.gwt.leaflet.client.types.Point;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.MarkerCluster;

public class IconCreateCallback extends JSObjectWrapper {
	
    private Logger logger = Logger.getLogger(getClass().getName());


    protected IconCreateCallback(final JSObject jsObject) {
        super(jsObject);
    }

    public IconCreateCallback() {
        super(JSObject.createJSFunction());
        setJSObject(getCallbackFunction());
    }

//    public JSObject createIcon(final MarkerCluster cluster) {
//        final DivIconOptions options = new DivIconOptions();
//        options.setHtml("<div><span>" + cluster.getChildCount() + "</span></div>");
//        options.setIconSize(new Point(40, 40));
//
//        int severity = 0;
//        String severityLabel = "Normal";
//        for (final NodeMarker marker : (List<NodeMarker>)cluster.getAllChildMarkers()) {
//            final int nodeSeverity = marker.getSeverity();
//            if (nodeSeverity > severity) {
//                severity = nodeSeverity;
//                severityLabel = marker.getSeverityLabel();
//            }
//            if (severity == 7) break;
//        }
//
//        options.setClassName("marker-cluster marker-cluster-" + severityLabel);
//
//        return new DivIcon(options).getJSObject();
//    }
    
    public JSObject createIcon(final MarkerCluster cluster){
    	final DivIconOptions options = new DivIconOptions();
    	options.setIconSize(new Point(40, 40));
    	
    	int severity = 0;
    	int total = 0;
    	double[] dataArray = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    	//class array corresponds to data array
    	String[] classArray = {"Indeterminate", "Cleared", "Normal", "Warning", "Minor", "Major", "Critical"};
    	
    	String severityLabel = "Normal";
    	for(final NodeMarker marker : (List<NodeMarker>)cluster.getAllChildMarkers()) {
    		final int nodeSeverity = marker.getSeverity();
    		total++;
    		dataArray[nodeSeverity -1] += 1.0;
    		if (nodeSeverity > severity) {
    			severity = nodeSeverity;
    			severityLabel = marker.getSeverityLabel();
    		}
    	}
    	
    	String svg = getChartSvg(20.0, 20.0, 18.0, 12.0, dataArray, classArray, (double) total);
    	
    	options.setHtml(svg + "<div><span>" + cluster.getChildCount() + "</span></div>");
    	options.setClassName("marker-cluster marker-cluster-" + severityLabel);
    	
    	return new DivIcon(options).getJSObject();
    	
    }
	
	//this function returns the svg for a donut chart with the given parameters
	private String getChartSvg(double cx, double cy, double r, double innerR,
			double[] dataArray, String[] classArray, double total) {
		String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">";

		double startangle = 0;
		for (int i = 0; i < dataArray.length; i++) {

			if (dataArray[i] > 0) {

				double endangle = startangle + (((dataArray[i]) / total) * Math.PI
						* 2.0);

				String path = "<path d=\"";

				double x1 = cx + (r * Math.sin(startangle));
				double y1 = cy - (r * Math.cos(startangle));
				double X1 = cx + (innerR * Math.sin(startangle));
				double Y1 = cy - (innerR * Math.cos(startangle));

				double x2 = cx + (r * Math.sin(endangle));
				double y2 = cy - (r * Math.cos(endangle));
				double X2 = cx + (innerR * Math.sin(endangle));
				double Y2 = cy - (innerR * Math.cos(endangle));

				int big = 0;
				if (endangle - startangle > Math.PI)
					big = 1;

				String d;
				// this branch is if one data value comprises 100% of the data
				if (dataArray[i] >= total) {

					d = "M " + X1 + "," + Y1 + " A " + innerR + "," + innerR
							+ " 0 " + "1" + " 0 " + X1 + ","
							+ (Y1 + (2 * innerR)) + " A " + innerR + ","
							+ innerR + " 0 " + big + " 0 " + X1 + "," + Y1
							+ " M " + x1 + "," + y1 + " A " + r + "," + r
							+ " 0 " + big + " 1 " + x1 + "," + (y1 + (2 * r))
							+ " A " + r + "," + r + " 0 " + big + " 1 " + x1
							+ "," + y1;

				} else {
					// path string
					d = "M " + X1 + "," + Y1 + " A " + innerR + "," + innerR
							+ " 0 " + big + " 1 " + X2 + "," + Y2 + " L " + x2
							+ "," + y2 + " A " + r + "," + r + " 0 " + big
							+ " 0 " + x1 + "," + y1 + " Z";
				}
				path = path.concat(d + "\"" + " class =");
	            
	            path = path.concat(classArray[i]);
//	            path = path.concat(" stroke= \"black\"");
	            path = path.concat(" stroke-width= \"0\"/>");
	            
	            svg = svg.concat(path);
	            startangle = endangle;

			}
			
			
		}
				
		svg = svg.concat("</svg>");
	    
	    return svg;

	}

    public native final JSObject getCallbackFunction() /*-{
        var self = this;
        return function(cluster) {
            return self.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.IconCreateCallback::createIcon(Lorg/opennms/features/vaadin/nodemaps/internal/gwt/client/ui/MarkerCluster;)(cluster);
        };
    }-*/;
}
