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
                    nodeDetails.add(nodedetails.get(i));
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
