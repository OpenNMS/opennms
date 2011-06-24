package org.opennms.features.node.list.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class NodeInterfaceList implements EntryPoint {


  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {  
      
      NodeList<Element> nodes = RootPanel.getBodyElement().getElementsByTagName("opennms:interfacelist");
      if(nodes.getLength() > 0) {
          for(int i = 0; i < nodes.getLength(); i++) {
              PageableNodeList nodeList = new PageableNodeList();
              Element elem = nodes.getItem(i);
              RootPanel.get(elem.getId()).add(nodeList);
          }
      }
      
  }

   
}
