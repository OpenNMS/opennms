package org.opennms.features.reporting.repository.remote;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opennms.features.reporting.dao.remoterepository.DefaultRemoteRepositoryConfigDao;
import org.opennms.features.reporting.dao.remoterepository.RemoteRepositoryConfigDao;
import org.opennms.features.reporting.model.Report;
import org.opennms.features.reporting.repository.ReportRepository;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

// TODO Tak: SSL Auth and Session-Handling(create/destroy)
public class DefaultRemoteRepository implements ReportRepository {

    private RemoteRepositoryConfigDao m_config = new DefaultRemoteRepositoryConfigDao();

    private ApacheHttpClient m_client;
    private ApacheHttpClientConfig m_clientConfig;
    private WebResource m_webResource;

    public DefaultRemoteRepository() {

        /**
         * DefaultApacheHttpClientConfig config = new
         * DefaultApacheHttpClientConfig();
         * config.getState().setCredentials(null, null, -1, "foo", "bar");
         * ApacheHttpClient c = ApacheHttpClient.create(config);
         * WebResource r = c.resource("http://host/base"); 
         * String s = r.get(String.class);
         * s = r.post(String.class, s);
         */

        m_clientConfig = new DefaultApacheHttpClientConfig();
        m_clientConfig.getState().setCredentials(null,
                                                 "localhost",
                                                 8080,
                                                 m_config.getLoginUser(),
                                                 m_config.getLoginRepoPassword());
        m_client = ApacheHttpClient.create(m_clientConfig);
    }

    @Override
    public List<Report> getReports() {
        ArrayList<Report> resultReports = new ArrayList<Report>();
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_config.getURI()
                    + "getReports");
            resultReports = m_webResource.get(new GenericType<ArrayList<Report>>() {
            });
        }
        return resultReports;
    }

    @Override
    public List<Report> getOnlineReports() {
        ArrayList<Report> resultReports = new ArrayList<Report>();
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_config.getURI()
                    + "getOnlineReports");
            resultReports = m_webResource.get(new GenericType<ArrayList<Report>>() {
            });
        }
        return resultReports;
    }

    @Override
    public String getReportService(String id) {
        String result = "";
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_config.getURI()
                    + "getReportService/" + id);
            result = m_webResource.get(String.class);
        }
        return result;
    }

    @Override
    public String getDisplayName(String id) {
        String result = "";
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_config.getURI()
                    + "getDisplayName/" + id);
            result = m_webResource.get(String.class);
        }
        return result;
    }

    @Override
    public String getEngine(String id) {
        String result = "";
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_config.getURI()
                    + "getEngine/" + id);
            result = m_webResource.get(String.class);
        }
        return result;
    }

    @Override
    public InputStream getTemplateStream(String id) {
        InputStream templateStreamResult = null;
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_config.getURI()
                    + "getTemplateStream/" + id);
            templateStreamResult = m_webResource.get(InputStream.class);
        }
        return templateStreamResult;
    }

    private Boolean isConfigOk() {
        if (m_config != null) {
            if (m_config.isRepositoryActive()) {
            } else {
                // TODO Tak: Logging RemoteRepository not activated
                return false;
            }
        } else {
            // TODO Tak: Logging no Config for RemoteRepository found
            return false;
        }
        return true;
    }

    public RemoteRepositoryConfigDao getConfig() {
        return m_config;
    }

    public void setConfig(RemoteRepositoryConfigDao config) {
        m_config = config;
    }
}
