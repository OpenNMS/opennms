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
package org.opennms.features.reporting.dao.jasper;

import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * <p>LocalJasperReportsDao interface.<p/>
 * <p/>
 * Interface for generic local reports configuration access.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Ronny Trommer <ronny@opennms.com>
 * @version $Id: $
 * @since 1.10.1
 */
public interface LocalJasperReportsDao {

    /**
     * <p>getEngine</p>
     * <p/>
     * Get jasper report database engine
     *
     * @param id a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    String getEngine(String id);


    /**
     * <p>getTemplateStream</p>
     * <p/>
     * Get jasper report template as input stream
     *
     * @param id a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    InputStream getTemplateStream(String id) throws FileNotFoundException;

    /**
     * <p>getTemplateLocation</p>
     * <p/>
     * Get jasper report template location
     *
     * @param id a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    String getTemplateLocation(String id);

    /**
     * <p>loadConfiguration</p>
     *
     * Load XML configuration and unmarshalling
     */
    void loadConfiguration() throws Exception;

    /**
     * <p>setConfigResource</p>
     * 
     * Set configuration resource DAO for jasper reports
     * 
     * @param configResource a {@link org.springframework.core.io.Resource} object
     */
    void setConfigResource(Resource configResource);

    /**
     * <p>getConfigResource</p>>
     * 
     * Get configuration resource DAO for jasper reports
     * 
     * @return a {@link org.springframework.core.io.Resource} object
     */
    Resource getConfigResource();

    /**
     * <p>setJrTemplateResource</p>
     *
     * Set configuration resource DAO for jasper report templates
     *
     * @param jrTemplateResource a {@link org.springframework.core.io.Resource} object
     */
    void setJrTemplateResource(Resource jrTemplateResource);

    /**
     * <p>getJrTemplateResource</p>>
     *
     * Get configuration resource DAO for jasper report templates
     *
     * @return a {@link org.springframework.core.io.Resource} object
     */
    Resource getJrTemplateResource();
}
