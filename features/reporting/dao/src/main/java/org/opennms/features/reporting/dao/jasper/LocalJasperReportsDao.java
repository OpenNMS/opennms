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
