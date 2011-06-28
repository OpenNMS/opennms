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
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"1\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"31\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"17.172.224.47\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"2\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"35\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"199.239.136.200\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"3\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"37\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"199.59.149.198\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"1\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"33\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"207.46.197.32\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"1\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"32\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"207.46.245.32\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"2\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"29\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"208.87.33.150\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"0\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"44\"," +
    		  "\"@isDown\" : \"true\"," +
    		  "\"ipAddress\" : \"212.58.254.252\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"0\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"43\"," +
    		  "\"@isDown\" : \"true\"," +
    		  "\"ipAddress\" : \"213.174.202.56\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"0\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"57\"," +
    		  "\"@isDown\" : \"true\"," +
    		  "\"ipAddress\" : \"63.236.5.136\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"7\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"28\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"64.13.232.129\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"0\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"58\"," +
    		  "\"@isDown\" : \"true\"," +
    		  "\"ipAddress\" : \"64.202.189.170\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"0\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"45\"," +
    		  "\"@isDown\" : \"true\"," +
    		  "\"ipAddress\" : \"65.55.175.254\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"3\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"34\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"66.150.160.122\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"0\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"47\"," +
    		  "\"@isDown\" : \"true\"," +
    		  "\"ipAddress\" : \"66.54.56.30\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"3\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"36\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"69.147.125.65\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"3\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"39\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"69.63.189.11\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"3\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"40\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"74.125.115.99\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"3\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"38\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"74.125.67.106\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"3\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"27\"," +
    		  "\"@isDown\" : \"false\"," +
    		  "\"ipAddress\" : \"74.125.67.18\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"0\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"46\"," +
    		  "\"@isDown\" : \"true\"," +
    		  "\"ipAddress\" : \"78.109.84.196\"," +
    		  "\"nodeId\" : \"2\"" +
    		"}, {" +
    		  "\"@snmpPrimary\" : \"S\"," +
    		  "\"@monitoredServiceCount\" : \"0\"," +
    		  "\"@isManaged\" : \"M\"," +
    		  "\"@id\" : \"56\"," +
    		  "\"@isDown\" : \"true\"," +
    		  "\"ipAddress\" : \"98.129.229.144\"," +
    		  "\"nodeId\" : \"2\"" +
    		"} ]" +
    		"}";
    
    public void getAllIpInterfacesForNode(int nodeId, RequestCallback callback) {
        String url = BASE_URL + nodeId + "/ipinterfaces";
        sendRequest(callback, url);
    }

    
    
    public void getAllSnmpInterfacesForNode(int nodeId, RequestCallback callback) {
        String url = BASE_URL + nodeId + "/snmpinterfaces";
        sendRequest(callback, url);
    }

    public void findIpInterfacesMatching(int nodeId, String parameter, String value, RequestCallback callback) {
        String url = BASE_URL + nodeId + "/ipinterfaces?" + parameter + "=" + value + "&comparator=contains";
        sendRequest(callback, url);
        
    }

    public void findSnmpInterfacesMatching(int nodeId, String parameter, String value, RequestCallback callback) {
        String url = BASE_URL + nodeId + "/snmpinterfaces?" + parameter + "=" + value;
        if(!parameter.equals("ifIndex") && !parameter.equals("ifSpeed")) {
            url += "&comparator=contains";
        }
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
