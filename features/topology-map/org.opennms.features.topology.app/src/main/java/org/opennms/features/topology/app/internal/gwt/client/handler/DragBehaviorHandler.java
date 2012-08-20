package org.opennms.features.topology.app.internal.gwt.client.handler;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.ToggleButton;

public interface DragBehaviorHandler{
    public void onDragStart(Element elem);
    public void onDrag(Element elem);
    public void onDragEnd(Element elem);
    public ToggleButton getToggleBtn();
}