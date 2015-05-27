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
import java.util.List;

import org.opennms.features.gwt.graph.resource.list.client.presenter.DefaultResourceListPresenter.SearchPopupDisplay;
import org.opennms.features.gwt.graph.resource.list.client.view.ReportSelectListView;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HasWidgets;

public class ReportSelectListPresenter implements Presenter, ReportSelectListView.Presenter<ResourceListItem> {

    private ReportSelectListView<ResourceListItem> m_view;
    private SearchPopupDisplay m_searchPopup;
    private final String m_targetUrl;
    private String m_baseUrl;

    public ReportSelectListPresenter(ReportSelectListView<ResourceListItem> view, SearchPopupDisplay searchView, String targetUrl, String baseUrl) {
        setView(view);
        getView().setPresenter(this);
        initializeSearchPopup(searchView);
        m_targetUrl = targetUrl;
        m_baseUrl = baseUrl;
    }

    private void initializeSearchPopup(SearchPopupDisplay searchPopupView) {
        m_searchPopup = searchPopupView;
        m_searchPopup.setHeightOffset(425);
        m_searchPopup.setTargetWidget(getView().searchPopupTarget());
        m_searchPopup.getSearchConfirmBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                m_searchPopup.hideSearchPopup();
                getView().setDataList(filterList(m_searchPopup.getSearchText(), getView().getDataList()));
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
                    getView().setDataList(filterList(m_searchPopup.getSearchText(), getView().getDataList()));
                }
            }
        });
    }
    
    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(getView().asWidget());
    }


    @Override
    public void onGraphButtonClick() {
        List<ResourceListItem> reports = getView().getSelectedReports();
        if(reports != null) {
            buildUrlAndGoToGraphPage(reports);
        } else {
            getView().showWarning();
        }
        
        
    }


    private void buildUrlAndGoToGraphPage(List<ResourceListItem> reports) {
        StringBuilder sb = new StringBuilder();
        sb.append(m_baseUrl);
        sb.append(m_targetUrl);
        sb.append("?reports=all&resourceId=");

        boolean first = true;
        for(ResourceListItem item : reports) {
            if(!first) {
                
                sb.append("&resourceId=");
            }
            sb.append(item.getId());
            first = false;
        }
        
        Location.assign(sb.toString());
    }


    @Override
    public void onClearSelectionButtonClick() {
        getView().clearAllSelections();
        
    }


    @Override
    public void onSearchButtonClick() {
        m_searchPopup.showSearchPopup();
    }
    
    private List<ResourceListItem> filterList(String searchText, List<ResourceListItem> dataList) {
        List<ResourceListItem> list = new ArrayList<ResourceListItem>();
        for(ResourceListItem item : dataList) {
            if(item.getValue().toLowerCase().contains(searchText.toLowerCase())) {
                list.add(item);
            }
        }
        return list;
    }


    public void setView(ReportSelectListView<ResourceListItem> view) {
        m_view = view;
    }


    public ReportSelectListView<ResourceListItem> getView() {
        return m_view;
    }


    @Override
    public void onGraphAllButtonClick() {
        List<ResourceListItem> reports = getView().getAllReports();
        buildUrlAndGoToGraphPage(reports);
    }

}
