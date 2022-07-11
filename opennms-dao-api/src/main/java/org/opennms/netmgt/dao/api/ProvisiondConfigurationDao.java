/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;

/**
 * DAO interface for Provisiond configuration
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public interface ProvisiondConfigurationDao {
    
    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.provisiond.ProvisiondConfiguration} object.
     */
    ProvisiondConfiguration getConfig() throws IOException;
    
    /**
     * <p>getDef</p>
     *
     * @param defName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.provisiond.RequisitionDef} object.
     */
    RequisitionDef getDef(String defName) throws IOException;
    
    /**
     * <p>getDefs</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<RequisitionDef> getDefs() throws IOException;
    
    /**
     * <p>getImportThreads</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getImportThreads() throws IOException;
    
    /**
     * <p>getScanThreads</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getScanThreads() throws IOException;
    
    /**
     * <p>getRescanThreads</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getRescanThreads() throws IOException;
    
    /**
     * <p>getWriteThreads</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getWriteThreads() throws IOException;
    
    /**
     * <p>getRequisitionDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRequisitionDir() throws IOException;
    
    /**
     * <p>getForeignSourceDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignSourceDir() throws IOException;
        
    /**
     * The underlying XML-based DAO abstraction in the default implementation doesn't provide access to the container so
     * this method is defined so that access to the container doesn't have to be exposed and a reload can still be controlled
     * by the user.
     *
     * Automatically reading in new values if the file changes is a different use case from expecting the services to alter
     * their state based on a configuration change.  This method will most likely be used with event processing and possibly
     * in the ReST API.
     *
     * @throws org.springframework.dao.DataAccessResourceFailureException if any.
     */
    void reloadConfiguration() throws IOException;

    Map<String, Long> getRequisitionSchemeCount() throws IOException;
}
