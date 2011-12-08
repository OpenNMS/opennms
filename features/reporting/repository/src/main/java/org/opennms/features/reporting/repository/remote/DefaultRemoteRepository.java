package org.opennms.features.reporting.repository.remote;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.opennms.features.reporting.dao.remoterepository.DefaultRemoteRepositoryConfigDao;
import org.opennms.features.reporting.dao.remoterepository.RemoteRepositoryConfigDao;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.jasperreport.SimpleJasperReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.opennms.features.reporting.sdo.RemoteReportSDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.GenericType;
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
            m_webResource = m_client.resource(m_config.getURI()
                    + "getReports");
            List<RemoteReportSDO> webCallResult = m_webResource.get(new GenericType<List<RemoteReportSDO>>() {});
            logger.debug("getReports got '{}' RemoteReportSDOs",
                         webCallResult.size());
            
            //TODO Tak: clean that up
            for (RemoteReportSDO report : webCallResult) {
                SimpleJasperReportDefinition result = new SimpleJasperReportDefinition();
                try {
                    BeanUtils.copyProperties(result, report);
                } catch (IllegalAccessException e) {
                    logger.debug("getReports IllegalAssessException while copyProperties from '{}' to '{}' with exception.", report, result);
                    logger.error("getReports IllegalAssessException while copyProperties '{}'", e);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    logger.debug("getReports InvocationTargetException while copyProperties from '{}' to '{}' with exception.", report, result);
                    logger.error("getReports InvocationTargetException while copyProperties '{}'", e);
                    e.printStackTrace();
                }
                resultReports.add(result);
                logger.debug("getReports got: '{}'", report.toString());
            }
        }
        return resultReports;
    }

    @Override
    public List<BasicReportDefinition> getOnlineReports() {
        List<BasicReportDefinition> resultReports = new ArrayList<BasicReportDefinition>();
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_config.getURI()
                    + "getOnlineReports");

            List<RemoteReportSDO> webCallResult = m_webResource.get(new GenericType<List<RemoteReportSDO>>() {});
            
          //TODO Tak: clean that up
            for (RemoteReportSDO report : webCallResult) {
                SimpleJasperReportDefinition result = new SimpleJasperReportDefinition();
                try {
                    BeanUtils.copyProperties(result, report);
                } catch (IllegalAccessException e) {
                    logger.debug("getOnlineReports IllegalAssessException while copyProperties from '{}' to '{}' with exception.", report, result);
                    logger.error("getOnlineReports IllegalAssessException while copyProperties '{}'", e);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    logger.debug("getOnlineReports InvocationTargetException while copyProperties from '{}' to '{}' with exception.", report, result);
                    logger.error("getOnlineReports InvocationTargetException while copyProperties '{}'", e);
                    e.printStackTrace();
                }
                resultReports.add(result);
                logger.debug("getOnlineReports got: '{}'", report.toString());
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
                logger.debug("RemoteRepository '{}' is NOT activated.",
                             m_config.getRepositoryName());
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

    @Override
    public String getRepositoryId() {
        return m_config.getRepositoryId();
    }
}
