package org.opennms.features.gwt.combobox.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class NodeSuggestionCombobox implements EntryPoint {
  

  /**
   * This is the entry point method.
   */
    public void onModuleLoad() {
        
        if(Window.Navigator.getUserAgent().contains("MSIE")) {
            NodeList<Element> divs = RootPanel.getBodyElement().getElementsByTagName("div");
            for(int j = 0; j < divs.getLength(); j++) {
                Element element = divs.getItem(j);
                if(element.hasAttribute("name") && element.getAttribute("name").contains("opennms-nodeSuggestionCombobox")) {
                    createView(element);
                }
            }
            
        }else {
            NodeList<Element> nodes = RootPanel.getBodyElement().getElementsByTagName("opennms:nodeSuggestionCombobox");
            if(nodes.getLength() > 0) {
                for(int i = 0; i < nodes.getLength(); i++) {
                    createView(nodes.getItem(i));
                }
                
            }
        }
      
    
    }

    private void createView(Element elem) {
        SimpleEventBus eventBus = new SimpleEventBus();
        AppController appView = new AppController(eventBus);
        appView.go(RootPanel.get(elem.getId()));
    }
    
}
