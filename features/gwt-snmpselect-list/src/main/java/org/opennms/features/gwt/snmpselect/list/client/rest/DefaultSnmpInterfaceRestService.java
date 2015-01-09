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

package org.opennms.features.gwt.snmpselect.list.client.rest;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.gwt.snmpselect.list.client.view.SnmpCellListItem;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class DefaultSnmpInterfaceRestService implements SnmpInterfaceRestService {
    
    private static String DEFAULT_RESPONSE = "[ {" +
      "\"poll\" : \"false\"," +
      "\"pollFlag\" : \"N\"," +
      "\"ifIndex\" : \"2\"," +
      "\"id\" : \"139\"," +
      "\"collect\" : \"true\"," +
      "\"collectFlag\" : \"C\"," +
      "\"ifAdminStatus\" : \"1\"," +
      "\"ifAlias\" : \"\"," +
      "\"ifDescr\" : \"eth0\"," +
      "\"ifName\" : \"eth0\"," +
      "\"ifOperStatus\" : \"1\"," +
      "\"ifSpeed\" : \"10000000\"," +
      "\"ifType\" : \"6\"," +
      "\"ipInterfaces\" : \"138\"," +
      "\"netMask\" : \"255.255.255.0\"," +
      "\"nodeId\" : \"10\"," +
      "\"physAddr\" : \"00163e13f215\"" +
    "}, {" +
      "\"poll\" : \"false\"," +
      "\"pollFlag\" : \"N\"," +
      "\"ifIndex\" : \"3\"," +
      "\"id\" : \"140\"," +
      "\"collect\" : \"true\"," +
      "\"collectFlag\" : \"UC\"," +
      "\"ifAdminStatus\" : \"2\"," +
      "\"ifAlias\" : \"\"," +
      "\"ifDescr\" : \"sit0\"," +
      "\"ifName\" : \"sit0\"," +
      "\"ifOperStatus\" : \"2\"," +
      "\"ifSpeed\" : \"0\"," +
      "\"ifType\" : \"131\"," +
      "\"nodeId\" : \"10\"" +
    "} ]";
    
    private SnmpInterfaceRequestHandler m_requestHandler;
    private int m_nodeId;
    
    public DefaultSnmpInterfaceRestService(int nodeId) {
        m_nodeId = nodeId;
    }
    
    @Override
    public void getInterfaceList() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode("rest/nodes/" + m_nodeId + "/snmpinterfaces?limit=0"));
        builder.setHeader("accept", "application/json");
        
        try {
            builder.sendRequest(null, new RequestCallback() {

                @Override
                public void onResponseReceived(Request request, Response response) {
                    if(response.getStatusCode() == 200) {
                        m_requestHandler.onResponse(parseJSONData(response.getText()));
                    }else {
                        m_requestHandler.onError("An Error Occurred retreiving the SNMP Interfaces for this node.\n" +
                        		"Status Code: " + response.getStatusCode());
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    m_requestHandler.onError(exception.getMessage());
                    
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
        
    }

    protected List<SnmpCellListItem> parseJSONData(final String jsonString) {
        final List<SnmpCellListItem> cellList = new ArrayList<SnmpCellListItem>();
        final JSONValue value = JSONParser.parseStrict(jsonString);
        final JSONObject obj = value.isObject();
        final JSONArray arr = value.isArray();
        JsArray<SnmpCellListItem> jsArray = null;

        if (obj != null) {
            jsArray = createJsArray(obj.getJavaScriptObject());
        } else if (arr != null) {
            jsArray = createJsArray(arr.getJavaScriptObject());
        } else {
            doLog(jsonString + " does not parse as an array or object!", value);
        }

        if (jsArray != null) {
            for(int i = 0; i < jsArray.length(); i++) {
                cellList.add(jsArray.get(i));
            }
        }

        return cellList;
    }

    private static native JsArray<SnmpCellListItem> createJsArray(final JavaScriptObject jso) /*-{
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

    @Override
    public void updateCollection( int ifIndex, String collectFlag ) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT, URL.encode("rest/nodes/" + m_nodeId + "/snmpinterfaces/" + ifIndex));
        builder.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        try {
            builder.sendRequest("collect=" + collectFlag, new RequestCallback() {

                @Override
                public void onResponseReceived(Request request, Response response) {
                    
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    m_requestHandler.onError("There was an error when saving the interface collection value");
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSnmpInterfaceRequestHandler(SnmpInterfaceRequestHandler handler) {
        m_requestHandler = handler;
    }
 
    public static native void doLog(final String message, final Object o) /*-{
        console.log(message,o);
    }-*/;
}