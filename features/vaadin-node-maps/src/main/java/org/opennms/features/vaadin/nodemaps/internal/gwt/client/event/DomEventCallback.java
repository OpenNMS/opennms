package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.Widget;

public interface DomEventCallback {
    public void onEvent(final NativeEvent event);

    public Widget getWidget();
    public Element getElement();
    public String[] getEventTypes();
}
