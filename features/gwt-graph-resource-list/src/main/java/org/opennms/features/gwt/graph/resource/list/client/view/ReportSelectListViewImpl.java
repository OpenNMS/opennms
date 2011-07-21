package org.opennms.features.gwt.graph.resource.list.client.view;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

public class ReportSelectListViewImpl extends Composite implements ReportSelectListView<ResourceListItem>{

    private static ReportSelectListViewImplUiBinder uiBinder = GWT.create(ReportSelectListViewImplUiBinder.class);

    interface ReportSelectListViewImplUiBinder extends UiBinder<Widget, ReportSelectListViewImpl> { }

    @UiField
    LayoutPanel m_layoutPanel;
    
    @UiField
    FlowPanel m_treeContainer;
    
    @UiField
    Button m_removeButton;
    
    @UiField
    Button m_graphButton;
    
    @UiField
    Button m_searchButton;
    
    ReportSelectListCellTree m_reportCellTree;
    
    private List<ResourceListItem> m_dataList;
    
    private final MultiSelectionModel<ResourceListItem> m_selectionModel;
    private List<ResourceListItem> m_selectedReports;

    private Presenter<ResourceListItem> m_presenter;
    
    public ReportSelectListViewImpl(List<ResourceListItem> dataList) {
        m_dataList = dataList;
        
        m_selectionModel = new MultiSelectionModel<ResourceListItem>();
        m_selectionModel.addSelectionChangeHandler(new Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if(m_selectionModel.getSelectedSet().size() > 0) {
                    m_selectedReports = new ArrayList<ResourceListItem>(m_selectionModel.getSelectedSet());
                }else {
                    m_selectedReports = null;
                }
                
            }
        });
        
        initWidget(uiBinder.createAndBindUi(this));
        
        m_layoutPanel.setSize("100%", "500px");
        m_treeContainer.add(makeCellTree(m_dataList));
    }
    
    private ReportSelectListCellTree makeCellTree(List<ResourceListItem> list) {
        return new ReportSelectListCellTree(list, m_selectionModel);
    }
    
    @UiHandler("m_graphButton")
    public void onGraphButtonClick(ClickEvent event) {
        m_presenter.onGraphButtonClick();
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
    public List<ResourceListItem> getSelectedReports() {
        return m_selectedReports;
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
        Window.alert("Please Select a Report to Graph");
    }

    @Override
    public List<ResourceListItem> getDataList() {
        return m_dataList;
    }

    @Override
    public Widget searchPopupTarget() {
        GWT.log("treeContainer height: " + m_treeContainer.getOffsetHeight());
        return m_treeContainer.asWidget();
    }


}
