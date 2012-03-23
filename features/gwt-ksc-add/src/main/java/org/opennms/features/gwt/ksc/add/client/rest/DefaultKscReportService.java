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
