/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.gwt.combobox.client.presenter;

import org.opennms.features.gwt.combobox.client.rest.NodeRestResponseMapper;
import org.opennms.features.gwt.combobox.client.rest.NodeService;
import org.opennms.features.gwt.combobox.client.view.NodeDetail;
import org.opennms.features.gwt.combobox.client.view.SuggestionComboboxView;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HasWidgets;

public class SuggestionComboboxPresenter implements Presenter, SuggestionComboboxView.Presenter<NodeDetail>{
    
    private final SimpleEventBus m_eventBus;
    private final SuggestionComboboxView<NodeDetail> m_view;
    private final NodeService m_nodeService;
    
    public SuggestionComboboxPresenter(SimpleEventBus eventBus, SuggestionComboboxView<NodeDetail> view, NodeService nodeService) {
        m_eventBus = eventBus;
        m_view = view;
        m_view.setPresenter(this);
        
        m_nodeService = nodeService;
    }
    
    @Override
    public void go(final HasWidgets container) {
        container.clear();
        container.add(m_view.asWidget());
    }

    public SimpleEventBus getEventBus() {
        return m_eventBus;
    }

    public SuggestionComboboxView<NodeDetail> getDisplay() {
        return m_view;
    }

    @Override
    public void onGoButtonClicked() {
        m_nodeService.getNodeByNodeLabel(m_view.getSelectedText(), new RequestCallback() {
            
            @Override
            public void onResponseReceived(Request request, Response response) {
                if(response.getStatusCode() == 200) {
                    m_view.setData(NodeRestResponseMapper.mapNodeJSONtoNodeDetail(response.getText()));
                }else {
                    //m_view.setData(NodeRestResponseMapper.mapNodeJSONtoNodeDetail(DefaultNodeService.TEST_RESPONSE));
                    Window.alert("Error Occurred Retrieving Nodes: " + response.getStatusCode() + " " + response.getStatusText());
                }
            }
            
            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("error in retrieving the Rest call");
            }
        });
    }

    @Override
    public void onEnterKeyEvent() {
        m_nodeService.getNodeByNodeLabel(m_view.getSelectedText(), new RequestCallback() {

            @Override
            public void onResponseReceived(Request request, Response response) {
                if(response.getStatusCode() == 200) {
                    m_view.setData(NodeRestResponseMapper.mapNodeJSONtoNodeDetail(response.getText()));
                }else {
                    Window.alert("Error Occurred Retrieving Nodes: " + response.getStatusCode() + " " + response.getStatusText());
                    // m_view.setData(NodeRestResponseMapper.mapNodeJSONtoNodeDetail(DefaultNodeService.TEST_RESPONSE));
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("error in retrieving the Rest call");
                
            }
        });
    }

    public NodeService getNodeService() {
        return m_nodeService;
    }

    @Override
    public void onNodeSelected() {
        StringBuilder builder = new StringBuilder();
        builder.append(getBaseHref() + "graph/chooseresource.htm");
        builder.append("?reports=all");
        builder.append("&parentResourceType=node");
        builder.append("&parentResource=" + m_view.getSelectedNode().getId());

        Location.assign(builder.toString());
    }
    
    public final native String getBaseHref()/*-{
        try{
            return $wnd.getBaseHref();
        }catch(err){
            return "";
        }
    }-*/;

}
