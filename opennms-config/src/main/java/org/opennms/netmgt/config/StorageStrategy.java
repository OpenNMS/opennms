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

package org.opennms.netmgt.config;

import java.util.List;

import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.datacollection.Parameter;

/**
 * <p>StorageStrategy interface.</p>
 */
public interface StorageStrategy {
    /**
     * <p>getRelativePathForAttribute</p>
     *
     * @param resourceParent a {@link java.lang.String} object.
     * @param resource a {@link java.lang.String} object.
     * @param attribute a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getRelativePathForAttribute(String resourceParent, String resource, String attribute);

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
     * @param resource a {@link org.opennms.netmgt.config.collector.CollectionResource} object
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
