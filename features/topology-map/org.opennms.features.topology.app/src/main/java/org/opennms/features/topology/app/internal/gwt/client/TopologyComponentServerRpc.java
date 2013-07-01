package org.opennms.features.topology.app.internal.gwt.client;

import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.communication.ServerRpc;

import java.util.List;
import java.util.Map;

public interface TopologyComponentServerRpc extends ServerRpc {
    
    public void doubleClicked(MouseEventDetails eventDetails);
    public void deselectAllItems();
    public void edgeClicked(String edgeId);
    public void backgroundClicked();
    public void scrollWheel(double scrollVal, int x, int y);
    public void mapPhysicalBounds(int width, int height);
    public void marqueeSelection(String[] vertexIds, MouseEventDetails eventDetails);
    public void contextMenu(String target, String type, int x, int y);
    public void clientCenterPoint(int x, int y);
    public void vertexClicked(String vertexId, MouseEventDetails eventDetails, String platform);
    public void updateVertices(List<String> vertices);
    public void backgroundDoubleClick(double x, double y);
}
