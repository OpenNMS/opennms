package org.opennms.features.gwt.graph.resource.list.client.view;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class ReportSelectListViewImpl extends Composite implements ReportSelectListView<ResourceListItem>{

    private static ReportSelectListViewImplUiBinder uiBinder = GWT.create(ReportSelectListViewImplUiBinder.class);

    interface ReportSelectListViewImplUiBinder extends UiBinder<Widget, ReportSelectListViewImpl> { }

    @UiField
    LayoutPanel m_layoutPanel;
    private List<ResourceListItem> m_dataList;
    
    public ReportSelectListViewImpl(List<ResourceListItem> dataList) {
        m_dataList = dataList;
        initWidget(uiBinder.createAndBindUi(this));
        
        m_layoutPanel.setSize("100%", "500px");
    }
    
    @UiFactory ReportSelectListCellTree makeCellTree() {
        return new ReportSelectListCellTree(m_dataList);
        
    }
    
    @Override
    public void setDataList(List<ResourceListItem> dataList) {
        // TODO Auto-generated method stub
        
    }


}
