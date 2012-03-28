package org.opennms.features.gwt.ksc.add.client.rest;

import com.google.gwt.http.client.RequestCallback;

public interface KscReportService {
    public void getAllReports(RequestCallback callback);
    public void addGraphToReport(RequestCallback callback, int kscReportId, String graphTitle, String graphName, String resourceId, String timeSpan);
}
