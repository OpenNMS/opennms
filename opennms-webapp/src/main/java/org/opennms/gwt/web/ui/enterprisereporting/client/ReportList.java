package org.opennms.gwt.web.ui.enterprisereporting.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

public class ReportList extends Composite {

    private static ReportListUiBinder uiBinder = GWT
            .create(ReportListUiBinder.class);

    interface ReportListUiBinder extends UiBinder<Widget, ReportList> {}
    
    @UiField
    public ReportListGrid reportListTable;
    
    public ReportList() {
        initWidget(uiBinder.createAndBindUi(this));
        populateReportList();
    }

    private void populateReportList() {
        String url = getUrl();
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        try {
            Request request = builder.sendRequest(null, new RequestCallback() {

                public void onResponseReceived(Request request, Response response) {
                    if(200 == response.getStatusCode()) {
                        Document reportDom = XMLParser.parse(response.getText());
                        int totalReports = reportDom.getElementsByTagName("report").getLength();
                        
                        
                        reportListTable.resize(totalReports,5);
                        setupHeaders();
                        
                        for(int i = 0; i < totalReports; i++) {
                            String name = reportDom.getElementsByTagName("reportName").item(i).getFirstChild().getNodeValue();
                            String template = reportDom.getElementsByTagName("reportTemplate").item(i).getFirstChild().getNodeValue();
                            String schedule = reportDom.getElementsByTagName("cronSchedule").item(i).getFirstChild().getNodeValue();
                            String format = reportDom.getElementsByTagName("reportFormat").item(i).getFirstChild().getNodeValue();
                            String engine = reportDom.getElementsByTagName("reportEngine").item(i).getFirstChild().getNodeValue();
                            
                            reportListTable.setText(i, 0, name);
                            reportListTable.setText(i, 1, template);
                            reportListTable.setText(i, 2, schedule);
                            reportListTable.setText(i, 3, format);
                            reportListTable.setText(i, 4, engine);
                        }
                        
                    }else {
                        Window.alert("Got a response but the status code was wrong\ncode: " + response.getStatusCode() + "\ntext: " + response.getStatusText());
                    }
                }

                public void onError(Request request, Throwable exception) {
                    Window.alert("An Error Occurred on getting the report list \nerror: " + exception.getMessage());
                }
           });
        }catch(RequestException e) {
            
        }
    }

    private String getUrl() {
        String completeURL = Location.getHref();
        String[] urlSplit = completeURL.split("/");
        String openNMSPath = urlSplit[3];
        return "http://" + Location.getHost() + "/" + openNMSPath + "/rest/reports";
    }

    private void setupHeaders() {
        reportListTable.addHeader(0, "name");
        reportListTable.addHeader(1, "template");
        reportListTable.addHeader(2, "cron schedule");
        reportListTable.addHeader(3, "format");
        reportListTable.addHeader(4, "engine");
    }

}
