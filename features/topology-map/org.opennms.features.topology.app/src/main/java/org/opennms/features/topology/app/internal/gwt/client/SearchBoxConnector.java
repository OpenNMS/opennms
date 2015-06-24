/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;


@Connect(org.opennms.features.topology.app.internal.ui.SearchBox.class)
public class SearchBoxConnector extends AbstractComponentConnector {

    SearchBoxServerRpc m_rpc = RpcProxy.create(SearchBoxServerRpc.class, this);
    private SuggestOracle.Callback m_callback;
    private SuggestOracle.Request m_request;


    @Override
    public VSearchBox getWidget(){
        return (VSearchBox) super.getWidget();
    }

    @Override
    public SearchBoxState getState() {
        return (SearchBoxState)super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if(stateChangeEvent.hasPropertyChanged("suggestions") || stateChangeEvent.hasPropertyChanged("triggerCount")){
            SuggestOracle.Response response = new SuggestOracle.Response(getState().getSuggestions());
            if(m_callback != null){
                m_callback.onSuggestionsReady(m_request, response);
            }
            getState().getTriggerCount();
        }

        if (stateChangeEvent.hasPropertyChanged("selected")) {
            //deduplicate the list of selected and have it change the token field background
            //or something

        }

        if (stateChangeEvent.hasPropertyChanged("focused")) {
            getWidget().setFocused(getState().getFocused());
        }

    }

    private static native void log(Object message) /*-{
        $wnd.console.debug(message);
    }-*/;

    @Override
    public VSearchBox createWidget(){
        VSearchBox widget = GWT.create(VSearchBox.class);
        widget.setSearchBoxConnector(this);
        return widget;
    }

    public void query(SuggestOracle.Request request, SuggestOracle.Callback callback, int indexFrom, int indexTo) {
        m_rpc.querySuggestions(request.getQuery(), indexFrom, indexTo);
        m_callback = callback;
        m_request = request;
    }

    public void selectSuggestion(SearchSuggestion suggestion) {
        m_rpc.selectSuggestion(suggestion);
    }

    public void removeSelected(SearchSuggestion searchSuggestion) {
        m_rpc.removeSelected(searchSuggestion);
    }

    public void removeFocused(SearchSuggestion searchSuggestion) {
        m_rpc.removeFocused(searchSuggestion);
    }

    public void addToFocus(SearchSuggestion searchSugestion){
        m_rpc.addToFocus(searchSugestion);
    }

    public void centerOnSuggestion(SearchSuggestion searchSuggestion) {
        m_rpc.centerSearchSuggestion(searchSuggestion);
    }

    public void toggleSuggestionCollapse(SearchSuggestion searchSuggestion) {
        m_rpc.toggleSuggestionCollapse(searchSuggestion);
    }
}
