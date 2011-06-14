package org.opennms.features.node.list.gwt.client;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class PageableNodeList extends Composite implements ProvidesResize {
    
    private static final List<IpInterface> IP_INTERFACES = Arrays.asList(
            new IpInterface("172.20.1.11", "barbrady.opennms.com", "123 Fourth Avenue"));
    
    private static final List<PhysicalInterface> PHYSICAL_INTERFACES = Arrays.asList(
            new PhysicalInterface("3", "sit0", "sit0", null, "0", "0.0.0.0"),
            new PhysicalInterface("1", "lo", "lo", null, "1000000000", "0.0.0.0"),
            new PhysicalInterface("2", "eth0", "eth0", null, "1000000000", "172.20.1.11"));
    
    private static PageableNodeListUiBinder uiBinder = GWT.create(PageableNodeListUiBinder.class);

    interface PageableNodeListUiBinder extends UiBinder<Widget, PageableNodeList> {}
    
    @UiField
    TabLayoutPanel m_tabLayoutPanel;
    
    @UiField
    IpInterfaceTable m_ipInterfaceTable;
    
    @UiField
    PhysicalInterfaceTable m_physicalInterfaceTable;
    
    @UiField
    FlowPanel m_ipTableDiv;
    
    @UiField
    FlowPanel m_physTableDiv;
    
    public PageableNodeList() {
        initWidget(uiBinder.createAndBindUi(this));
        
        initializeTabBar();
        initializeTables();
    }

    private void initializeTables() {
        m_ipInterfaceTable.setSize("400px", "300px");
        m_ipInterfaceTable.setRowCount(IP_INTERFACES.size(), true);
        m_ipInterfaceTable.setRowData(0, IP_INTERFACES);
        SimplePager ipSimplePager = new SimplePager();
        ipSimplePager.setDisplay(m_ipInterfaceTable);
        m_ipTableDiv.add(ipSimplePager);
        
        m_physicalInterfaceTable.setSize("400px", "300px");
        m_physicalInterfaceTable.setRowCount(PHYSICAL_INTERFACES.size(), true);
        m_physicalInterfaceTable.setRowData(0, PHYSICAL_INTERFACES);
        SimplePager physicalSimplePager = new SimplePager();
        physicalSimplePager.setDisplay(m_physicalInterfaceTable);
        m_physTableDiv.add(physicalSimplePager);
    }

    private void initializeTabBar() {
        m_tabLayoutPanel.setSize("550px", "520px");
    }

    
    public void onResize() {
        m_tabLayoutPanel.onResize();
    }


}
