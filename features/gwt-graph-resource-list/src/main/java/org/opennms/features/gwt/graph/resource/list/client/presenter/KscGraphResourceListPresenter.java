/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.features.gwt.graph.resource.list.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.gwt.graph.resource.list.client.view.KscChooseResourceListView;
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

public class KscGraphResourceListPresenter implements Presenter, KscChooseResourceListView.Presenter<ResourceListItem> {
    
    public interface SearchPopupDisplay {
        HasClickHandlers getSearchConfirmBtn();
        HasClickHandlers getCancelBtn();
        HasKeyPressHandlers getTextBox();
        Widget asWidget();
        String getSearchText();
        void showSearchPopup();
        void hideSearchPopup();
        void setTargetWidget(Widget target);
    }
    
    KscChooseResourceListView<ResourceListItem> m_view;
    SearchPopupDisplay m_searchPopup;
    List<ResourceListItem> m_dataList;
    
    public KscGraphResourceListPresenter(KscChooseResourceListView<ResourceListItem> view, SearchPopupDisplay searchPopupView, JsArray<ResourceListItem> resourceList) {
        m_view = view;
        m_view.setPresenter(this);
        
        initializeSearchPopup(searchPopupView);
        
        m_dataList = convertJsArrayToList(resourceList);
        
        m_view.setDataList(m_dataList);
    }
    

    private void initializeSearchPopup(SearchPopupDisplay searchPopupView) {
        m_searchPopup = searchPopupView;
        m_searchPopup.setTargetWidget(m_view.asWidget());
        m_searchPopup.getSearchConfirmBtn().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                m_searchPopup.hideSearchPopup();
                m_view.setDataList(filterList(m_searchPopup.getSearchText()));
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
                    m_view.setDataList(filterList(m_searchPopup.getSearchText()));
                }
            }
        });
    }


    private List<ResourceListItem> convertJsArrayToList(JsArray<ResourceListItem> resourceList) {
        List<ResourceListItem> data = new ArrayList<ResourceListItem>();
        for(int i = 0; i < resourceList.length(); i++) {
            data.add(resourceList.get(i));
        }
        return data;
    }

    public void updateSearchTerm(String searchTerm) {
        if(searchTerm.equals("")) {
            m_view.setDataList(m_dataList);
        }else {
            List<ResourceListItem> newList = new ArrayList<ResourceListItem>();
            
            for(ResourceListItem item : m_dataList) {
                if(item.getValue().contains(searchTerm)) {
                    newList.add(item);
                }
            }
            
            m_view.setDataList(newList);
        }
        
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(m_view.asWidget());
    }


    @Override
    public void onSearchButtonClicked() {
        m_searchPopup.showSearchPopup();
    }

    @Override
    public void onResourceItemSelected() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void onChooseResourceClicked() {
        ResourceListItem resource = m_view.getSelectedResource();
        if(resource != null) {
            Location.assign("KSC/customGraphEditDetails.htm?resourceId=" + resource.getId());
        }else {
            m_view.showWarning();
        }
    }


    @Override
    public void onViewResourceClicked() {
        ResourceListItem resource = m_view.getSelectedResource();
        if(resource != null){
            Location.assign("KSC/customGraphChooseResource.htm?selectedResourceId=&resourceId=" + resource.getId());
        }else{
            m_view.showWarning();
        }
        
    }

    private List<ResourceListItem> filterList(String searchText) {
        List<ResourceListItem> list = new ArrayList<ResourceListItem>();
        for(ResourceListItem item : m_dataList) {
            if(item.getValue().contains(searchText)) {
                list.add(item);
            }
        }
        return list;
    }

}
