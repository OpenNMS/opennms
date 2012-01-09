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

import org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition;

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
    public Boolean isRepositoryActive(String repositoryID);

    public URI getURI(String repositoryID);

    public String getLoginUser(String repositoryID);

    public String getLoginRepoPassword(String repositoryID);

    public String getRepositoryName(String repositoryID);

    public String getRepositoryDescription(String repositoryID);

    public String getRepositoryManagementURL(String repositoryID);

    public List<RemoteRepositoryDefinition> getAllRepositories();

    public List<RemoteRepositoryDefinition> getActiveRepositories();

    public String getJasperReportsVersion();

    /**
     * <p>getRepositoryById</p>
     *
     * Get a repository by specific repository ID
     *
     * @return a {@link org.opennms.features.reporting.model.remoterepository.RemoteRepositoryDefinition} object
     */
    public RemoteRepositoryDefinition getRepositoryById(String repositoryId);

    /**
     * <p>loadConfiguration</p>
     *
     * Load XML configuration and unmarshalling
     */
    void loadConfiguration() throws Exception;
}