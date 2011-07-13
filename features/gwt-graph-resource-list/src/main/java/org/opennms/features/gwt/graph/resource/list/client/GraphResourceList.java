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
        NodeList<Element> nodes = RootPanel.getBodyElement().getElementsByTagName("opennms:kscChooseResourceList");
        if(nodes.getLength() > 0) {
            for(int i = 0; i < nodes.getLength(); i++) {
                Element elem = nodes.getItem(i);
                AppController appView = new AppController(getResourceListData(getDataObjectAttribute(elem)));
                appView.go(RootPanel.get(elem.getId()));
            }
        }
        
        NodeList<Element> resourceListNodes = RootPanel.getBodyElement().getElementsByTagName("opennms:resourceList");
        if(resourceListNodes.getLength() > 0) {
            for(int i = 0; i < resourceListNodes.getLength(); i++) {
                Element elem = resourceListNodes.getItem(i);
                ResourceListAppController resourceListView = new ResourceListAppController(getResourceListData(getDataObjectAttribute(elem)));
                resourceListView.go(RootPanel.get(elem.getId()));
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
