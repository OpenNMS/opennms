/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.shared.AbstractComponentState;

public class SearchBoxState extends AbstractComponentState {

    List<SearchSuggestion> m_suggestions = new ArrayList<>();
    List<SearchSuggestion> m_selected = new ArrayList<>();
    List<SearchSuggestion> m_focused = new ArrayList<>();
    int m_triggerCount = 0;

    public void setSuggestions(List<SearchSuggestion> suggestions){
        m_suggestions = suggestions;
        //This is a stupid hack to get VAADIN state to push all changes all the time
        m_triggerCount += 1;
    }

    public List<SearchSuggestion> getSuggestions(){
        return m_suggestions;
    }

    public void setSelected(List<SearchSuggestion> selected){
        m_selected = selected;
    }

    //Needed to have this method so that triggerCount would trigger a push to client
    public void setTriggerCount(int count){
        m_triggerCount = count;
    }
    public int getTriggerCount(){
        return m_triggerCount;
    }


    public List<SearchSuggestion> getSelected(){
        return m_selected;
    }

    public void setFocused(List<SearchSuggestion> focused) {
        m_focused = focused;
    }

    public List<SearchSuggestion> getFocused() {
        return m_focused;
    }

}
