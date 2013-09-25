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

package org.opennms.features.topology.app.internal.ui;


import com.vaadin.shared.AbstractComponentState;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Notification;
import org.opennms.features.topology.app.internal.gwt.client.SearchBoxServerRpc;
import org.opennms.features.topology.app.internal.gwt.client.SearchBoxState;
import org.opennms.features.topology.app.internal.gwt.client.SearchSuggestion;
import org.opennms.features.topology.app.internal.gwt.client.SharedVertex;

import java.util.ArrayList;
import java.util.List;


public class SearchBox extends AbstractComponent{

    SearchBoxServerRpc m_rpc = new SearchBoxServerRpc(){

        private static final long serialVersionUID = 6945103738578953390L;

        @Override
        public void querySuggestions(String query, int indexFrom, int indexTo) {
            List<SearchSuggestion> suggestionList = new ArrayList<SearchSuggestion>();
            suggestionList.add(createSuggestion("test " + Math.random()* 1234));
            suggestionList.add(createSuggestion("test2" + Math.random()* 1234));

            getState().setSuggestions(suggestionList);
        }

    };

    public SearchBox(){
        registerRpc(m_rpc);
        init();

    }

    @Override
    protected SearchBoxState getState() {
        return (SearchBoxState) super.getState();
    }

    private void init() {
        setWidth(250.0f, Unit.PIXELS);
        setImmediate(true);
    }

    public SearchSuggestion createSuggestion(String label) {
        SharedVertex v = new SharedVertex();
        v.setLabel(label);

        SearchSuggestion searchSuggestion = new SearchSuggestion();
        searchSuggestion.setLabel(label);
        searchSuggestion.setVertexKey("key :: " + label);
        searchSuggestion.setNamespace("nodes");

        return searchSuggestion;
    }


}
