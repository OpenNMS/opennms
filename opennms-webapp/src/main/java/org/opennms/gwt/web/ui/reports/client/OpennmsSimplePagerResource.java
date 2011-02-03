package org.opennms.gwt.web.ui.reports.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.SimplePager.Resources;
import com.google.gwt.user.cellview.client.SimplePager.Style;

public interface OpennmsSimplePagerResource extends Resources {
    
    public OpennmsSimplePagerResource INSTANCE = GWT.create(OpennmsSimplePagerResource.class);
    
    public interface SimplePagerStyle extends Style{};
    
    @Source({"OpennmsSimplePager.css"})
    public SimplePagerStyle simplePagerStyle();
    
}
