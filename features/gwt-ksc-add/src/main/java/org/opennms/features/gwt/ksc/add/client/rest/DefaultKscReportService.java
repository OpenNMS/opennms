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

package org.opennms.features.gwt.ksc.add.client.rest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.UrlBuilder;

public class DefaultKscReportService implements KscReportService {

    private static String BASE_URL = "rest/ksc";

    @Override
    public void getAllReports(final RequestCallback callback) {
        sendRequest(callback, RequestBuilder.GET, BASE_URL);
    }

    @Override
    public void addGraphToReport(final RequestCallback callback, final int kscReportId, final String graphTitle, final String graphName, final String resourceId, final String timeSpan) {
        UrlBuilder builder = new UrlBuilder();
        builder.setPath(BASE_URL + "/" + kscReportId);
        builder.setParameter("title", graphTitle);
        builder.setParameter("reportName", graphName);
        builder.setParameter("resourceId", resourceId);
        builder.setParameter("timespan", timeSpan);

        // we just want a relative URL, so we render it and strip the beginning :)
        final String url = builder.buildString().replace("http:///", "");
        GWT.log("making request: " + url);
        sendRequest(callback, RequestBuilder.PUT, url);
    }

    private void sendRequest(final RequestCallback callback, final Method method, final String url) {
        final RequestBuilder builder = new RequestBuilder(method, url);
        builder.setHeader("accept", "application/json");
        try {
            builder.sendRequest(null, callback);
        } catch (final RequestException e) {
            e.printStackTrace();
        }
    }

}
