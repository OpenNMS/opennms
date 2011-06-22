package org.opennms.features.node.list.gwt.client;

import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.Style;

public interface OnmsSimplePagerResources extends SimplePager.Resources {
    @Source({"com/google/gwt/user/cellview/client/SimplePager.css","OnmsSimplePager.css"})
    Style simplePagerStyle();
}
