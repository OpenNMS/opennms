package org.opennms.features.node.list.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class NodePageList implements EntryPoint {


  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {  
      PageableNodeList nodeList = new PageableNodeList();
      RootPanel.get().add(nodeList);
      
  }

   
}
