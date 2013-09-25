package org.opennms.features.vaadin.nodemaps.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;

import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

public class NodeMapQueryLink implements Operation {
    private static final Logger LOG = Logger.getLogger(NodeMapQueryLink.class.getName());

    @Override
    public String getId() {
        return "NodeMapQueryLink";
    }

    @Override
    public Undoer execute(final List<VertexRef> targets, final OperationContext operationContext) {
        final StringBuilder sb = new StringBuilder();
        sb.append(VaadinServlet.getCurrent().getServletContext().getContextPath());
        sb.append("/node-maps#search/nodeId%20in%20");

        final List<String> nodeIds = new ArrayList<String>();
        for (final VertexRef ref : targets) {
            if ("nodes".equals(ref.getNamespace())) {
                nodeIds.add(ref.getId());
            }
        }

        final Iterator<String> i = nodeIds.iterator();
        while (i.hasNext()) {
            sb.append(i.next());
            if (i.hasNext()) {
                sb.append(",");
            }
        }

        final String redirectUrl = sb.toString();
        LOG.info("redirecting to: " + redirectUrl);
        final UI ui = operationContext.getMainWindow();
        ui.getPage().getJavaScript().execute("window.location = '" + redirectUrl + "';");

        return null;
    }

    @Override
    public boolean display(final List<VertexRef> targets, final OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(final List<VertexRef> targets, final OperationContext operationContext) {
        for (final VertexRef ref : targets) {
            if ("nodes".equals(ref.getNamespace())) {
                return true;
            }
        }
        return false;
    }

}
