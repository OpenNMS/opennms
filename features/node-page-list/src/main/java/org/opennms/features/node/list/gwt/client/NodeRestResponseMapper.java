package org.opennms.features.node.list.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class NodeRestResponseMapper {

    /**
     * 
     * @param jsonString
     * @return
     */
    public static List<IpInterface> createIpInterfaceData(String jsonString){
        List<IpInterface> ipIfaceList = new ArrayList<IpInterface>();
        JSONObject jsonObject = JSONParser.parseStrict(jsonString).isObject();
        
        if(jsonObject != null && jsonObject.containsKey("ipInterface")) {
            
            if(jsonObject.get("ipInterface").isObject() != null) {
                JSONObject jObj = jsonObject.get("ipInterface").isObject();
                ipIfaceList.add(createIpInterfaceOverlay(jObj.getJavaScriptObject()));
                
            }else if(jsonObject.get("ipInterface").isArray() != null) {
                JSONArray jArray = jsonObject.get("ipInterface").isArray();
                JsArray<IpInterface> ipFaces = createIpInterfaceData(jArray.getJavaScriptObject());
                for(int i = 0; i < ipFaces.length(); i++) {
                    ipIfaceList.add(ipFaces.get(i));
                }
            }
        }
        
        return ipIfaceList;
    }
    
    public static native IpInterface createIpInterfaceOverlay(JavaScriptObject jso)/*-{
        return jso;
    }-*/;
    
    public static native JsArray<IpInterface> createIpInterfaceData(JavaScriptObject jso) /*-{
        return jso;
    }-*/;
    
    /**
     * 
     * @param jsonString
     * @return
     */
    public static List<PhysicalInterface> createSnmpInterfaceData(String jsonString){
        List<PhysicalInterface> physIfaceList = new ArrayList<PhysicalInterface>();
        JSONObject jsonObject = JSONParser.parseStrict(jsonString).isObject();
        
        if(jsonObject != null && jsonObject.containsKey("snmpInterface")) {
            
            if(jsonObject.get("snmpInterface").isObject() != null) {
                JSONObject jObj = jsonObject.get("snmpInterface").isObject();
                physIfaceList.add(createSnmpInterfaceOverlay(jObj.getJavaScriptObject()));
                
            }else if(jsonObject.get("snmpInterface").isArray() != null) {
                JSONArray jArray = jsonObject.get("snmpInterface").isArray();
                JsArray<PhysicalInterface> ipFaces = createSnmpInterfaceData(jArray.getJavaScriptObject());
                for(int i = 0; i < ipFaces.length(); i++) {
                    physIfaceList.add(ipFaces.get(i));
                }
            }
        }
        
        return physIfaceList;
    }
    
    public static native PhysicalInterface createSnmpInterfaceOverlay(JavaScriptObject jso) /*-{
        return jso;
    }-*/;
    
    public static native JsArray<PhysicalInterface> createSnmpInterfaceData(JavaScriptObject jso) /*-{
        return jso;
    }-*/;
    
    
}
