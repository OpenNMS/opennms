package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.List;

import com.vaadin.shared.communication.ServerRpc;

public interface NodeIdSelectionRpc extends ServerRpc {
    public void setSelectedNodes(List<Integer> nodeIds);
}
