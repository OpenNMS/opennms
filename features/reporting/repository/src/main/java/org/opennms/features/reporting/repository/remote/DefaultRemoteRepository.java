/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.reporting.repository.remote;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.jasperreport.SimpleJasperReportDefinition;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.opennms.features.reporting.sdo.RemoteReportSDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

/**
 * <p>DefaultRemoteRepository.java</p>
 *
 * @author <a href="mailto:markus@opennms.org">Markus Neumann</a>
 *
 * @version $Id: $ 
 */

//TODO Tak: SSL Auth and Session-Handling(create/destroy)
public class DefaultRemoteRepository implements ReportRepository {

    private RemoteRepositoryDefinition m_remoteRepositoryDefintion;
    private Logger logger = LoggerFactory.getLogger(DefaultRemoteRepository.class);
    private final String JASPER_REPORTS_VERSION;

    private ApacheHttpClient m_client;
    private ApacheHttpClientConfig m_clientConfig;
    private WebResource m_webResource;

    public DefaultRemoteRepository(
            RemoteRepositoryDefinition remoteRepositoryDefinition,
            String jasperReportsVersion) {
        this.m_remoteRepositoryDefintion = remoteRepositoryDefinition;
        this.JASPER_REPORTS_VERSION = jasperReportsVersion;
        m_clientConfig = new DefaultApacheHttpClientConfig();
        m_clientConfig.getState().setCredentials(null,
                m_remoteRepositoryDefintion.getURI().getHost(),
                m_remoteRepositoryDefintion.getURI().getPort(),
                m_remoteRepositoryDefintion.getLoginUser(),
                m_remoteRepositoryDefintion.getLoginRepoPassword());
        m_client = ApacheHttpClient.create(m_clientConfig);
    }

    @Override
    public List<BasicReportDefinition> getReports() {
        ArrayList<BasicReportDefinition> resultReports = new ArrayList<BasicReportDefinition>();
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_remoteRepositoryDefintion.getURI()
                    + "reports");
            List<RemoteReportSDO> webCallResult = new ArrayList<RemoteReportSDO>();
            try {
                webCallResult = m_webResource.get(new GenericType<List<RemoteReportSDO>>() {
                });
            } catch (Exception e) {
                logger.error("Error requesting report template from repository. Error message: '{}'",
                             e.getMessage());
                e.printStackTrace();
            }

            logger.debug("getReports got '{}' RemoteReportSDOs",
                         webCallResult.size());

            // TODO Tak: clean that up
            for (RemoteReportSDO report : webCallResult) {
                SimpleJasperReportDefinition result = new SimpleJasperReportDefinition();
                try {
                    BeanUtils.copyProperties(result, report);
                    result.setId(m_remoteRepositoryDefintion.getRepositoryId()
                            + "_" + result.getId());
                } catch (IllegalAccessException e) {
                    logger.debug("getReports IllegalAssessException while copyProperties from '{}' to '{}' with exception.",
                                 report, result);
                    logger.error("getReports IllegalAssessException while copyProperties '{}'",
                                 e);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    logger.debug("getReports InvocationTargetException while copyProperties from '{}' to '{}' with exception.",
                                 report, result);
                    logger.error("getReports InvocationTargetException while copyProperties '{}'",
                                 e);
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
        List<RemoteReportSDO> webCallResult = new ArrayList<RemoteReportSDO>();
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_remoteRepositoryDefintion.getURI()
                    + "onlineReports");
            try {
                webCallResult = m_webResource.get(new GenericType<List<RemoteReportSDO>>() {
                });
            } catch (Exception e) {
                logger.error("Error requesting online reports. Error message: '{}'",
                             e.getMessage());
                e.printStackTrace();
            }

            // TODO Tak: clean that up
            for (RemoteReportSDO report : webCallResult) {
                SimpleJasperReportDefinition result = new SimpleJasperReportDefinition();
                try {
                    BeanUtils.copyProperties(result, report);
                    result.setId(m_remoteRepositoryDefintion.getRepositoryId()
                            + "_" + result.getId());
                } catch (IllegalAccessException e) {
                    logger.debug("getOnlineReports IllegalAssessException while copyProperties from '{}' to '{}' with exception.",
                                 report, result);
                    logger.error("getOnlineReports IllegalAssessException while copyProperties '{}'",
                                 e);
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    logger.debug("getOnlineReports InvocationTargetException while copyProperties from '{}' to '{}' with exception.",
                                 report, result);
                    logger.error("getOnlineReports InvocationTargetException while copyProperties '{}'",
                                 e);
                    e.printStackTrace();
                }
                resultReports.add(result);
                logger.debug("getOnlineReports got: '{}'", report.toString());
            }
        }
        return resultReports;
    }

    @Override
    public String getReportService(String reportId) {
        reportId = reportId.substring(reportId.indexOf("_") + 1);
        String result = "";
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_remoteRepositoryDefintion.getURI()
                    + "reportService/" + reportId);
            try {
                result = m_webResource.get(String.class);
            } catch (Exception e) {
                logger.error("Error requesting report service by report id. Error message: '{}'",
                             e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public String getDisplayName(String reportId) {
        reportId = reportId.substring(reportId.indexOf("_") + 1);
        String result = "";
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_remoteRepositoryDefintion.getURI()
                    + "displayName/" + reportId);
            try {
                result = m_webResource.get(String.class);
            } catch (Exception e) {
                logger.error("Error requesting display name by report id. Error message: '{}'",
                             e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public String getEngine(String reportId) {
        reportId = reportId.substring(reportId.indexOf("_") + 1);
        String result = "";
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_remoteRepositoryDefintion.getURI()
                    + "engine/" + reportId);
            try {
                result = m_webResource.get(String.class);
            } catch (Exception e) {
                logger.error("Error requesting engine by id. Error message: '{}'",
                             e.getMessage());
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public InputStream getTemplateStream(String reportId) {
        reportId = reportId.substring(reportId.indexOf("_") + 1);
        InputStream templateStreamResult = null;
        if (isConfigOk()) {
            m_webResource = m_client.resource(m_remoteRepositoryDefintion.getURI()
                    + "templateStream/" + reportId);
            try {
                templateStreamResult = m_webResource.get(InputStream.class);
            } catch (Exception e) {
                logger.error("Error requesting template stream by id. Error message: '{}'",
                             e.getMessage());
                e.printStackTrace();
            }
        }
        return templateStreamResult;
    }

    private Boolean isConfigOk() {
        if (m_remoteRepositoryDefintion != null) {
            if (m_remoteRepositoryDefintion.isRepositoryActive()) {
            } else {
                logger.debug("RemoteRepository '{}' is NOT activated.",
                             m_remoteRepositoryDefintion.getRepositoryName());
                return false;
            }
        } else {
            logger.debug("Problem by RemoteRepository Config Access. No RemoteRepository can be used.");
            return false;
        }
        return true;
    }

    public RemoteRepositoryDefinition getConfig() {
        return this.m_remoteRepositoryDefintion;
    }

    public void setConfig(RemoteRepositoryDefinition remoteRepositoryDefintion) {
        this.m_remoteRepositoryDefintion = remoteRepositoryDefintion;
    }

    @Override
    public String getRepositoryId() {
        return this.m_remoteRepositoryDefintion.getRepositoryId();
    }

    @Override
    public String getRepositoryName() {
        return this.m_remoteRepositoryDefintion.getRepositoryName();
    }

    @Override
    public String getRepositoryDescription() {
        return this.m_remoteRepositoryDefintion.getRepositoryDescription();
    }

    @Override
    public String getManagementUrl() {
        return this.m_remoteRepositoryDefintion.getRepositoryManagementURL();
    }

    public String getJASPER_REPORTS_VERSION() {
        return JASPER_REPORTS_VERSION;
    }
}
