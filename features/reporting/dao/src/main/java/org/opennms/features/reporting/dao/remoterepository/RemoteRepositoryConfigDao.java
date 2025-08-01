/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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