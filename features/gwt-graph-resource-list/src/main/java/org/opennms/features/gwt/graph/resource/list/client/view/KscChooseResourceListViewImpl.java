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

package org.opennms.features.gwt.graph.resource.list.client.view;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class KscChooseResourceListViewImpl extends Composite implements KscChooseResourceListView<ResourceListItem> {
    

    private static KscChooseResourceListViewImplUiBinder uiBinder = GWT.create(KscChooseResourceListViewImplUiBinder.class);

    interface KscChooseResourceListViewImplUiBinder extends UiBinder<Widget, KscChooseResourceListViewImpl> {}
    
    @UiField
    LayoutPanel m_layoutPanel;
    
    @UiField
    VerticalPanel m_vPanel;
    
    @UiField
    Button m_searchBtn;
    
    @UiField
    Button m_chooseChildResourceBtn;
    
    @UiField
    Button m_viewChildResourceBtn;
    
    @UiField
    ResourceTable m_resourceTable;
    
    @UiField
    FlowPanel m_simplePagerContainer;
    
    
    private ListDataProvider<ResourceListItem> m_dataProvider;
    private Presenter<ResourceListItem> m_presenter;
    
    public KscChooseResourceListViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        
        m_layoutPanel.setSize("100%", "500px");

        m_dataProvider = new ListDataProvider<ResourceListItem>();
        m_dataProvider.addDataDisplay(m_resourceTable);
        
        m_resourceTable.setPageSize(14);
        m_resourceTable.setWidth("100%");
        
        initializeSimplePager();
        
        m_chooseChildResourceBtn.getElement().getStyle().setMarginTop(10, Unit.PX);
        m_viewChildResourceBtn.getElement().getStyle().setMarginTop(10, Unit.PX);
    }

    public void setDataList(List<ResourceListItem> dataList) {
        getDataProvider().setList(dataList);
    }

    public void setPresenter(Presenter<ResourceListItem> presenter) {
        m_presenter = presenter;
    }

    private ListDataProvider<ResourceListItem> getDataProvider() {
        return m_dataProvider;
    }

    private CellTable<ResourceListItem> getCellTable() {
        return m_resourceTable;
    }


    
    @UiHandler("m_searchBtn")
    protected void openSearchBox(ClickEvent event) {
        m_presenter.onSearchButtonClicked();
    }
    

    private void initializeSimplePager() {
        SimplePager pager = new SimplePager(TextLocation.CENTER);
        pager.setStyleName("onms-table-no-borders-margin");
        pager.getElement().getStyle().setWidth(100, Unit.PCT);
        pager.setDisplay(getCellTable());
        
        m_simplePagerContainer.add(pager);
    }

    @UiHandler("m_viewChildResourceBtn")
    protected void handleViewChildResourceClicked(ClickEvent event) {
        m_presenter.onViewResourceClicked();
    }

    @UiHandler("m_chooseChildResourceBtn")
    protected void handleChooseChildResourceClicked(ClickEvent event) {
        m_presenter.onChooseResourceClicked();
    }

    @Override
    public ResourceListItem getSelectedResource() {
        return m_resourceTable.getSelectedResourceItem();
    }

    @Override
    public void showWarning() {
        Window.alert("Please Select a Resource");
    }

}
