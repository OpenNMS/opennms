/**
 * 
 */
package org.opennms.dashboard.client;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Image;

public class ResourceGraph extends Image {
    public ResourceGraph() {
        super();
    }

    public void displayNoGraph() {
        setUrl("images/rrd/error.png");
    }
    
    public void setGraph(String resourceId, String reportName, String start, String end) {
        setUrl(buildGraphUrl(resourceId, reportName, start, end));
    }
    
    public void prefetchGraph(String resourceId, String reportName, String start, String end) {
        Image.prefetch(buildGraphUrl(resourceId, reportName, start, end));
    }

    private String buildGraphUrl(String resourceId, String report, String start, String end) {
        return "graph/graph.png?resourceId=" + URL.encodeComponent(resourceId) + "&report=" + URL.encodeComponent(report) + "&start=" + start + "&end=" + end;
    }
}