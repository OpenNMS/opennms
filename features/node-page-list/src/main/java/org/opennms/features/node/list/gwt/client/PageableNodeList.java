package org.opennms.features.node.list.gwt.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PageableNodeList extends Composite implements ProvidesResize {
    
    public class SnmpInterfacesRequestCallback implements RequestCallback {

        @Override
        public void onResponseReceived(Request request, Response response) {
            if(response.getStatusCode() == 200) {
                NodeRestResponseMapper.createIpInterfaceData(response.getText());
            }else {
                JsArray<PhysicalInterface> physIface = NodeRestResponseMapper.createSnmpInterfaceData(NodeRestClientService.IP_INTERFACES_TEST_RESPONSE);
                PhysicalInterface iface = physIface.get(0);
                String ipAddress = iface.getIpAddress();
                Window.alert("ipAddress: " + ipAddress);
            }
        }

        @Override
        public void onError(Request request, Throwable exception) {
            //TODO: Fail with an error
        }

    }


    public class IpInterfacesRequestCallback implements RequestCallback {

        @Override
        public void onResponseReceived(Request request, Response response) {
            if(response.getStatusCode() == 200) {
                NodeRestResponseMapper.createSnmpInterfaceData(response.getText());
            } else {
                String jsonStr = NodeRestClientService.IP_INTERFACES_TEST_RESPONSE;
                List<IpInterface> ipIfaces = NodeRestResponseMapper.createIpInterfaceData(jsonStr);
                IpInterface ipIface = ipIfaces.get(0);
                String hostName = ipIface.getIpHostName();
                String blah = hostName;
            }
        }

        @Override
        public void onError(Request request, Throwable exception) {
            //TODO: Fail graciously with error
        }

    }
    
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
    
    @UiField
    Button m_ipSearchBtn;
    
    @UiField
    Button m_physSearchBtn;
    
    @UiField
    ListBox m_ipSearchList;
    
    @UiField
    TextBox m_ipTextBox;
    
    @UiField
    ListBox m_physSearchList;
    
    @UiField
    TextBox m_physTextBox;
    
    NodeRestClientService m_nodeService = new NodeRestClientService();
    
    public PageableNodeList() {
        initWidget(uiBinder.createAndBindUi(this));
        
        getNodeId();
        
        initializeTabBar();
        initializeTables();
        initializeListBoxes();
    }
    
    
    public int extractNodeIdFromLocation() {
        return Integer.valueOf(Location.getParameter("node"));
    }
    
    public void setNodeId(int nodeId) {
        if(nodeId == -1) {
            nodeId = extractNodeIdFromLocation();
        }
        
        m_nodeService.getAllIpInterfacesForNode(nodeId, new IpInterfacesRequestCallback());
        //m_nodeService.getAllSnmpInterfacesForNode(nodeId, new SnmpInterfacesRequestCallback());
        
    }
    
    public native void getNodeId()/*-{
        this.@org.opennms.features.node.list.gwt.client.PageableNodeList::setNodeId(I)($wnd.nodeId == undefined? -1 : $wnd.nodeId);
    }-*/;

    private void initializeListBoxes() {
        m_ipSearchList.addItem("IP Address", "ip");
        m_ipSearchList.addItem("IP Host Name", "host");
        
        m_physSearchList.addItem("index", "index");
        m_physSearchList.addItem("SNMP ifDescr", "ifDescr");
        m_physSearchList.addItem("SNMP ifName","ifName");
        m_physSearchList.addItem("SNMP ifAlias","ifAlias");
        m_physSearchList.addItem("SNMP ifSpeed","ifSpeed");
        m_physSearchList.addItem("IP Address","ip");
        m_physSearchList.addItem("SNMP ifPhysAddr","ifPhysAddr");
    }

    private void initializeTables() {
        m_ipInterfaceTable.setPageSize(15);
        SimplePager ipSimplePager = new SimplePager();
        ipSimplePager.setDisplay(m_ipInterfaceTable);
        ipSimplePager.startLoading();
        m_ipTableDiv.add(ipSimplePager);
        
        SimplePager physicalSimplePager = new SimplePager();
        physicalSimplePager.setDisplay(m_physicalInterfaceTable);
        physicalSimplePager.startLoading();
        m_physTableDiv.add(physicalSimplePager);
    }

    private void initializeTabBar() {
        m_tabLayoutPanel.setSize("550px", "520px");
    }
    
    @UiHandler("m_ipSearchBtn")
    public void handleIpSearchBtnClick(ClickEvent event) {
        
    }
    
    @UiHandler("m_physSearchBtn")
    public void handlePhysSearchClick(ClickEvent event) {
        
    }
    
    
    public void onResize() {
        m_tabLayoutPanel.onResize();
    }


}
