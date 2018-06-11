/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.dao.remoterepository;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryConfig;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * <p>DefaultRemoteRepositoryConfigDao class.</p>
 * <p/>
 * Class realize the data access to remote-repository.xml.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Ronny Trommer <ronny@opennms.com>
 * @version $Id: $
 * @since 1.10.1
 */
public class DefaultRemoteRepositoryConfigDao implements
        RemoteRepositoryConfigDao {
    /**
     * Logging
     */
    private Logger logger = LoggerFactory.getLogger(DefaultRemoteRepositoryConfigDao.class);

    /**
     * Config resource for remote repository configuration file
     */
    private Resource m_configResource;

    /**
     * Remote repository model
     */
    private RemoteRepositoryConfig m_remoteRepositoryConfig;

    /**
     * Version number for jasper report
     */
    private String m_jasperReportsVersion;

    /**
     * Default constructor load the configuration file
     */
    public DefaultRemoteRepositoryConfigDao(Resource configResource) {
        m_configResource = configResource;

        Assert.notNull(m_configResource, "property configResource must be set to a non-null value");
        logger.debug("Config resource is set to " + m_configResource.toString());

        try {
            loadConfiguration();
        } catch (Exception e) {
            logger.error("Error could not load remote-repository.xml. Error message: '{}'", e.getMessage());
        }
        logger.debug("Configuration '{}' successfully loaded and unmarshalled.", m_configResource.getFilename());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadConfiguration() throws Exception {
        File file = null;
        try {
            file = m_configResource.getFile();
            Assert.notNull(file, "config file must be set to a non-null value");
        } catch (IOException e) {
            logger.error("Resource '{}' does not seem to have an underlying File object.", m_configResource);
        }

        setRemoteRepositoryConfig(JaxbUtils.unmarshal(RemoteRepositoryConfig.class, file));
        Assert.notNull(m_remoteRepositoryConfig, "unmarshall config file returned a null value.");
        logger.debug("Unmarshalling config file '{}'", file.getAbsolutePath());
        logger.debug("Remote repository configuration assigned: '{}'", m_remoteRepositoryConfig.toString());

        //TODO indigo: The jasper report version should be configured here?
        setJasperReportsVersion(m_remoteRepositoryConfig.getJasperReportsVersion());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getConfigResource() {
        return m_configResource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfigResource(Resource configResource) {
        m_configResource = configResource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJasperReportsVersion() {
        return m_jasperReportsVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean isRepositoryActive(String repositoryID) {
        return this.getRepositoryById(repositoryID).isRepositoryActive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getURI(String repositoryID) {
        return this.getRepositoryById(repositoryID).getURI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLoginUser(String repositoryID) {
        return this.getRepositoryById(repositoryID).getLoginUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLoginRepoPassword(String repositoryID) {
        return this.getRepositoryById(repositoryID).getLoginRepoPassword();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRepositoryName(String repositoryID) {
        return this.getRepositoryById(repositoryID).getRepositoryName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRepositoryDescription(String repositoryID) {
        return this.getRepositoryById(repositoryID).getRepositoryDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRepositoryManagementURL(String repositoryID) {
        return this.getRepositoryById(repositoryID).getRepositoryManagementURL();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RemoteRepositoryDefinition> getAllRepositories() {
        List<RemoteRepositoryDefinition> resultList = new ArrayList<>();
        resultList.addAll(this.m_remoteRepositoryConfig.getRepositoryList());
        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RemoteRepositoryDefinition> getActiveRepositories() {
        List<RemoteRepositoryDefinition> resultList = new ArrayList<>();
        for (RemoteRepositoryDefinition repository : this.m_remoteRepositoryConfig.getRepositoryList()) {
            if (repository.isRepositoryActive()) {
                resultList.add(repository);
            }
        }
        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RemoteRepositoryDefinition getRepositoryById(String repositoryID) {
        //TODO Tak: How to fail safe this?
        RemoteRepositoryDefinition result = null;
        for (RemoteRepositoryDefinition repository : this.getAllRepositories()) {
            if (repositoryID.equals(repository.getRepositoryId())) {
                return repository;
            }
        }
        return result;
    }

    /**
     * <p>setRemoteRepositoryConfig</p>
     * <p/>
     * Set remote repository configuration
     *
     * @param remoteRepositoryConfig a {@link org.opennms.features.reporting.model.remoterepository.RemoteRepositoryConfig} object
     */
    private void setRemoteRepositoryConfig(RemoteRepositoryConfig remoteRepositoryConfig) {
        m_remoteRepositoryConfig = remoteRepositoryConfig;
    }

    /**
     * <p>setJasperReportsVersion</p>
     * <p/>
     * Set version for jasper report
     *
     * @param jasperReportsVersion a {@link java.lang.String} object
     */
    private void setJasperReportsVersion(String jasperReportsVersion) {
        m_jasperReportsVersion = jasperReportsVersion;
    }
}
