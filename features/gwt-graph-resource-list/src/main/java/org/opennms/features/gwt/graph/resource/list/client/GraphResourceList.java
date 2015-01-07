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

package org.opennms.features.gwt.graph.resource.list.client;

import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GraphResourceList implements EntryPoint {

  /**
   * This is the entry point method.
   */
    @Override
    public void onModuleLoad() {
        
        if(Navigator.getUserAgent().contains("MSIE")) {
            
            NodeList<Element> divs = RootPanel.getBodyElement().getElementsByTagName("div");
            for(int k = 0; k < divs.getLength(); k++) {
                Element element = divs.getItem(k);
                if(element.hasAttribute("name")) {
                    if(element.getAttribute("name").equals("opennms-kscChooseResourceList")) {
                        createKscChooseResourceView(element);
                    }else if(element.getAttribute("name").equals("opennms-graphResourceList")) {
                        createGraphResourceView(element);
                    }else if(element.getAttribute("name").equals("opennms-nodeSnmpReportList")) {
                        createKscReportListView(element);
                    }else if(element.getAttribute("name").equals("opennms-kscCustomReportList")) {
                        createKscCustomReportView(element);
                    }else if(element.getAttribute("name").equals("opennms-reportSelectionList")) {
                        createReportSelectView(element);
                    }
                }
            }
            
        }else {
            NodeList<Element> kscChooseResourceList = RootPanel.getBodyElement().getElementsByTagName("opennms:kscChooseResourceList");
            if(kscChooseResourceList.getLength() > 0) {
                for(int i = 0; i < kscChooseResourceList.getLength(); i++) {
                    Element elem = kscChooseResourceList.getItem(i);
                    createKscChooseResourceView(elem);
                }
            }
            
            NodeList<Element> graphResourceListNodes = RootPanel.getBodyElement().getElementsByTagName("opennms:graphResourceList");
            if(graphResourceListNodes.getLength() > 0) {
                for(int i = 0; i < graphResourceListNodes.getLength(); i++) {
                    Element elem = graphResourceListNodes.getItem(i);
                    createGraphResourceView(elem);
                }
            }
            
            NodeList<Element> nodeSnmpReportNodes = RootPanel.getBodyElement().getElementsByTagName("opennms:nodeSnmpReportList");
            if(nodeSnmpReportNodes.getLength() > 0) {
                for(int i = 0; i < nodeSnmpReportNodes.getLength(); i++) {
                    Element elem = nodeSnmpReportNodes.getItem(i);
                    createKscReportListView(elem);
                }
            }
            
            NodeList<Element> kscCustomReportNodes = RootPanel.getBodyElement().getElementsByTagName("opennms:kscCustomReportList");
            if(kscCustomReportNodes.getLength() > 0) {
                for(int i = 0; i < kscCustomReportNodes.getLength(); i++) {
                    Element elem = kscCustomReportNodes.getItem(i);
                    createKscCustomReportView(elem);
                }
            }
            
            NodeList<Element> reportSelectListNodes = RootPanel.getBodyElement().getElementsByTagName("opennms:reportSelectionList");
            if(reportSelectListNodes.getLength() > 0) {
                for(int i = 0; i < reportSelectListNodes.getLength(); i++) {
                    Element elem = reportSelectListNodes.getItem(i);
                    createReportSelectView(elem);
                }
            }
        }
    }


    private void createReportSelectView(Element element) {
        ReportSelectListAppController reportSelectListAppController = new ReportSelectListAppController(getResourceListData(getDataObjectAttribute(element)), getTargetUrl(element), getBaseUrl());
        reportSelectListAppController.go(RootPanel.get(element.getId()));
    }
    
    
    private void createKscCustomReportView(Element element) {
        KscCustomReportAppController kscCustomReportList = new KscCustomReportAppController(getResourceListData(getDataObjectAttribute(element)), getBaseUrl(), getReadOnlyAttribue(element));
        kscCustomReportList.go(RootPanel.get(element.getId()));
    }
    
    
    private void createKscReportListView(Element element) {
        KscReportListAppController nodeSnmpReportList = new KscReportListAppController(getResourceListData(getDataObjectAttribute(element)), getBaseUrl());
        nodeSnmpReportList.go(RootPanel.get(element.getId()));
    }
    
    
    private void createGraphResourceView(Element element) {
        ResourceListAppController resourceListView = new ResourceListAppController(getResourceListData(getDataObjectAttribute(element)), getTargetUrl(element), getBaseUrl());
        resourceListView.go(RootPanel.get(element.getId()));
    }
    
    
    private void createKscChooseResourceView(Element elem) {
        KscChooseResourceAppController appView = new KscChooseResourceAppController(getResourceListData(getDataObjectAttribute(elem)), getBaseUrl());
        appView.go(RootPanel.get(elem.getId()));
    }


    private String getDataObjectAttribute(Element elem) {
        return elem.getAttribute("dataObject") != null ? elem.getAttribute("dataObject") : "data";
    }

    private boolean getReadOnlyAttribue(Element elem){
        return elem.getAttribute("isreadonly") != null ? (elem.getAttribute("isreadonly").equals("true")) : false;
    }

    private String getTargetUrl(Element elem) {
        return elem.getAttribute("targetUrl") != null ? elem.getAttribute("targetUrl") : "graph/results.htm";
    }

    public final native String getBaseUrl() /*-{
        try{
            return $wnd.getBaseHref();
        }catch(err){
            return "";
        }
        
    }-*/;
    
    public final native JsArray<ResourceListItem> getResourceListData(String dataObject) /*-{
        return $wnd[dataObject].records;
    }-*/;

}
