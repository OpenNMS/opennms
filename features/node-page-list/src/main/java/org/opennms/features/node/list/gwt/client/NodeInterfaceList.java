package org.opennms.features.node.list.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class NodeInterfaceList implements EntryPoint {


  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {  
      
      if(Navigator.getUserAgent().contains("MSIE")) {
          NodeList<Element> divs = RootPanel.getBodyElement().getElementsByTagName("div");
          for(int j = 0; j < divs.getLength(); j++) {
              Element element = divs.getItem(j);
              if(element.hasAttribute("name") && element.getAttribute("name").equals("opennms-interfacelist")) {
                  createView(element);
              }
          }
      }else {
          NodeList<Element> nodes = RootPanel.getBodyElement().getElementsByTagName("opennms:interfacelist");
          if(nodes.getLength() > 0) {
              for(int i = 0; i < nodes.getLength(); i++) {
                  Element elem = nodes.getItem(i);
                  createView(elem);
              }
          }
      }
      
  }

    private void createView(Element elem) {
        PageableNodeList nodeList = new PageableNodeList();
          RootPanel.get(elem.getId()).add(nodeList);
    }

   
}
