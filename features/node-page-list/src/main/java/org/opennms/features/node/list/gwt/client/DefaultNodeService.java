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

package org.opennms.features.node.list.gwt.client;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.URL;

public class DefaultNodeService implements NodeService {
    
    private static String BASE_URL = "rest/nodes/";
    
    public static String SNMP_INTERFACES_TEST_RESPONSE = "{" +
    "\"@totalCount\" : \"3\"," + 
    "\"@count\" : \"3\"," +
    "\"snmpInterface\" : [ {" +
    " \"@poll\" : \"false\"," +
    " \"@pollFlag\" : \"N\"," +
    " \"@ifIndex\" : \"3\"," +
    " \"@id\" : \"240\"," +
    " \"@collect\" : \"false\"," +
    " \"@collectFlag\" : \"N\"," +
    " \"ifAdminStatus\" : \"2\"," +
    " \"ifAlias\" : \"\"," +
    " \"ifDescr\" : \"sit0\"," +
    " \"ifName\" : \"sit0\"," +
    " \"ifOperStatus\" : \"2\"," +
    " \"ifSpeed\" : \"0\"," +
    " \"ifType\" : \"131\"," +
    " \"ipAddress\" : \"0.0.0.0\"," +
    " \"lastCapsdPoll\" : \"2010-12-14T11:18:23.385-05:00\"," +
    " \"nodeId\" : \"11\"" +
    "}, {" +
    " \"@poll\" : \"false\"," +
    " \"@pollFlag\" : \"N\"," +
    " \"@ifIndex\" : \"1\"," +
    " \"@id\" : \"242\"," +
    " \"@collect\" : \"false\"," +
    " \"@collectFlag\" : \"N\"," +
    " \"ifAdminStatus\" : \"1\"," +
    " \"ifAlias\" : \"\"," +
    " \"ifDescr\" : \"lo\"," +
    " \"ifName\" : \"lo\"," +
    " \"ifOperStatus\" : \"1\"," +
    " \"ifSpeed\" : \"10000000\"," +
    " \"ifType\" : \"24\"," +
    " \"ipAddress\" : \"0.0.0.0\"," +
    " \"lastCapsdPoll\" : \"2010-12-14T11:18:23.385-05:00\"," +
    " \"nodeId\" : \"11\"" +
    "}, {" +
    " \"@poll\" : \"false\"," +
    " \"@pollFlag\" : \"N\"," +
    " \"@ifIndex\" : \"2\"," +
    " \"@id\" : \"238\"," +
    " \"@collect\" : \"true\"," +
    " \"@collectFlag\" : \"C\"," +
    " \"ifAdminStatus\" : \"1\"," +
    " \"ifAlias\" : \"\"," +
    " \"ifDescr\" : \"eth0\"," +
    " \"ifName\" : \"eth0\"," +
    " \"ifOperStatus\" : \"1\"," +
    " \"ifSpeed\" : \"10000000\"," +
    " \"ifType\" : \"6\"," +
    " \"ipAddress\" : \"172.20.1.11\"," +
    " \"ipInterfaces\" : \"130\"," +
    " \"lastCapsdPoll\" : \"2010-12-14T11:18:23.385-05:00\"," +
    " \"netMask\" : \"255.255.255.0\"," +
    " \"nodeId\" : \"11\"," +
    " \"physAddr\" : \"00163e13f215\"" +
    " } ]" +
    "}";
    
    public static String IP_INTERFACES_TEST_RESPONSE = "{" +
    		"\"@totalCount\" : \"23\"," +
    		"\"@count\" : \"23\"," +
    		"\"ipInterface\" : [ {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"0\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"42\"," +
    		  "\"@isDown\" : \"true\"," +
    		  "\"ipAddress\" : \"128.167.119.25\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"2\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"30\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"161.221.89.118\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}]" +
    		"}";
    
    @Override
    public void getAllIpInterfacesForNode(int nodeId, RequestCallback callback) {
        String url = BASE_URL + nodeId + "/ipinterfaces?limit=0";
        sendRequest(callback, url);
    }

    
    
    @Override
    public void getAllSnmpInterfacesForNode(int nodeId, RequestCallback callback) {
        String url = BASE_URL + nodeId + "/snmpinterfaces?limit=0";
        sendRequest(callback, url);
    }

    @Override
    public void findIpInterfacesMatching(int nodeId, String parameter, String value, RequestCallback callback) {
        String url = BASE_URL + nodeId + "/ipinterfaces?" + parameter + "=" + value + "&comparator=contains&limit=0";
        sendRequest(callback, url);
        
    }

    @Override
    public void findSnmpInterfacesMatching(int nodeId, String parameter, String value, RequestCallback callback) {
        String url = BASE_URL + nodeId + "/snmpinterfaces?" + parameter + "=" + value;
        if(!parameter.equals("ifIndex") && !parameter.equals("ifSpeed")) {
            url += "&comparator=contains";
        }
        url += "&limit=0";
        sendRequest(callback, url);
        
    }
    
    private void sendRequest(RequestCallback callback, String url) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        builder.setHeader("accept", "application/json");
        try {
            builder.sendRequest(null, callback);
        } catch (RequestException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
