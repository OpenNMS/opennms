package org.opennms.features.reporting.repository.remote;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opennms.features.reporting.dao.remoterepository.DefaultRemoteRepositoryConfigDao;
import org.opennms.features.reporting.dao.remoterepository.RemoteRepositoryConfigDao;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.jasperreport.SimpleJasperReportsDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

// TODO Tak: SSL Auth and Session-Handling(create/destroy)
public class DefaultRemoteRepository implements ReportRepository {

    private RemoteRepositoryConfigDao m_config = new DefaultRemoteRepositoryConfigDao();
    private Logger logger = LoggerFactory.getLogger(DefaultRemoteRepository.class.getSimpleName());

    private ApacheHttpClient m_client;
    private ApacheHttpClientConfig m_clientConfig;
    private WebResource m_webResource;

    public DefaultRemoteRepository() {

        m_clientConfig = new DefaultApacheHttpClientConfig();
        m_clientConfig.getState().setCredentials(null,
                                                 "localhost",
                                                 8080,
                                                 m_config.getLoginUser(),
                                                 m_config.getLoginRepoPassword());
        m_client = ApacheHttpClient.create(m_clientConfig);
    }

    @Override
    public List<BasicReportDefinition> getReports() {
        ArrayList<BasicReportDefinition> resultReports = new ArrayList<BasicReportDefinition>();
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_config.getURI() + "getReports");
            SimpleJasperReportsDefinition webCallResult = m_webResource.get(SimpleJasperReportsDefinition.class);
            for (BasicReportDefinition report : webCallResult.getReportList()) {
                resultReports.add(report);
            }
        }
        return resultReports;
    }

    @Override
    public List<BasicReportDefinition> getOnlineReports() {
        List<BasicReportDefinition> resultReports = new ArrayList<BasicReportDefinition>();
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_config.getURI() + "getOnlineReports");
            
            SimpleJasperReportsDefinition webCallResult = m_webResource.get(SimpleJasperReportsDefinition.class);
            for (BasicReportDefinition report : webCallResult.getReportList()) {
                if (report.getOnline()){
                    resultReports.add(report);
                }
            }
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
                logger.debug("RemoteRepository '{}' is NOT activated.", m_config.getRepositoryName());
                return false;
            }
        } else {
            logger.debug("Problem by RemoteRepository Config Access. No RemoteRepository can be used.");
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
