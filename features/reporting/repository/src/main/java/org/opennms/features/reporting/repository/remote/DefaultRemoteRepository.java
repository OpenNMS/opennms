/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.cxf.common.util.Base64Utility;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.jasperreport.SimpleJasperReportDefinition;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.opennms.features.reporting.sdo.RemoteReportSDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>DefaultRemoteRepository class.</p>
 * <p/>
 * Implementation of OpenNMS CONNECT CIO Report repository
 *
 * @author Markus Neumann <markus@opennms.com>
 * @version $Id: $
 * @since 1.10.1
 */
public class DefaultRemoteRepository implements ReportRepository {

    /**
     * Logging
     */
    private Logger logger = LoggerFactory.getLogger(DefaultRemoteRepository.class);

    /**
     * Model for repository configuration for remote-repository.xml
     */
    private RemoteRepositoryDefinition m_remoteRepositoryDefintion;

    /**
     * Jasper report version number
     */
    private String m_jasperReportsVersion;

    private final String m_authorizationHeader;

    /**
     * Default constructor to initialize the ReST HTTP client
     *
     * @param remoteRepositoryDefinition a {@link org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition} object
     * @param jasperReportsVersion       a {@link java.lang.String} object
     */
    public DefaultRemoteRepository(
            RemoteRepositoryDefinition remoteRepositoryDefinition,
            String jasperReportsVersion) {
        this.m_remoteRepositoryDefintion = remoteRepositoryDefinition;
        this.m_jasperReportsVersion = jasperReportsVersion;
        m_authorizationHeader = "Basic " + Base64Utility.encode((m_remoteRepositoryDefintion.getLoginUser() + ":" + m_remoteRepositoryDefintion.getLoginRepoPassword()).getBytes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getReports() {
        List<BasicReportDefinition> resultReports = new ArrayList<>();
        if (isConfigOk()) {
            WebTarget target = getTarget(m_remoteRepositoryDefintion.getURI() + "reports" + "/" + m_jasperReportsVersion);

            List<RemoteReportSDO> webCallResult = new ArrayList<>();
            try {
                webCallResult = getBuilder(target).get(new GenericType<List<RemoteReportSDO>>() {});
            } catch (Exception e) {
                logger.error("Error requesting report template from repository. Error message: '{}' Uri was: '{}'", e.getMessage(), target.getUri());
                e.printStackTrace();
            }

            logger.debug("getReports got '{}' RemoteReportSDOs from uri '{}'", webCallResult.size(), target.getUri());

            resultReports = this.mapSDOListToBasicReportList(webCallResult);

        }
        return resultReports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BasicReportDefinition> getOnlineReports() {
        List<BasicReportDefinition> resultReports = new ArrayList<>();
        List<RemoteReportSDO> webCallResult = new ArrayList<>();
        if (isConfigOk()) {
            WebTarget target = getTarget(m_remoteRepositoryDefintion.getURI() + "onlineReports" + "/" + m_jasperReportsVersion);
            try {
                webCallResult = getBuilder(target).get(new GenericType<List<RemoteReportSDO>>() {});
            } catch (Exception e) {
                logger.error("Error requesting online reports. Error message: '{}' URI was: '{}'", e.getMessage(), target.getUri());
                e.printStackTrace();
            }
            
            logger.debug("getOnlineReports got '{}' RemoteReportSDOs from uri '{}'", webCallResult.size(), target.getUri());
            
            resultReports = this.mapSDOListToBasicReportList(webCallResult);
        }
        return resultReports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getReportService(String reportId) {
        reportId = reportId.substring(reportId.indexOf('_') + 1);
        String result = "";
        if (isConfigOk()) {
            WebTarget target = getTarget(m_remoteRepositoryDefintion.getURI() + "reportService/" + reportId);
            try {
                result = getBuilder(target).get(String.class);
            } catch (Exception e) {
                logger.error("Error requesting report service by report id. Error message: '{}' URI was: '{}'", e.getMessage(), target.getUri());
                e.printStackTrace();
            }
            logger.debug("getReportService for id / result: '{}' URI was: '{}' ", reportId + " / " + result, target.getUri());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName(String reportId) {
        reportId = reportId.substring(reportId.indexOf('_') + 1);
        String result = "";
        if (isConfigOk()) {
            WebTarget target = getTarget(m_remoteRepositoryDefintion.getURI() + "displayName/" + reportId);
            try {
                result = getBuilder(target).get(String.class);
            } catch (Exception e) {
                logger.error("Error requesting display name by report id. Error message: '{}' URI was: '{}'", e.getMessage(), target.getUri());
                e.printStackTrace();
            }
            
            logger.debug("getDisplayName for id / result: '{}' URI was: '{}' ", reportId + " / " + result, target.getUri());
            
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEngine(String reportId) {
        reportId = reportId.substring(reportId.indexOf('_') + 1);
        String result = "";
        if (isConfigOk()) {
            WebTarget target = getTarget(m_remoteRepositoryDefintion.getURI() + "engine/" + reportId);
            try {
                result = getBuilder(target).get(String.class);
            } catch (Exception e) {
                logger.error("Error requesting engine by id. Error message: '{}' URI was: '{}'", e.getMessage(), target.getUri());
                e.printStackTrace();
            }
            
            logger.debug("getEngine for id / result: '{}' URI was: '{}' ", reportId + " / " + result, target.getUri());
            
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getTemplateStream(String reportId) {
        reportId = reportId.substring(reportId.indexOf('_') + 1);
        InputStream templateStreamResult = null;
        if (isConfigOk()) {
            WebTarget target = getTarget(m_remoteRepositoryDefintion.getURI() + "templateStream/" + reportId);
            try {
                templateStreamResult = getBuilder(target).get(InputStream.class);
            } catch (Exception e) {
                logger.error("Error requesting template stream by id. Error message: '{}' URI was: '{}'", e.getMessage(), target.getUri());
                e.printStackTrace();
            }
            
            logger.debug("getTemplateStream for id / inputstream: '{}' URI was: '{}' ", reportId + " / " + templateStreamResult, target.getUri());
            
        }
        return templateStreamResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRepositoryId() {
        logger.debug("getRepositoryId was called: '{}'", m_remoteRepositoryDefintion.getRepositoryId());
        return this.m_remoteRepositoryDefintion.getRepositoryId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRepositoryName() {
        logger.debug("getRepositoryName was called: '{}'", m_remoteRepositoryDefintion.getRepositoryName());
        return this.m_remoteRepositoryDefintion.getRepositoryName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRepositoryDescription() {
        logger.debug("getRepositoryDescription was called: '{}'", m_remoteRepositoryDefintion.getRepositoryDescription());
        return this.m_remoteRepositoryDefintion.getRepositoryDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getManagementUrl() {
        logger.debug("getRepositoryDescription was called: '{}'", m_remoteRepositoryDefintion.getRepositoryDescription());
        return this.m_remoteRepositoryDefintion.getRepositoryManagementURL();
    }

    private Boolean isConfigOk() {
        if (m_remoteRepositoryDefintion != null) {
            if (m_remoteRepositoryDefintion.isRepositoryActive()) {
            } else {
                logger.debug("RemoteRepository '{}' is NOT activated.", m_remoteRepositoryDefintion.getRepositoryName());
                return false;
            }
        } else {
            logger.debug("Problem by RemoteRepository Config Access. RemoteRepository can't be used.");
            return false;
        }
        return true;
    }

    private List<BasicReportDefinition> mapSDOListToBasicReportList(List<RemoteReportSDO> remoteReportSDOList) {
        List<BasicReportDefinition> resultList = new ArrayList<>();
        for (RemoteReportSDO report : remoteReportSDOList) {
            SimpleJasperReportDefinition result = new SimpleJasperReportDefinition();
            try {
                BeanUtils.copyProperties(result, report);
                result.setId(m_remoteRepositoryDefintion.getRepositoryId()
                        + "_" + result.getId());
            } catch (IllegalAccessException e) {
                logger.debug("SDO to BasicReport mapping IllegalAssessException while copyProperties from '{}' to '{}' with exception.", report, result);
                logger.error("SDO to BasicReport mapping IllegalAssessException while copyProperties '{}' RepositoryURI: '{}'", e, m_remoteRepositoryDefintion.getURI());
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                logger.debug("SDO to BasicReport mapping InvocationTargetException while copyProperties from '{}' to '{}' with exception.", report, result);
                logger.error("SDO to BasicReport mapping InvocationTargetException while copyProperties '{}' RepositoryURI: '{}'", e, m_remoteRepositoryDefintion.getURI());
                e.printStackTrace();
            }

            logger.debug("SDO to BasicReport mapping got: '{}'", report.toString());
            resultList.add(result);
        }
        logger.debug("SDO to BasicReport mapping returns resultList: '{}'", resultList.toString());
        return resultList;
    }

    @Override
    public void loadConfiguration() {
        logger.debug("reloading for configuration was called. No reoad for remote repository possible.");
    }

    private WebTarget getTarget(String url) {
        final Client client = ClientBuilder.newClient();
        return client.target(url);
    }

    private Invocation.Builder getBuilder(final WebTarget target) {
        return target.request().header("Authorization", m_authorizationHeader);
    }
}
