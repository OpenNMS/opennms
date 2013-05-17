package org.opennms.features.vaadin.dashboard;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
@Theme("opennms")
@Title("OpenNMS Dashboard")
public class DashboardUI extends UI {

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        rootLayout.setSpacing(true);

        rootLayout.addComponent(new HeaderLayout());

        VerticalLayout portalWrapper = new VerticalLayout();
        portalWrapper.setSizeFull();
        portalWrapper.setMargin(true);

        rootLayout.addComponent(portalWrapper);
        rootLayout.setExpandRatio(portalWrapper, 1);
        setContent(rootLayout);


        Navigator navigator = new Navigator(this, portalWrapper);
        navigator.addView("dashboard", DashboardView.class);
        navigator.addView("sandwichboard", SandwichBoardView.class);

        navigator.navigateTo("sandwichboard");
    }

}
