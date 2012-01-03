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

package org.opennms.features.reporting.dao.remoterepository;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryConfig;
import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRemoteRepositoryConfigDao implements
        RemoteRepositoryConfigDao {

    Logger logger = LoggerFactory.getLogger(DefaultRemoteRepositoryConfigDao.class);
    
    private final String REMOTE_REPOSITORY_XML = System.getProperty("opennms.home")
            + File.separator
            + "etc"
            + File.separator
            + "remote-repository.xml";

    private RemoteRepositoryConfig config;

    private RemoteRepositoryConfig readConfig() {
        try {
            config = JAXB.unmarshal(new File(REMOTE_REPOSITORY_XML), RemoteRepositoryConfig.class);
        } catch (Exception e) {
            logger.error("fail to unmarshal file '{}', '{}'", REMOTE_REPOSITORY_XML, e.getMessage());
            e.printStackTrace();
        }
        return config;
    }

    @Override
    public String getJasperReportsVersion() {
        return this.readConfig().getJasperRepotsVersion();
    }
    
    @Override
    public Boolean isRepositoryActive(String repositoryID) {
        return this.getRepositoryById(repositoryID).isRepositoryActive();
    }

    @Override
    public URI getURI(String repositoryID) {
        return this.getRepositoryById(repositoryID).getURI();
    }

    @Override
    public String getLoginUser(String repositoryID) {
        return this.getRepositoryById(repositoryID).getLoginUser();
    }

    @Override
    public String getLoginRepoPassword(String repositoryID) {
        return this.getRepositoryById(repositoryID).getLoginRepoPassword();
    }

    @Override
    public String getRepositoryName(String repositoryID) {
        return this.getRepositoryById(repositoryID).getRepositoryName();
    }

    @Override
    public String getRepositoryDescription(String repositoryID) {
        return this.getRepositoryById(repositoryID).getRepositoryDescription();
    }

    @Override
    public String getRepositoryManagementURL(String repositoryID) {
        return this.getRepositoryById(repositoryID).getRepositoryManagementURL();
    }

    //TODO Tak: How to fail safe this?
    public RemoteRepositoryDefinition getRepositoryById(String repositoryID) {
        RemoteRepositoryDefinition result = null;
        for(RemoteRepositoryDefinition repository : this.getAllRepositories()) {
            if (repositoryID.equals(repository.getRepositoryId())) {
                return repository;
            }
        }
        return result;
    }

    @Override
    public List<RemoteRepositoryDefinition> getAllRepositories() {
        List<RemoteRepositoryDefinition> resultList = new ArrayList<RemoteRepositoryDefinition>();
        resultList.addAll(this.readConfig().getRepositoryList());
        return resultList;
    }

    @Override
    public List<RemoteRepositoryDefinition> getActiveRepositories() {
        List<RemoteRepositoryDefinition> resultList = new ArrayList<RemoteRepositoryDefinition>();
        for (RemoteRepositoryDefinition repository : this.readConfig().getRepositoryList()) {
            if (repository.isRepositoryActive()) {
                resultList.add(repository);
            }
        }
        return resultList;
    }
}
