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
