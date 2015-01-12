package org.opennms.features.vaadin.surveillanceviews.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("dashboard")
@Title("OpenNMS Surveillance Views")
public class SurveillanceViewsUI extends UI {

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        rootLayout.setSpacing(true);
        rootLayout.addComponent(new Label("Hello SurveillanceViewsUI"));
        setContent(rootLayout);
    }
}
