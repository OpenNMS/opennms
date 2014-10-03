/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.gwt.ksc.add.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public abstract class KscReportRestResponseMapper {

    public static List<KscReport> translate(final Response response) {
        final String jsonText = response.getText();
        return translate(jsonText);
    }

    public static List<KscReport> translate(final String jsonText) {
        final List<KscReport> reports = new ArrayList<KscReport>();
        final JSONValue value = JSONParser.parseStrict(jsonText);
        final JSONArray arr = value.isArray();
        final JSONObject obj = value.isObject();
        JsArray<KscReport> jsReports = null;

        if (obj != null) {
            jsReports = translateJsonReportList(obj.getJavaScriptObject());
        } else if (arr != null) {
            jsReports = translateJsonReportList(arr.getJavaScriptObject());
        } else {
            doLog(jsonText + " did not parse as an object or array!", value);
        }

        if (jsReports != null) {
            for(int i = 0; i < jsReports.length(); i++) {
                reports.add(jsReports.get(i));
            }
        }

        doLog("KSC reports:",reports);
        return reports;
    }

    private static native JsArray<KscReport> translateJsonReportList(final JavaScriptObject jso) /*-{
        if (jso.kscReport) {
            if( Object.prototype.toString.call( jso.kscReport ) === '[object Array]' ) {
                return jso.kscReport;
            } else {
                return [ jso.kscReport ];
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
