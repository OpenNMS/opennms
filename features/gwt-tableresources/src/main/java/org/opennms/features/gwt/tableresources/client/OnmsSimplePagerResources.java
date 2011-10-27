package org.opennms.features.gwt.tableresources.client;

import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.Style;

public interface OnmsSimplePagerResources extends SimplePager.Resources {
    @Source({"OnmsSimplePager.css"})
    Style simplePagerStyle();
}
