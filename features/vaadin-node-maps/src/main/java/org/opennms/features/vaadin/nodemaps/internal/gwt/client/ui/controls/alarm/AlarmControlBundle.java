package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.alarm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

public interface AlarmControlBundle extends ClientBundle {
    public static final AlarmControlBundle INSTANCE = GWT.create(AlarmControlBundle.class);

    @Source("AlarmControl.css")
    AlarmControlCss css();
}