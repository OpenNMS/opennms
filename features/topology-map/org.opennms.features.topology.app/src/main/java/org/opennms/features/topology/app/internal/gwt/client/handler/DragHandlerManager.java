/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal.gwt.client.handler;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;


public class DragHandlerManager{
    Map<String, DragBehaviorHandler> m_dragHandlers = new HashMap<String, DragBehaviorHandler>();
    DragBehaviorHandler m_currentHandler;
    
    public void addDragBehaviorHandler(String key, DragBehaviorHandler handler) {
        m_dragHandlers.put(key, handler);
    }
    
    public boolean setCurrentDragHandler(String key) {
        if(m_dragHandlers.containsKey(key)) {
            m_currentHandler = m_dragHandlers.get(key);
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
    
}