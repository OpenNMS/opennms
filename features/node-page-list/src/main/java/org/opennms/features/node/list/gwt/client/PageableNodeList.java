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

package org.opennms.features.node.list.gwt.client;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.opennms.features.node.list.gwt.client.events.IpInterfaceSelectionEvent;
import org.opennms.features.node.list.gwt.client.events.IpInterfaceSelectionHandler;
import org.opennms.features.node.list.gwt.client.events.PhysicalInterfaceSelectionEvent;
import org.opennms.features.node.list.gwt.client.events.PhysicalInterfaceSelectionHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.Resources;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window.Location;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class PageableNodeList extends Composite implements ProvidesResize, PhysicalInterfaceSelectionHandler, IpInterfaceSelectionHandler {
    
    public class SnmpInterfacesRequestCallback implements RequestCallback {

        @Override
        public void onResponseReceived(Request request, Response response) {
            if(response.getStatusCode() == 200) {
                updatePhysicalInterfaceList(NodeRestResponseMapper.createSnmpInterfaceData(response.getText()));
            }else {
                //updatePhysicalInterfaceList(NodeRestResponseMapper.createSnmpInterfaceData(DefaultNodeService.SNMP_INTERFACES_TEST_RESPONSE));
                showErrorDialogBox("Error attempting to get SnmpInterfaces");
            }
        }

        @Override
        public void onError(Request request, Throwable exception) {
            showErrorDialogBox("Error attempting to get SnmpInterfaces");
        }

    }


    public class IpInterfacesRequestCallback implements RequestCallback {

        @Override
        public void onResponseReceived(Request request, Response response) {
            if(response.getStatusCode() == 200) {
                updateIpInterfaceList(NodeRestResponseMapper.createIpInterfaceData(response.getText()));
            } else {
                //updateIpInterfaceList(NodeRestResponseMapper.createIpInterfaceData(DefaultNodeService.IP_INTERFACES_TEST_RESPONSE));
                showErrorDialogBox("Error attempting to get IpInterfaces");
            }
        }

        @Override
        public void onError(Request request, Throwable exception) {
            showErrorDialogBox("Error attempting to get IpInterfaces");
        }

    }
    
    private static PageableNodeListUiBinder uiBinder = GWT.create(PageableNodeListUiBinder.class);
    
    interface PageableNodeListUiBinder extends UiBinder<Widget, PageableNodeList> {}
    
    public static final String COOKIE = "hideNodePageErrorDialog";
    
//    @UiField
//    TabLayoutPanel m_tabLayoutPanel;
    
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
    
    @UiField
    FlowPanel m_ipInterfaceTableDiv;

    @UiField
    FlowPanel m_physicalTableDiv;
    
    ErrorDialogBox m_errorDialog;
    
    NodeService m_nodeService = new DefaultNodeService();
    private ListDataProvider<IpInterface> m_ipIfaceDataProvider;
    private ListDataProvider<PhysicalInterface> m_physicalIfaceDataProvider;

    private int m_nodeId;
    

    public PageableNodeList() {
        initWidget(uiBinder.createAndBindUi(this));
        
        getNodeIdFromPage();
        
        initializeTabBar();
        initializeTables();
        initializeListBoxes();
    }
    
    
    public void showErrorDialogBox(String errorMsg) {
        if(m_errorDialog == null) {
            m_errorDialog = new ErrorDialogBox();
        }
        
        if(!Boolean.parseBoolean(Cookies.getCookie(COOKIE))) {
            m_errorDialog.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop());
            m_errorDialog.setWidth("" + (getOffsetWidth()-12) + "px");
            m_errorDialog.setErrorMessageAndShow(errorMsg);
            
        }
        
    }


    public int extractNodeIdFromLocation() {
        if(Location.getParameter("node") != null) {
            return Integer.valueOf(Location.getParameter("node"));
        }else {
            return -1;
        }
        
    }
    
    public void setNodeId(int nodeId) {
        if(nodeId == -1) {
            nodeId = extractNodeIdFromLocation();
        }
        m_nodeId = nodeId;
        m_nodeService.getAllIpInterfacesForNode(nodeId, new IpInterfacesRequestCallback());
        m_nodeService.getAllSnmpInterfacesForNode(nodeId, new SnmpInterfacesRequestCallback());
        
    }
    
    public int getNodeId() {
        return m_nodeId;
    }
    
    public native void getNodeIdFromPage()/*-{
        this.@org.opennms.features.node.list.gwt.client.PageableNodeList::setNodeId(I)($wnd.nodeId == undefined? -1 : $wnd.nodeId);
    }-*/;
    
    public void updateIpInterfaceList(List<IpInterface> ipInterfaces) {
        m_ipIfaceDataProvider.setList(ipInterfaces);
    }
    
    public void updatePhysicalInterfaceList(List<PhysicalInterface> physicalInterfaces) {
        m_physicalIfaceDataProvider.setList(physicalInterfaces);
    }

    private void initializeListBoxes() {
        m_ipSearchList.addItem("IP Address", "ipAddress");
        m_ipSearchList.addItem("IP Host Name", "ipHostName");
        
        m_physSearchList.addItem("index", "ifIndex");
        m_physSearchList.addItem("SNMP ifDescr", "ifDescr");
        m_physSearchList.addItem("SNMP ifName","ifName");
        m_physSearchList.addItem("SNMP ifAlias","ifAlias");
        m_physSearchList.addItem("SNMP ifSpeed","ifSpeed");
        m_physSearchList.addItem("IP Address","ipAddress");
        m_physSearchList.addItem("SNMP ifPhysAddr","physAddr");
    }

    private void initializeTables() {

        m_ipInterfaceTable.setPageSize(19);
        m_ipInterfaceTable.addSelectEventHandler(this);
        
        m_ipIfaceDataProvider = new ListDataProvider<IpInterface>();
        m_ipIfaceDataProvider.addDataDisplay(m_ipInterfaceTable);
        
        SimplePager ipSimplePager = new SimplePager(TextLocation.CENTER, (Resources) GWT.create(OnmsSimplePagerResources.class), true, 1000, false);
        ipSimplePager.setWidth("100%");
        ipSimplePager.setDisplay(m_ipInterfaceTable);
        ipSimplePager.startLoading();
        m_ipTableDiv.add(ipSimplePager);
        
        m_physicalInterfaceTable.setPageSize(20);
        m_physicalInterfaceTable.addSelectEventHandler(this);
        
        m_physicalIfaceDataProvider = new ListDataProvider<PhysicalInterface>();
        m_physicalIfaceDataProvider.addDataDisplay(m_physicalInterfaceTable);
        
        SimplePager physicalSimplePager = new SimplePager(TextLocation.CENTER, (Resources) GWT.create(OnmsSimplePagerResources.class), true, 1000, false);
        physicalSimplePager.setWidth("100%");
        physicalSimplePager.setDisplay(m_physicalInterfaceTable);
        physicalSimplePager.startLoading();
        m_physTableDiv.add(physicalSimplePager);
        
        m_ipInterfaceTableDiv.getElement().getStyle().setOverflow(Overflow.AUTO);
        m_physicalTableDiv.getElement().getStyle().setOverflow(Overflow.AUTO);
        
    }

    private void initializeTabBar() {
//        m_tabLayoutPanel.setSize("100%", "520px");
//        Node node = m_tabLayoutPanel.getElement().getChild(1);
//        Element element = Element.as(node);
//        element.getStyle().setHeight(100, Unit.EM);
    }
    
    @UiHandler("m_ipSearchBtn")
    public void handleIpSearchBtnClick(ClickEvent event) {
        String parameter = m_ipSearchList.getValue(m_ipSearchList.getSelectedIndex());
        String value = m_ipTextBox.getText();
        m_nodeService.findIpInterfacesMatching(m_nodeId, parameter, value, new IpInterfacesRequestCallback());
    }
    
    @UiHandler("m_physSearchBtn")
    public void handlePhysSearchClick(ClickEvent event) {
        String parameter = m_physSearchList.getValue(m_physSearchList.getSelectedIndex());
        String value = m_physTextBox.getText();
        m_nodeService.findSnmpInterfacesMatching(getNodeId(), parameter, value, new SnmpInterfacesRequestCallback());
    }
    
    
    public void onResize() {
//        m_tabLayoutPanel.onResize();
    }


    @Override
    public void onPhysicalInterfaceSelected(PhysicalInterfaceSelectionEvent event) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseHref() + "element/snmpinterface.jsp");
        urlBuilder.append("?node=" + getNodeId());
        urlBuilder.append("&ifindex=" + event.getIfIndex());
        
        Location.assign(urlBuilder.toString());
    }


    @Override
    public void onIpInterfaceSelection(IpInterfaceSelectionEvent event) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseHref() + "element/interface.jsp");
        urlBuilder.append("?ipinterfaceid=" + event.getIpInterfaceId());
        
        Location.assign(urlBuilder.toString());
    }
    
    public final native String getBaseHref() /*-{
        try{
            return $wnd.getBaseHref();
        }catch(err){
            return "";
        }
    }-*/;
    

}
