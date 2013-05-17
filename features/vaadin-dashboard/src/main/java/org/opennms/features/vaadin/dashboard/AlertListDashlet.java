package org.opennms.features.vaadin.dashboard;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.dashboard.sandwichboard.Sandwich;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class AlertListDashlet extends VerticalLayout implements Sandwich {

    int tries = 0;

    public AlertListDashlet() {
        setMargin(true);
        setCaption("Nodes with pending problems");

        setWidth("100%");
        addNodes();
    }

    private void addNodes() {
        addComponent(new NodeLabel("mit.internal.opennms.com has 1 alarms (1 hour)", Severity.SEVERE));
        addComponent(new NodeLabel("duke.internal.opennms.com has 2 alarms (2 hours)", Severity.SEVERE));
        addComponent(new NodeLabel("cmu.internal.opennms.com has 1 alarms (2 hours)", Severity.WARNING));
        addComponent(new NodeLabel("ncstate.internal.opennms.com has 2 alarms (2 hours)", Severity.WARNING));
        addComponent(new NodeLabel("timmy.internal.opennms.com has 3 alarms (1 day)", Severity.SEVERE));
        addComponent(new NodeLabel("stanford.internal.opennms.com has 2 alarms (3 days)", Severity.SEVERE));
        addComponent(new NodeLabel("mit.internal.opennms.com has 1 alarms (1 hour)", Severity.SEVERE));
        addComponent(new NodeLabel("duke.internal.opennms.com has 2 alarms (2 hours)", Severity.SEVERE));
        addComponent(new NodeLabel("cmu.internal.opennms.com has 1 alarms (2 hours)", Severity.WARNING));
        addComponent(new NodeLabel("ncstate.internal.opennms.com has 2 alarms (2 hours)", Severity.WARNING));
        addComponent(new NodeLabel("timmy.internal.opennms.com has 3 alarms (1 day)", Severity.SEVERE));
        addComponent(new NodeLabel("stanford.internal.opennms.com has 2 alarms (3 days)", Severity.SEVERE));
        addComponent(new NodeLabel("mit.internal.opennms.com has 1 alarms (1 hour)", Severity.SEVERE));
        addComponent(new NodeLabel("duke.internal.opennms.com has 2 alarms (2 hours)", Severity.SEVERE));
        addComponent(new NodeLabel("cmu.internal.opennms.com has 1 alarms (2 hours)", Severity.WARNING));
        addComponent(new NodeLabel("ncstate.internal.opennms.com has 2 alarms (2 hours)", Severity.WARNING));
        addComponent(new NodeLabel("timmy.internal.opennms.com has 3 alarms (1 day)", Severity.SEVERE));
        addComponent(new NodeLabel("stanford.internal.opennms.com has 2 alarms (3 days)", Severity.SEVERE));
    }

    @Override
    public boolean allowAdvance() {
        if (tries++ % 3 == 0) {
            return true;
        } else {
            return false;
        }
    }

    public enum Severity {
        SEVERE, WARNING
    }

    private class NodeLabel extends Label {
        public NodeLabel(String content, Severity severity) {
            super(content);
            addStyleName("node-label");
            addStyleName(severity.name().toLowerCase());
        }
    }
}
