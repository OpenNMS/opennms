package org.opennms.features.gwt.combobox.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class NodeSuggestionCombobox implements EntryPoint {
  

  /**
   * This is the entry point method.
   */
    public void onModuleLoad() {
        NodeList<Element> nodes = RootPanel.getBodyElement().getElementsByTagName("opennms:nodeSuggestionCombobox");
        if(nodes.getLength() > 0) {
            for(int i = 0; i < nodes.getLength(); i++) {
                Element elem = nodes.getItem(i);
                SimpleEventBus eventBus = new SimpleEventBus();
                AppController appView = new AppController(eventBus);
                appView.go(RootPanel.get(elem.getId()));
            }
            
        }
      
    
  }
}
