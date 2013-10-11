/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal.gwt.client;

import com.vaadin.shared.AbstractComponentState;

import java.util.Collection;
import java.util.List;

public class SearchBoxState extends AbstractComponentState {

    List<SearchSuggestion> m_suggestions;
    List<SearchSuggestion> m_selected;
    List<SearchSuggestion> m_focused;

    public void setSuggestions(List<SearchSuggestion> suggestions){
        m_suggestions = suggestions;
    }

    public List<SearchSuggestion> getSuggestions(){
        return m_suggestions;
    }

    public void setSelected(List<SearchSuggestion> selected){
        m_selected = selected;
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
