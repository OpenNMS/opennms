package org.opennms.features.gwt.ksc.add.client;

import java.util.ArrayList;
import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

public class KscReportRestResponseMapper {

    public static List<KscReport> translate(final Response response) {
        final String jsonText = response.getText();
        return translate(jsonText);
    }

    public static List<KscReport> translate(final String jsonText) {
        final List<KscReport> reports = new ArrayList<KscReport>();
        final JSONObject jsonObject = JSONParser.parseStrict(jsonText).isObject();

        if(jsonObject != null && jsonObject.containsKey("kscReport")) {
            if(jsonObject.get("kscReport").isArray() != null) {
                final JSONArray jArray = jsonObject.get("kscReport").isArray();
                final JsArray<KscReport> jsReports = translateJsonReportList(jArray.getJavaScriptObject());
                for(int i = 0; i < jsReports.length(); i++) {
                    reports.add(jsReports.get(i));
                }
            } else if (jsonObject.get("kscReport").isObject() != null) {
                final KscReport report = translateJsonReport(jsonObject.get("kscReport").isObject().getJavaScriptObject());
                reports.add(report);
            } else {
                GWT.log("invalid object response: " + jsonObject);
            }
        } else {
            GWT.log("invalid object response: " + jsonObject);
        }

        return reports;
    }

    private static native KscReport translateJsonReport(final JavaScriptObject jso) /*-{
        return jso;
    }-*/;

    private static native JsArray<KscReport> translateJsonReportList(final JavaScriptObject jso) /*-{
        return jso;
    }-*/;

}
