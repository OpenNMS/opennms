package org.opennms.features.topology.app.internal.gwt.client.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ToggleButton;

public class DragHandlerManager implements ClickHandler{
    Map<String, DragBehaviorHandler> m_dragHandlers = new HashMap<String, DragBehaviorHandler>();
    DragBehaviorHandler m_currentHandler;
    
    public void addDragBehaviorHandler(String key, DragBehaviorHandler handler) {
        ToggleButton tg = handler.getToggleBtn();
        tg.addClickHandler(this);
        
        m_dragHandlers.put(key, handler);
    }
    
    public boolean setCurrentDragHandler(String key) {
        if(m_dragHandlers.containsKey(key)) {
            m_currentHandler = m_dragHandlers.get(key);
            m_currentHandler.getToggleBtn().setDown(true);
            return true;
        }
        return false;
    }
    
    public void onDragStart(Element elem) {
        m_currentHandler.onDragStart(elem);
    }
    
    public void onDrag(Element elem) {
        m_currentHandler.onDrag(elem);
    }
    
    public void onDragEnd(Element elem) {
        m_currentHandler.onDragEnd(elem);
    }
    
    public List<ToggleButton> getDragControlsButtons(){
        List<ToggleButton> btns = new ArrayList<ToggleButton>();
        for(String key : m_dragHandlers.keySet()) {
            btns.add(m_dragHandlers.get(key).getToggleBtn());
        }
        return btns;
    }
    
    @Override
    public void onClick(ClickEvent event) {
        for(String key : m_dragHandlers.keySet()) {
            DragBehaviorHandler dragHandler = m_dragHandlers.get(key);
            if(event.getSource() == dragHandler.getToggleBtn()) {
                setCurrentDragHandler(key);
            }else {
                dragHandler.getToggleBtn().setDown(false);
            }
        }
    }
}