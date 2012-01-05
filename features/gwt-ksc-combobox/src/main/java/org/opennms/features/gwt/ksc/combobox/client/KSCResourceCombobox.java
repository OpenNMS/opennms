package org.opennms.features.gwt.ksc.combobox.client;

import org.opennms.features.gwt.ksc.combobox.client.view.KscReportDetail;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class KSCResourceCombobox implements EntryPoint {

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
      
      
      if(Window.Navigator.getUserAgent().contains("MSIE")) {
          NodeList<Element> divs = RootPanel.getBodyElement().getElementsByTagName("div");
          for(int j = 0; j < divs.getLength(); j++) {
              Element element = divs.getItem(j);
              if(element.hasAttribute("name") && element.getAttribute("name").contains("opennms-kscReportCombobox")) {
                  createView(element);
              }
          }
          
      }else {
          NodeList<Element> nodes = RootPanel.getBodyElement().getElementsByTagName("opennms:kscReportCombobox");
          if(nodes.getLength() > 0) {
              for(int i = 0; i < nodes.getLength(); i++) {
                  createView(nodes.getItem(i));
              }
          }
      }
  }

  private void createView(Element elem) {
      AppController appView = new AppController(getKscComboboxData());
      appView.go(RootPanel.get(elem.getId()));
  }
  
  public static native JsArray<KscReportDetail> getKscComboboxData() /*-{
      return $wnd.kscComboData;
  }-*/;
  
}
