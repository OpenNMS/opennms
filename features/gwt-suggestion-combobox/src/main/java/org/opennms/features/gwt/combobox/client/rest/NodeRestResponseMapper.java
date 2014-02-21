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

package org.opennms.features.gwt.combobox.client.rest;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.gwt.combobox.client.view.NodeDetail;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;

public class NodeRestResponseMapper {

    
    public static List<NodeDetail> mapNodeJSONtoNodeDetail(final String jsonString) {
        final List<NodeDetail> nodeDetails = new ArrayList<NodeDetail>();
        final JSONArray jArray = JSONParser.parseStrict(jsonString).isArray();

        if (jArray != null) {
            final JsArray<NodeDetail> jsDetails = createNodeDetailsArray(jArray.getJavaScriptObject());
            for(int i = 0; i < jsDetails.length(); i++) {
                if(!jsDetails.get(i).getNodeType().equals("D")) {
                    nodeDetails.add(jsDetails.get(i));
                }
            }
        }
        
        return nodeDetails;
    }
    
    private static native JsArray<NodeDetail> createNodeDetailsArray(final JavaScriptObject jso) /*-{
        return jso;
    }-*/;
}
