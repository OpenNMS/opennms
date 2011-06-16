package org.opennms.features.node.list.gwt.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class NodeRestResponseMapper {

    
    public static List<IpInterface> createIpInterfaceData(String jsonString){
        List<IpInterface> ipIfaceList = new ArrayList<IpInterface>();
        JSONValue jsonVal = JSONParser.parseStrict(jsonString);
        
        
        if(jsonVal.isObject() != null && jsonVal.isObject().containsKey("ipInterface")) {
            JSONObject ipInterface = jsonVal.isObject().get("ipInterface").isObject();
            ipIfaceList.add(createIpInterfaceOverlay(ipInterface.getJavaScriptObject()));
        }else if(jsonVal.isArray() != null){
             JsArray<IpInterface> ipFaces = createIpInterfaceData(jsonVal.isArray().getJavaScriptObject());
             for(int i = 0; i < ipFaces.length(); i++) {
                 ipIfaceList.add(ipFaces.get(i));
             }
        }
        
        return ipIfaceList;
    }
    
    public static native IpInterface createIpInterfaceOverlay(JavaScriptObject jso)/*-{
        return jso;
    }-*/;
    
    public static native JsArray<IpInterface> createIpInterfaceData(JavaScriptObject jso) /*-{
        return jso.ipInterface;
    }-*/;
    
    public static JsArray<PhysicalInterface> createSnmpInterfaceData(String jsonString){
        JSONValue value = JSONParser.parseLenient(jsonString);
        return createSnmpInterfaceData(value.isObject().getJavaScriptObject());
    }

    public static native JsArray<PhysicalInterface> createSnmpInterfaceData(JavaScriptObject jso) /*-{
        return jso.snmpInterface;
    }-*/;
    
    
}
