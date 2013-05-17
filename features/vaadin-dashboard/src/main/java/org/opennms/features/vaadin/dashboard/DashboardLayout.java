package org.opennms.features.vaadin.dashboard;

import org.vaadin.addon.portallayout.container.PortalColumns;
import org.vaadin.addon.portallayout.portal.StackPortalLayout;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class DashboardLayout extends PortalColumns {


    private StackPortalLayout column1;
    private StackPortalLayout column2;
    private StackPortalLayout column3;

    public DashboardLayout() {
        setSizeFull();

        createLayouts();

        column1.portletFor(new DemoDashlet("OpenNMS Dashboard playground"));
        column2.portletFor(new MapDashlet());
        column3.portletFor(new AlertListDashlet());
    }

    private void createLayouts() {
        column1 = new StackPortalLayout();
        column2 = new StackPortalLayout();
        column3 = new StackPortalLayout();

        column1.setSizeFull();
        column2.setSizeFull();
        column3.setSizeFull();

        column1.setSpacing(true);
        column2.setSpacing(true);
        column3.setSpacing(true);

        column1.setMargin(true);
        column2.setMargin(true);
        column3.setMargin(true);

        appendPortal(column1);
        appendPortal(column2);
        appendPortal(column3);
    }
}
