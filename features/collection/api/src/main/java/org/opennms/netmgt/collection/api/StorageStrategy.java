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
package org.opennms.netmgt.collection.api;

import java.util.List;
import org.opennms.netmgt.model.ResourcePath;

/**
 * <p>StorageStrategy interface.</p>
 */
public interface StorageStrategy {
    /**
     * <p>getRelativePathForAttribute</p>
     *
     * @param resourceParent a {@link java.lang.String} object.
     * @param resource a {@link java.lang.String} object.
     * @return a {@link java.nio.file.Path} object.
     */
    public ResourcePath getRelativePathForAttribute(ResourcePath resourceParent, String resource);

    /**
     * <p>setResourceTypeName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setResourceTypeName(String name);

    /**
     * This functions translate resourceIndex into a "unique" and "non-variable" name that could be identify
     * a resource, as described earlier.
     * 
     * This method could be expensive because it could require send SNMP queries and make complicated functions to
     * build the name. So you must try to call it only when is necessary.
     *
     * @param resource a {@link org.opennms.netmgt.collection.api.CollectionResource} object
     * @return a {@link java.lang.String} object.
     */
    public String getResourceNameFromIndex(CollectionResource resource);
    
    /**
     * Add to a strategy the possibility to get additional information using SNMP when is necessary.
     * There are complex tables on some MIBs where indexes depends on indexes from other tables (indirect indexing).
     * For this kind of resources we must send some additional SNMP queries to build a unique name.
     * 
     * @param agent a {@link org.opennms.netmgt.config.StorageStrategyService} object.
     */
    public void setStorageStrategyService(StorageStrategyService agent);

    /**
     * <p>setParameters</p>
     *
     * @param parameterCollection a {@link java.util.List} object.
     */
    public void setParameters(List<Parameter> parameterCollection) throws IllegalArgumentException;

}
