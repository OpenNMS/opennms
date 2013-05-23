/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
  @Override
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
