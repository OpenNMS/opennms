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

package org.opennms.features.node.list.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public abstract class NodeRestResponseMapper {
    public static List<IpInterface> createIpInterfaceData(String jsonString){
        final List<IpInterface> ipIfaceList = new ArrayList<IpInterface>();
        final JSONValue value = JSONParser.parseStrict(jsonString);

        JSONObject obj = value.isObject();
        JSONArray arr = value.isArray();
        JsArray<IpInterface> ipIfaces = null;
        if (obj != null) {
            ipIfaces = createIpInterfaceData(obj.getJavaScriptObject());
        } else if (arr != null) {
            ipIfaces = createIpInterfaceData(arr.getJavaScriptObject());
        } else {
            doLog(jsonString + " did not parse as an object or array!", value);
        }

        if (ipIfaces != null) {
            for(int i = 0; i < ipIfaces.length(); i++) {
                ipIfaceList.add(ipIfaces.get(i));
            }
        }
        doLog("ip interfaces:", ipIfaceList);
        return ipIfaceList;
    }
    
    public static native JsArray<IpInterface> createIpInterfaceData(final JavaScriptObject jso) /*-{
        if (jso.ipInterface) {
            if( Object.prototype.toString.call( jso.ipInterface ) === '[object Array]' ) {
                return jso.ipInterface;
            } else {
                return [ jso.ipInterface ];
            }
        } else {
            if( Object.prototype.toString.call( jso ) === '[object Array]' ) {
                return jso;
            } else {
                return [ jso ];
            }
        }
    }-*/;

    public static List<PhysicalInterface> createSnmpInterfaceData(String jsonString){
        final List<PhysicalInterface> physIfaceList = new ArrayList<PhysicalInterface>();
        final JSONValue value = JSONParser.parseStrict(jsonString);
        final JSONObject obj = value.isObject();
        final JSONArray arr = value.isArray();
        JsArray<PhysicalInterface> snmpIfaces = null;
        if (obj != null) {
            snmpIfaces = createSnmpInterfaceData(obj.getJavaScriptObject());
        } else if (arr != null) {
            snmpIfaces = createSnmpInterfaceData(arr.getJavaScriptObject());
        } else {
            doLog(jsonString + " did not parse as an object or array!", value);
        }

        if (snmpIfaces != null) {
            for(int i = 0; i < snmpIfaces.length(); i++) {
                physIfaceList.add(snmpIfaces.get(i));
            }
        }

        doLog("physical interfaces:", physIfaceList);
        return physIfaceList;
    }
    
    public static native JsArray<PhysicalInterface> createSnmpInterfaceData(final JavaScriptObject jso) /*-{
        if (jso.snmpInterface) {
            if( Object.prototype.toString.call( jso.snmpInterface ) === '[object Array]' ) {
                return jso.snmpInterface;
            } else {
                return [ jso.snmpInterface ];
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
