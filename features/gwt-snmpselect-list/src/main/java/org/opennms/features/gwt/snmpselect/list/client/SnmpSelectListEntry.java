/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.gwt.snmpselect.list.client;


import org.opennms.features.gwt.snmpselect.list.client.rest.DefaultSnmpInterfaceRestService;
import org.opennms.features.gwt.snmpselect.list.client.view.SnmpCellListItem;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.user.client.ui.RootPanel;

public class SnmpSelectListEntry implements EntryPoint {

    @Override
    public void onModuleLoad() {
        
        if(Navigator.getUserAgent().contains("MSIE")) {
            NodeList<Element> divs = RootPanel.getBodyElement().getElementsByTagName("div");
            for(int j = 0; j < divs.getLength(); j++) {
                Element element = divs.getItem(j);
                if(element.hasAttribute("name") && element.getAttribute("name").equals("opennms-snmpSelectList")) {
                    createView(element);
                }
            }
        }else {
        
            NodeList<Element> nodes = RootPanel.getBodyElement().getElementsByTagName("opennms:snmpSelectList");
            if(nodes.getLength() > 0) {
                for(int i = 0; i < nodes.getLength(); i++) {
                    Element elem = nodes.getItem(i);
                    createView(elem);
                }
                
            }
        }
    }

    private void createView(Element elem) {
        AppController appController = new AppController(new DefaultSnmpInterfaceRestService(getNodeId()));
        appController.go(RootPanel.get(elem.getId()));
    }
    
    private int getNodeId() {
        if(Location.getParameter("node") != null) {
            return Integer.valueOf(Location.getParameter("node"));
        }else {
            return -1;
        }
    }

    public static native JsArray<SnmpCellListItem> getTestDataList()/*-{
        return $wnd.testData.snmpInterface;
    }-*/;

}
