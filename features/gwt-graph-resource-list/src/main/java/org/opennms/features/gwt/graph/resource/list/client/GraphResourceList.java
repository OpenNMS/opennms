package org.opennms.features.gwt.graph.resource.list.client;

import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GraphResourceList implements EntryPoint {

  /**
   * This is the entry point method.
   */
    public void onModuleLoad() {
        NodeList<Element> kscChooseResourceList = RootPanel.getBodyElement().getElementsByTagName("opennms:kscChooseResourceList");
        if(kscChooseResourceList.getLength() > 0) {
            for(int i = 0; i < kscChooseResourceList.getLength(); i++) {
                Element elem = kscChooseResourceList.getItem(i);
                KscChooseResourceAppController appView = new KscChooseResourceAppController(getResourceListData(getDataObjectAttribute(elem)));
                appView.go(RootPanel.get(elem.getId()));
            }
        }
        
        NodeList<Element> graphResourceListNodes = RootPanel.getBodyElement().getElementsByTagName("opennms:graphResourceList");
        if(graphResourceListNodes.getLength() > 0) {
            for(int i = 0; i < graphResourceListNodes.getLength(); i++) {
                Element elem = graphResourceListNodes.getItem(i);
                ResourceListAppController resourceListView = new ResourceListAppController(getResourceListData(getDataObjectAttribute(elem)));
                resourceListView.go(RootPanel.get(elem.getId()));
            }
        }
        
        NodeList<Element> nodeSnmpReportNodes = RootPanel.getBodyElement().getElementsByTagName("opennms:nodeSnmpReportList");
        if(nodeSnmpReportNodes.getLength() > 0) {
            for(int i = 0; i < nodeSnmpReportNodes.getLength(); i++) {
                Element elem = nodeSnmpReportNodes.getItem(i);
                KscReportListAppController nodeSnmpReportList = new KscReportListAppController(getResourceListData(getDataObjectAttribute(elem)));
                nodeSnmpReportList.go(RootPanel.get(elem.getId()));
            }
        }
        
        NodeList<Element> kscCustomReportNodes = RootPanel.getBodyElement().getElementsByTagName("opennms:kscCustomReportList");
        if(kscCustomReportNodes.getLength() > 0) {
            for(int i = 0; i < kscCustomReportNodes.getLength(); i++) {
                Element elem = kscCustomReportNodes.getItem(i);
                KscCustomReportAppController kscCustomReportList = new KscCustomReportAppController(getResourceListData(getDataObjectAttribute(elem)));
                kscCustomReportList.go(RootPanel.get(elem.getId()));
            }
        }
        
        NodeList<Element> reportSelectListNodes = RootPanel.getBodyElement().getElementsByTagName("opennms:reportSelectionList");
        if(reportSelectListNodes.getLength() > 0) {
            for(int i = 0; i < reportSelectListNodes.getLength(); i++) {
                Element elem = reportSelectListNodes.getItem(i);
                ReportSelectListAppController reportSelectListAppController = new ReportSelectListAppController(getResourceListData(getDataObjectAttribute(elem)));
                reportSelectListAppController.go(RootPanel.get(elem.getId()));
            }
        }
    }


    private String getDataObjectAttribute(Element elem) {
        return elem.getAttribute("dataObject") != null ? elem.getAttribute("dataObject") : "data";
    }
    
    
    public final native JsArray<ResourceListItem> getResourceListData(String dataObject) /*-{
        return $wnd[dataObject].records;
    }-*/;
}
