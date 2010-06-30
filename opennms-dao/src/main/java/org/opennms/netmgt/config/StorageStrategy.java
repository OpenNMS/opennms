//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.util.List;

import org.opennms.netmgt.config.datacollection.Parameter;

/**
 * <p>StorageStrategy interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface StorageStrategy {
    /**
     * <p>getRelativePathForAttribute</p>
     *
     * @param resourceParent a {@link java.lang.String} object.
     * @param resourceParent a {@link java.lang.String} object.
     * @param resourceParent a {@link java.lang.String} object.
     * @param resourceParent a {@link java.lang.String} object.
     * @param resourceParent a {@link java.lang.String} object.
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

    /*
     * This functions translate resourceIndex into a "unique" and "non-variable" name that could be identify
     * a resource, as described earlier.
     * 
     * This method could be expensive because it could require send SNMP queries and make complicated functions to
     * build the name. So you must try to call it only when is necessary.
     */
    /**
     * <p>getResourceNameFromIndex</p>
     *
     * @param resourceParent a {@link java.lang.String} object.
     * @param resourceIndex a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getResourceNameFromIndex(String resourceParent, String resourceIndex);
    
    /*
     * Add to a strategy the possibility to get additional information using SNMP when is necessary.
     * There are complex tables on some MIBs where indexes depends on indexes from other tables (indirect indexing).
     * For this kind of resources we must send some additional SNMP queries to build a unique name.
     */ 
    /**
     * <p>setStorageStrategyService</p>
     *
     * @param agent a {@link org.opennms.netmgt.config.StorageStrategyService} object.
     */
    public void setStorageStrategyService(StorageStrategyService agent);

    /**
     * <p>setParameters</p>
     *
     * @param parameterCollection a {@link java.util.List} object.
     */
    public void setParameters(List<Parameter> parameterCollection);

}
