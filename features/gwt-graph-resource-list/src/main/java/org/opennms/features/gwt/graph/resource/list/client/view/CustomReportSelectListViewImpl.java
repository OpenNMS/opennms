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

package org.opennms.features.gwt.graph.resource.list.client.view;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

public class CustomReportSelectListViewImpl extends Composite implements CustomReportSelectListView<ResourceListItem>{

    private static CustomReportSelectListViewImplUiBinder uiBinder = GWT.create(CustomReportSelectListViewImplUiBinder.class);

    interface CustomReportSelectListViewImplUiBinder extends UiBinder<Widget, CustomReportSelectListViewImpl> { }

    @UiField
    LayoutPanel m_layoutPanel;
    
    @UiField
    FlowPanel m_treeContainer;
    
    @UiField
    Button m_removeButton;
    
    @UiField
    Button m_submitButton;
    
    @UiField
    Button m_searchButton;
    
    ReportSelectListCellTree m_reportCellTree;
    
    private List<ResourceListItem> m_dataList;
    
    private final SingleSelectionModel<ResourceListItem> m_selectionModel;
    private ResourceListItem m_selectedReport;

    private Presenter<ResourceListItem> m_presenter;
    
    public CustomReportSelectListViewImpl(List<ResourceListItem> dataList) {
        m_dataList = dataList;
        
        m_selectionModel = new SingleSelectionModel<ResourceListItem>();
        m_selectionModel.addSelectionChangeHandler(new Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                m_selectedReport = m_selectionModel.getSelectedObject();
            }
        });
        
        initWidget(uiBinder.createAndBindUi(this));
        
        m_layoutPanel.setSize("100%", "500px");
        m_layoutPanel.getElement().getStyle().setOverflow(Overflow.AUTO);
        m_treeContainer.add(makeCellTree(m_dataList));
        
    }
    
    private ReportSelectListCellTree makeCellTree(List<ResourceListItem> list) {
        return new ReportSelectListCellTree(list, m_selectionModel);
    }
    
    @UiHandler("m_submitButton")
    public void onSubmitButtonClick(ClickEvent event) {
        m_presenter.onSubmitButtonClick();
    }
    
    @UiHandler("m_removeButton")
    public void onRemoveButtonClick(ClickEvent event) {
        m_presenter.onClearSelectionButtonClick();
    }
    
    @UiHandler("m_searchButton")
    public void onSearchButtonClick(ClickEvent event) {
        m_presenter.onSearchButtonClick();
    }
    
    @Override
    public void setDataList(List<ResourceListItem> dataList) {
        m_treeContainer.clear();
        m_treeContainer.add(makeCellTree(dataList));
    }

    @Override
    public ResourceListItem getSelectedReport() {
        return m_selectedReport;
    }

    @Override
    public void setPresenter(Presenter<ResourceListItem> presenter) {
        m_presenter = presenter;
    }

    @Override
    public void clearAllSelections() {
        m_selectionModel.clear();
    }

    @Override
    public void showWarning() {
        Window.alert("Please Select a Resource to Graph");
    }

    @Override
    public List<ResourceListItem> getDataList() {
        return m_dataList;
    }

    @Override
    public Widget searchPopupTarget() {
        return m_treeContainer.asWidget();
    }

    @Override
    public List<ResourceListItem> getAllReports() {
        return m_dataList;
    }

}
