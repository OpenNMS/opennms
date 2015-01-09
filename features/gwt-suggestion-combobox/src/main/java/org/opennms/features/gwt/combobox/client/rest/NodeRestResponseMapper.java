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

package org.opennms.features.gwt.combobox.client.rest;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.gwt.combobox.client.view.NodeDetail;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public abstract class NodeRestResponseMapper {

    
    public static List<NodeDetail> mapNodeJSONtoNodeDetail(final String jsonString) {
        final List<NodeDetail> nodeDetails = new ArrayList<NodeDetail>();
        final JSONValue value = JSONParser.parseStrict(jsonString);
        final JSONObject obj = value.isObject();
        final JSONArray arr = value.isArray();
        JsArray<NodeDetail> jsDetails = null;

        if (obj != null) {
            jsDetails = createNodeDetailsArray(obj.getJavaScriptObject());
        } else if (arr != null) {
            jsDetails = createNodeDetailsArray(arr.getJavaScriptObject());
        } else {
            doLog(jsonString + " does not parse as an array or object!", value);
        }

        if (jsDetails != null) {
            for(int i = 0; i < jsDetails.length(); i++) {
                if(!jsDetails.get(i).getNodeType().equals("D")) {
                    nodeDetails.add(jsDetails.get(i));
                }
            }
        }

        return nodeDetails;
    }
    
    private static native JsArray<NodeDetail> createNodeDetailsArray(final JavaScriptObject jso) /*-{
        if (jso.node) {
            if( Object.prototype.toString.call( jso.node ) === '[object Array]' ) {
                return jso.node;
            } else {
                return [ jso.node ];
            }
        } else {
            if( Object.prototype.toString.call( jso ) === '[object Array]' ) {
                return jso;
            } else {
                return [ jso ];
            }
        }
    }-*/;

    public static native void doLog(final String message, final Object o) /*-{
        console.log(message,o);
    }-*/;
}
