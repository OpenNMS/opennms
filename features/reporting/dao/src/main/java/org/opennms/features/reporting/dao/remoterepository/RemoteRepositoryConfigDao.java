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

package org.opennms.features.reporting.dao.remoterepository;

import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;
import org.springframework.core.io.Resource;

import java.net.URI;
import java.util.List;

/**
 * <p>RemoteRepositoryConfigDao interface.</p>
 * <p/>
 * Interface for generic remote repository configuration access.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Ronny Trommer <ronny@opennms.com>
 * @version $Id: $
 * @since 1.8.1
 */
public interface RemoteRepositoryConfigDao {

    /**
     * <p>isRepositoryActive</p>
     * <p/>
     * Get activity state from a specific repository by ID
     *
     * @param repositoryID a {@link java.lang.String} object
     * @return a {@link java.lang.Boolean} object
     */
    public Boolean isRepositoryActive(String repositoryID);

    /**
     * <p>getURI</p>
     * <p/>
     * Get repository URI from specific a repository by ID
     *
     * @param repositoryID a {@link java.lang.String} object
     * @return a {@link java.net.URI} object
     */
    public URI getURI(String repositoryID);

    /**
     * <p>getLoginUser</p>
     * <p/>
     * Get login user name from a specific repository by ID
     *
     * @param repositoryID a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public String getLoginUser(String repositoryID);

    /**
     * <p>getLoginRepoPassword</p>
     * <p/>
     * Get login password from a specific repository by ID
     *
     * @param repositoryID a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public String getLoginRepoPassword(String repositoryID);

    /**
     * <p>getRepositoryName</p>
     * <p/>
     * Get repository name from a specific repository by ID
     *
     * @param repositoryID a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public String getRepositoryName(String repositoryID);

    /**
     * <p>getRepositoryDescription</p>
     * <p/>
     * Get description from a specific repository by ID
     *
     * @param repositoryID a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public String getRepositoryDescription(String repositoryID);

    /**
     * <p>getRepositoryManagementURL</p>
     * <p/>
     * Get management URL from a specific repository by ID
     *
     * @param repositoryID a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public String getRepositoryManagementURL(String repositoryID);

    /**
     * <p>getAllRepositories</p>
     * <p/>
     * Get *ALL* configured repositories
     *
     * @return a {@link java.util.List<RemoteRepositoryDefinition>} object
     */
    public List<RemoteRepositoryDefinition> getAllRepositories();

    /**
     * <p>getActiveRepositories</p>
     * <p/>
     * Get all *ACTIVE* repositories
     *
     * @return a {@link java.util.List<RemoteRepositoryDefinition>} object
     */
    public List<RemoteRepositoryDefinition> getActiveRepositories();

    /**
     * @return a {@link java.lang.String} object
     */
    @Deprecated
    public String getJasperReportsVersion();

    /**
     * <p>getRepositoryById</p>
     * <p/>
     * Get a repository by specific repository ID
     *
     * @return a {@link org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition} object
     */
    public RemoteRepositoryDefinition getRepositoryById(String repositoryId);

    /**
     * <p>loadConfiguration</p>
     * <p/>
     * Load XML configuration and unmarshalling
     */
    void loadConfiguration() throws Exception;

    /**
     * <p>getConfigResource</p>
     * <p/>
     * Get a resource for the remote repository configuration
     *
     * @return a {@link org.springframework.core.io.Resource} object
     */
    Resource getConfigResource();


    /**
     * <p>setConfigResource</p>
     * <p/>
     * Set a resource for the remote repository configuration
     *
     * @param configResource a {@link org.springframework.core.io.Resource} object
     */
    void setConfigResource(Resource configResource);
}