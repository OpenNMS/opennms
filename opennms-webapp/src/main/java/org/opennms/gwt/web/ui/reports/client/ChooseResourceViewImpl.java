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
 * by the Free Software Foundation, either version 2 of the License,
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

package org.opennms.gwt.web.ui.reports.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class ChooseResourceViewImpl extends Composite implements ChooseResourceView {

    private static final int DEFAULT_FAST_FORWARD_ROWS = 0;

    private static ChooseResourceViewImplUiBinder uiBinder = GWT.create(ChooseResourceViewImplUiBinder.class);

    interface ChooseResourceViewImplUiBinder extends UiBinder<Widget, ChooseResourceViewImpl> {}
    
    @UiField
    VerticalPanel m_vPanel;
    
    @UiField
    FlowPanel m_cellTableHolder;
    
    @UiField
    HorizontalPanel m_hPanel;
    
    @UiField
    Button m_searchBtn;
    
    @UiField
    Button m_chooseChildResourceBtn;
    
    @UiField
    Button m_viewChildResourceBtn;
    
    private ListDataProvider<ResourceListItem> m_dataProvider;
    private CellTable<ResourceListItem> m_cellTable;
    private SearchPopup m_searchPopup;
    private Presenter m_presenter;
    
    public ChooseResourceViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        
        removeBorderStyleFromTds();
        
        
        m_cellTable = new CellTable<ResourceListItem>(10, OpennmsCellTableResource.INSTANCE);
        setDataProvider(new ListDataProvider<ResourceListItem>());
        initializeCellTable();
        
        m_searchBtn.getElement().getStyle().setMarginRight(85, Unit.PX);
        m_chooseChildResourceBtn.getElement().getStyle().setMarginTop(10, Unit.PX);
        m_viewChildResourceBtn.getElement().getStyle().setMarginTop(10, Unit.PX);
    }

    public void removeBorderStyleFromTds() {
        removeBorderFromElement(1, m_vPanel.getElement().getElementsByTagName("td"));
        
        removeBorderFromElement(0, m_hPanel.getElement().getElementsByTagName("td"));
    }

    private void removeBorderFromElement(int startIndex, NodeList<Element> elemList) {
        for(int i = startIndex; i < elemList.getLength(); i++) {
            Element elm = elemList.getItem(i);
            elm.getStyle().setBorderStyle(BorderStyle.NONE);
        }
    }

    public void setDataList(List<ResourceListItem> dataList) {
        getDataProvider().setList(dataList);
    }

    public void setPresenter(Presenter presenter) {
        m_presenter = presenter;
    }

    public List<ResourceListItem> getData() {
        return getDataProvider().getList();
    }

    public ListDataProvider<ResourceListItem> getDataProvider() {
        return m_dataProvider;
    }

    public CellTable<ResourceListItem> getCellTable() {
        return m_cellTable;
    }

    private void setSearchPopup(SearchPopup searchPopup) {
        m_searchPopup = searchPopup;
    }

    public SearchPopup getSearchPopup() {
        return m_searchPopup;
    }
    
    @UiHandler("m_searchBtn")
    protected void openSearchBox(ClickEvent event) {
        if(getSearchPopup() != null) {
            getSearchPopup().setPopupPositionAndShow(new PositionCallback() {

                public void setPosition(int offsetWidth, int offsetHeight) {
                    getSearchPopup().setPopupPosition(m_vPanel.getOffsetWidth()/2, m_vPanel.getOffsetHeight());
                    
                }
           });
        }else {
            setSearchPopup(new SearchPopup());
            getSearchPopup().addSearchClickEventHandler(new SearchClickEventHandler() {
                
                public void onSearchClickEvent(String searchTerm) {
                    m_presenter.updateSearchTerm(searchTerm);
                }
            });
            getSearchPopup().setPopupPositionAndShow(new PositionCallback() {

                public void setPosition(int offsetWidth, int offsetHeight) {
                    getSearchPopup().setPopupPosition(m_vPanel.getOffsetWidth()/2, m_vPanel.getOffsetHeight());
                    
                }
           });
        }
    }

    private void initializeCellTable() {
        TextColumn<ResourceListItem> resourceColumn = new TextColumn<ResourceListItem>() {
    
            @Override
            public String getValue(ResourceListItem listItem) {
                return "" + listItem.getValue();
            }
            
        };
        
        final SingleSelectionModel<ResourceListItem> selectionModel = new SingleSelectionModel<ResourceListItem>(); 
        getCellTable().setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            
            public void onSelectionChange(SelectionChangeEvent event) {
                selectionModel.getSelectedObject();
            }
        });
        
        getCellTable().addColumn(resourceColumn, "Resources");
                    
        SimplePager pager = new SimplePager(TextLocation.CENTER, OpennmsSimplePagerResource.INSTANCE, true, 1000, false);
        pager.setDisplay(getCellTable());
        
        m_cellTableHolder.add(m_cellTable);
        m_hPanel.add(pager);
    }

    private void setDataProvider(ListDataProvider<ResourceListItem> dataProvider) {
        m_dataProvider = dataProvider;
        getDataProvider().addDataDisplay(getCellTable());
    }
    
    @UiHandler("m_viewChildResourceBtn")
    protected void handleViewChildResourceClicked(ClickEvent event) {
        SingleSelectionModel<ResourceListItem> selectionModel = (SingleSelectionModel<ResourceListItem>) getCellTable().getSelectionModel();
        if(selectionModel.getSelectedObject() != null){
                navigateTo("customGraphChooseResource.htm?selectedResourceId=&resourceId=" + selectionModel.getSelectedObject().getId());
        }else{
            Window.alert("Please Select a Resource");
        }
    }

    @UiHandler("m_chooseChildResourceBtn")
    protected void handleChooseChildResourceClicked(ClickEvent event) {
        SingleSelectionModel<ResourceListItem> selectionModel = (SingleSelectionModel<ResourceListItem>) getCellTable().getSelectionModel();
        if(selectionModel.getSelectedObject() != null){
                navigateTo("customGraphEditDetails.htm?resourceId=" + selectionModel.getSelectedObject().getId()); 
        }else {
            Window.alert("Please Select a Resource");
        }
    }

	private void navigateTo(String url) {
		m_presenter.navigateToUrl(getBaseHref() + url);
	}
    
    private String getBaseHref() {
    	final NodeList<Element> elements = Document.get().getElementsByTagName("base");
    	if (elements != null && elements.getLength() > 0) {
    		final String href = elements.getItem(0).getAttribute("href");
    		if (href != null) {
    			return href + "KSC/";
    		}
    	}
    	if (Navigator.getUserAgent().contains("msie")) {
    		return "";
    	} else {
    		return "KSC/";
    	}
    }
    
    public Widget asWidget() {
        return this.getWidget();
    }

}
