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
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class NodeRestResponseMapper {

    
    public static List<NodeDetail> mapNodeJSONtoNodeDetail(String jsonString){
        List<NodeDetail> nodeDetails = new ArrayList<NodeDetail>();
        JSONObject jsonObject = JSONParser.parseStrict(jsonString).isObject();
        
        if(jsonObject != null && jsonObject.containsKey("node")) {
            if(jsonObject.get("node").isObject() != null) {
                JSONObject jso = jsonObject.get("node").isObject();
                nodeDetails.add(createNodeDetailsOverlay(jso.getJavaScriptObject()));
                
            }else if(jsonObject.get("node").isArray() != null) {
                JSONArray jArray = jsonObject.get("node").isArray();
                JsArray<NodeDetail> nodedetails = createNodeDetailsArray(jArray.getJavaScriptObject());
                for(int i = 0; i < nodedetails.length(); i++) {
                    if(!nodedetails.get(i).getNodeType().equals("D")) {
                        nodeDetails.add(nodedetails.get(i));
                    }
                }
            }
        }
        
        return nodeDetails;
    }
    
    private static native NodeDetail createNodeDetailsOverlay(JavaScriptObject jso) /*-{
        return jso;
    }-*/;
    
    private static native JsArray<NodeDetail> createNodeDetailsArray(JavaScriptObject jso) /*-{
        return jso;
    }-*/;
}
