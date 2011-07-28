package org.opennms.features.gwt.snmpselect.list.client.view;

import java.util.List;

import org.opennms.features.gwt.snmpselect.list.client.view.handler.SnmpSelectTableCollectUpdateHandler;
import org.opennms.features.gwt.tableresources.client.OnmsSimplePagerResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.Resources;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class SnmpSelectListViewImpl extends Composite implements SnmpSelectListView<SnmpCellListItem>{

    private static SnmpSelectListViewImplUiBinder uiBinder = GWT
            .create(SnmpSelectListViewImplUiBinder.class);

    interface SnmpSelectListViewImplUiBinder extends
            UiBinder<Widget, SnmpSelectListViewImpl> {
    }

    @UiField
    LayoutPanel m_layoutPanel;
    
    @UiField
    SnmpSelectTable m_snmpSelectTable;
    
    @UiField
    FlowPanel m_pagerContainer;
    
    private Presenter<SnmpCellListItem> m_presenter;
    private SimplePager m_simplePager;
    private ListDataProvider<SnmpCellListItem> m_dataList;

    protected SnmpCellListItem m_updatedCell;

    public SnmpSelectListViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        m_layoutPanel.setSize("100%", "500px");
        m_layoutPanel.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
        m_layoutPanel.getElement().getStyle().setBorderWidth(1, Unit.PX);
        m_layoutPanel.getElement().getStyle().setBorderColor("#D0D0D0");
        
        m_snmpSelectTable.setWidth("100%");
        m_snmpSelectTable.setCollectUpdateHandler(new SnmpSelectTableCollectUpdateHandler() {
            
            @Override
            public void onSnmpInterfaceCollectUpdated(int ifIndex, String oldValue, String newValue) {
                m_presenter.onSnmpInterfaceCollectUpdated(ifIndex, oldValue, newValue);
            }
        });
        
        m_simplePager = new SimplePager(TextLocation.CENTER, (Resources) GWT.create(OnmsSimplePagerResources.class), true, 1000, false);
        m_simplePager.setWidth("100%");
        m_simplePager.setDisplay(m_snmpSelectTable);
        m_pagerContainer.add(m_simplePager);
        
        m_dataList = new ListDataProvider<SnmpCellListItem>();
        m_dataList.addDataDisplay(m_snmpSelectTable);
        
    }

    @Override
    public void setPresenter(Presenter<SnmpCellListItem> presenter) {
        m_presenter = presenter;
    }

    @Override
    public void setDataList(List<SnmpCellListItem> dataList) {
        m_dataList.setList(dataList);
    }

    @Override
    public SnmpCellListItem getUpdatedCell() {
        return m_updatedCell;
    }

    @Override
    public void showError(String message) {
        Window.alert("Error: " + message);
    }
    
    

}
