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

package org.opennms.features.gwt.graph.resource.list.client.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opennms.features.gwt.graph.resource.list.client.view.DefaultResourceListView;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class DefaultResourceListPresenter implements Presenter, DefaultResourceListView.Presenter<ResourceListItem> {
    
    public interface SearchPopupDisplay {
        HasClickHandlers getSearchConfirmBtn();
        HasClickHandlers getCancelBtn();
        HasKeyPressHandlers getTextBox();
        Widget asWidget();
        String getSearchText();
        void setHeightOffset(int offset);
        void showSearchPopup();
        void hideSearchPopup();
        void setTargetWidget(Widget target);
    }
    
    private DefaultResourceListView<ResourceListItem> m_view;
    private SearchPopupDisplay m_searchPopup;
    private List<ResourceListItem> m_dataList;
    private final String m_targetUrl;
    private String m_baseUrl;

    public DefaultResourceListPresenter(DefaultResourceListView<ResourceListItem> view, SearchPopupDisplay searchPopup, JsArray<ResourceListItem> dataList, String baseUrl) {
        this(view, searchPopup, dataList, null, baseUrl);
    }

    public DefaultResourceListPresenter(DefaultResourceListView<ResourceListItem> view, SearchPopupDisplay searchPopup, JsArray<ResourceListItem> dataList, String targetUrl, String baseUrl) {
        setView(view);
        getView().setPresenter(this);
        
        initializeSearchPopup(searchPopup);
        
        m_dataList = convertJsArrayToList(dataList);
        getView().setDataList(m_dataList);

        m_targetUrl = targetUrl;
        setBaseUrl(baseUrl);
    }

    private List<ResourceListItem> convertJsArrayToList(JsArray<ResourceListItem> resourceList) {
        List<ResourceListItem> data = new ArrayList<ResourceListItem>();
        if (resourceList != null) {
            for(int i = 0; i < resourceList.length(); i++) {
                data.add(resourceList.get(i));
            }
        }
        
        Collections.sort(data, new Comparator<ResourceListItem>() {

            @Override
            public int compare(ResourceListItem o1, ResourceListItem o2) {
                return o1.getValue().toLowerCase().compareTo(o2.getValue().toLowerCase());
            }
        });
        return data;
    }
    
    private void initializeSearchPopup(SearchPopupDisplay searchPopupView) {
        m_searchPopup = searchPopupView;
        m_searchPopup.setTargetWidget(getView().asWidget());
        m_searchPopup.getSearchConfirmBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                m_searchPopup.hideSearchPopup();
                getView().setDataList(filterList(m_searchPopup.getSearchText()));
            }
        });
        
        m_searchPopup.getCancelBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                m_searchPopup.hideSearchPopup();
            }
        });
        
        m_searchPopup.getTextBox().addKeyPressHandler(new KeyPressHandler() {
            
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if(event.getCharCode() == KeyCodes.KEY_ENTER) {
                    m_searchPopup.hideSearchPopup();
                    getView().setDataList(filterList(m_searchPopup.getSearchText()));
                }
            }
        });
    }
    
    private List<ResourceListItem> filterList(String searchText) {
        List<ResourceListItem> list = new ArrayList<ResourceListItem>();
        for(ResourceListItem item : m_dataList) {
            if(item.getValue().toLowerCase().contains(searchText.toLowerCase())) {
                list.add(item);
            }
        }
        return list;
    }
    
    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(getView().asWidget());
    }

    @Override
    public void onResourceItemSelected() {
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append("graph/chooseresource.htm");
        url.append("?reports=all");
        url.append("&parentResourceId=" + getView().getSelectedResource().getId());
        if (m_targetUrl != null) {
            url.append("&endUrl=").append(m_targetUrl);
        }

        Location.assign(url.toString());
    }

    @Override
    public void onSearchButtonClicked() {
        m_searchPopup.showSearchPopup();
    }

    public void setView(DefaultResourceListView<ResourceListItem> view) {
        m_view = view;
    }

    public DefaultResourceListView<ResourceListItem> getView() {
        return m_view;
    }

    public void setBaseUrl(String baseUrl) {
        m_baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return m_baseUrl;
    }

    

}
