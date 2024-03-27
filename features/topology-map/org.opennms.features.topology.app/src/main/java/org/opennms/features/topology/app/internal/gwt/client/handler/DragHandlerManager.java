/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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