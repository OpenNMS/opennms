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

import org.opennms.features.gwt.graph.resource.list.client.view.DefaultResourceListView;
import org.opennms.features.gwt.graph.resource.list.client.view.KscCustomSelectionView;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class KscCustomReportListPresenter extends DefaultResourceListPresenter implements Presenter {
    

    public interface SelectionDisplay{
        HasClickHandlers getSubmitButton();
        String getSelectAction();
        Widget asWidget();
    }

    private SelectionDisplay m_selectionDisplay;
    
    public KscCustomReportListPresenter(DefaultResourceListView<ResourceListItem> view, SearchPopupDisplay searchPopup, JsArray<ResourceListItem> dataList, SelectionDisplay selectionDisplay, String baseUrl) {
        super(view, searchPopup, dataList, baseUrl);
        initializeSelectionDisplay(selectionDisplay);
    }

    private void initializeSelectionDisplay(SelectionDisplay selectionDisplay) {
        m_selectionDisplay = selectionDisplay;
        
        m_selectionDisplay.getSubmitButton().addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                StringBuilder urlBuilder = new StringBuilder();
                urlBuilder.append(getBaseUrl() + "/KSC/formProcMain.htm");
                
                if(m_selectionDisplay.getSelectAction() != null) {
                    if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.VIEW)) {
                        urlBuilder.append("?report_action=View");
                    }else if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.CUSTOMIZE)) {
                        urlBuilder.append("?report_action=Customize");
                    }else if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.CREATE_NEW)) {
                        urlBuilder.append("?report_action=Create");
                    }else if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.CREATE_NEW_FROM_EXISTING)) {
                        urlBuilder.append("?report_action=CreateFrom");
                    }else if(m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.DELETE)) {
                        urlBuilder.append("?report_action=Delete");
                    }
                    
                    if(getView().getSelectedResource() != null) {
                        urlBuilder.append("&report=" +  getView().getSelectedResource().getId());
                        Location.assign(urlBuilder.toString());
                    } else if(getView().getSelectedResource() == null && m_selectionDisplay.getSelectAction().equals(KscCustomSelectionView.CREATE_NEW)) {
                        Location.assign(urlBuilder.toString());
                    }else {
                        getView().showWarning();
                    }
                } else {
                    getView().showWarning();
                }
                
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        super.go(container);
        container.add(m_selectionDisplay.asWidget());
        
    }
    
    @Override
    public void onResourceItemSelected() {
        //Don't do anything on selection
    }

}
