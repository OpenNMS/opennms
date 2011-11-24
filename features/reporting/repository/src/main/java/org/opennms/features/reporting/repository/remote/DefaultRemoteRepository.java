package org.opennms.features.reporting.repository.remote;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.opennms.features.reporting.model.Report;
import org.opennms.features.reporting.repository.ReportRepository;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

//TODO Tak: SSL Auth and Session-Handling(create/destroy)
public class DefaultRemoteRepository implements ReportRepository {

    private String SERVER_URI = "http://192.168.30.176:8080/opennms/connect/rest/repo/";
    private Client m_client = Client.create();
    private WebResource m_webResource;

    @Override
    public List<Report> getReports() {
        m_webResource = m_client.resource(SERVER_URI + "getReports");
        return m_webResource.get(new GenericType<ArrayList<Report>>(){});
    }

    @Override
    public List<Report> getOnlineReports() {
        m_webResource = m_client.resource(SERVER_URI + "getOnlineReports");
        return m_webResource.get(new GenericType<ArrayList<Report>>(){});
    }

    @Override
    public String getReportService(String id) {
        m_webResource = m_client.resource(SERVER_URI + "getReportService/" + id);
        return m_webResource.get(String.class);
    }

    @Override
    public String getDisplayName(String id) {
        m_webResource = m_client.resource(SERVER_URI + "getDisplayName/" + id);
        return m_webResource.get(String.class);
    }

    @Override
    public String getEngine(String id) {
        m_webResource = m_client.resource(SERVER_URI + "getEngine/" + id);
        return m_webResource.get(String.class);
    }

    @Override
    public InputStream getTemplateStream(String id) {
        m_webResource = m_client.resource(SERVER_URI + "getTemplateStream/" + id);
        return m_webResource.get(InputStream.class);
    }

}
