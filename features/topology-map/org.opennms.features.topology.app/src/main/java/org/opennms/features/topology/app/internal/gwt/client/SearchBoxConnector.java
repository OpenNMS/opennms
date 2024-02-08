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
