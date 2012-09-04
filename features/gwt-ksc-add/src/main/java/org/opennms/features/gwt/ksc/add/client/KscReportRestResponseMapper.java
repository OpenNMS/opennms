/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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
