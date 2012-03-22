package org.opennms.features.gwt.ksc.add.client.rest;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.URL;

public class DefaultKscReportService implements KscReportService {

    private static String BASE_URL = "rest/ksc";
    @Override
    public void getAllReports(final RequestCallback callback) {
        sendRequest(callback, BASE_URL);
    }

    private void sendRequest(final RequestCallback callback, final String url) {
        final RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        builder.setHeader("accept", "application/json");
        try {
            builder.sendRequest(null, callback);
        } catch (final RequestException e) {
            e.printStackTrace();
        }
    }

}
