package org.opennms.features.vaadin.nodemaps.ui;

import org.opennms.features.vaadin.nodemaps.gwt.client.VOpenlayersWidget;

import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.VerticalLayout;

@ClientWidget(value=VOpenlayersWidget.class, loadStyle=LoadStyle.EAGER)
public class OpenlayersWidgetComponent extends VerticalLayout {
    private static final long serialVersionUID = 1L;

}
