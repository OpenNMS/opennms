package org.opennms.features.gwt.ksc.combobox.client;

import org.opennms.features.gwt.ksc.combobox.client.view.KscReportDetail;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class KSCResourceCombobox implements EntryPoint {

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
      NodeList<Element> nodes = RootPanel.getBodyElement().getElementsByTagName("opennms:kscReportCombobox");
      if(nodes.getLength() > 0) {
          for(int i = 0; i < nodes.getLength(); i++) {
              Element elem = nodes.getItem(i);
              AppController appView = new AppController(getKscComboboxData());
              appView.go(RootPanel.get(elem.getId()));
          }
      }
  }
  
  public static native JsArray<KscReportDetail> getKscComboboxData() /*-{
      return $wnd.kscComboData;
  }-*/;
  
}
