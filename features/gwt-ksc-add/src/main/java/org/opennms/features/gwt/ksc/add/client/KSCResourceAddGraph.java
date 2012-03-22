package org.opennms.features.gwt.ksc.add.client;

import java.util.List;

import org.opennms.features.gwt.ksc.add.client.rest.DefaultKscReportService;
import org.opennms.features.gwt.ksc.add.client.rest.KscReportService;
import org.opennms.features.gwt.ksc.add.client.view.KscReport;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public class KSCResourceAddGraph implements EntryPoint {
    final static public boolean DEBUG = true;
    final static private String m_debugResponse =
        "{" +
            "\"@totalCount\":\"2\"," +
            "\"@count\":\"2\"," +
            "\"kscReport\":[" +
                "{\"@label\":\"Test\",\"@id\":\"0\"}," +
                "{\"@label\":\"Test 2\",\"@id\":\"1\"}" +
            "]" +
        "}";

    public void onModuleLoad() {
        final KscReportService service = new DefaultKscReportService();
        final NodeList<Element> nodes = RootPanel.getBodyElement().getElementsByTagName("opennms-addKscReport");

        if (nodes.getLength() > 0) {
            service.getAllReports(new RequestCallback() {
                @Override
                public void onResponseReceived(final Request request, final Response response) {
                    final String responseText;
                    if (response.getStatusCode() == 200) {
                        responseText = response.getText();
                    } else {
                        if (DEBUG) {
                            responseText = m_debugResponse;
                        } else {
                            Window.alert("Error occurred retrieving list of KSC reports (response was " + response.getStatusCode() + ".");
                            responseText = null;
                        }
                    }

                    if (responseText != null) {
                        handleResponseText(nodes, responseText);
                    }
                }

                @Override
                public void onError(final Request request, final Throwable exception) {
                    if (DEBUG) {
                        handleResponseText(nodes, m_debugResponse);
                    } else {
                        Window.alert("Error occurred retrieving list of KSC reports: " + exception.getLocalizedMessage());
                    }
                }
                
                public void handleResponseText(final NodeList<Element> nodes, final String responseText) {
                    final List<KscReport> reports = KscReportRestResponseMapper.translate(responseText);
                    for (int i = 0; i < nodes.getLength(); i++) {
                        createView(nodes.getItem(i), reports);
                    }
                }
            });
        }

    }

    private void createView(final Element elem, final List<KscReport> kscReportList) {
        final String report = elem.getAttribute("report");
        final String resourceId = elem.getAttribute("resourceId");
        if (report == null) {
            GWT.log("<opennms-addKscReport> tag found without a required report tag!");
            return;
        }
        if (resourceId == null) {
            GWT.log("<opennms-addKscReport> tag found without a required resourceId tag!");
            return;
        }

        GWT.log("creating view for report=" + report + ", resourceId=" + resourceId);
        new AppController(kscReportList, report, resourceId).go(RootPanel.get(elem.getId()));
    }
}
