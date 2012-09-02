package org.opennms.features.topology.netutils.internal.operations;

import java.net.URL;
import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.netutils.internal.EventsAlarmsWindow;
import org.opennms.features.topology.netutils.internal.Node;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

public class EventsAlarmsOperation implements Operation {

    private String m_eventsURL;

    private String m_alarmsURL;

    public boolean display(final List<Object> targets, final OperationContext operationContext) {
        return true;
    }

    public boolean enabled(final List<Object> targets, final OperationContext operationContext) {
        if (targets == null || targets.size() < 2)
            return true;
        return false;
    }

    public Undoer execute(final List<Object> targets, final OperationContext operationContext) {
        String label = "";
        int nodeID = -1;

        try {
            if (targets != null) {
                for (final Object target : targets) {
                    final Item vertexItem = operationContext.getGraphContainer().getVertexItem(target);
                    if (vertexItem != null) {
                        final Property labelProperty = vertexItem.getItemProperty("label");
                        label = labelProperty == null ? "" : (String) labelProperty.getValue();
                        final Property nodeIDProperty = vertexItem.getItemProperty("nodeID");
                        nodeID = nodeIDProperty == null ? -1 : (Integer) nodeIDProperty.getValue();
                    }
                }
            }
            final Node node = new Node(nodeID, null, label);

            final URL baseURL = operationContext.getMainWindow().getURL();

            final URL eventsURL;
            final URL alarmsURL;
            if (node.getNodeID() >= 0) {
                eventsURL = new URL(baseURL, getEventsURL() + "?filter=node%3D" + node.getNodeID());
                alarmsURL = new URL(baseURL, getAlarmsURL() + "?sortby=id&amp;acktype=unacklimit=20&amp;filter=node%3D" + node.getNodeID());
            } else {
                eventsURL = new URL(baseURL, getEventsURL());
                alarmsURL = new URL(baseURL, getAlarmsURL());
            }

            operationContext.getMainWindow().addWindow(new EventsAlarmsWindow(node, eventsURL, alarmsURL));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getId() {
        return "EventsAlarms";
    }

    public String getEventsURL() {
        return m_eventsURL;
    }

    public void setEventsURL(final String eventsURL) {
        this.m_eventsURL = eventsURL;
    }

    public String getAlarmsURL() {
        return m_alarmsURL;
    }

    public void setAlarmsURL(final String alarmsURL) {
        this.m_alarmsURL = alarmsURL;
    }

}
