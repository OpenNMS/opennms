/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.config;

public interface StorageStrategy {
    public String getRelativePathForAttribute(String resourceParent, String resource, String attribute);

    public void setResourceTypeName(String name);

    /*
     * This functions translate resourceIndex into a "unique" and "non-variable" name that could be identify
     * a resource, as described earlier.
     * 
     * This method could be expensive because it could require send SNMP queries and make complicated functions to
     * build the name. So you must try to call it only when is necessary.
     */
    public String getResourceNameFromIndex(String resourceParent, String resourceIndex);
    
    /*
     * Add to a strategy the possibility to get additional information using SNMP when is necessary.
     * There are complex tables on some MIBs where indexes depends on indexes from other tables (indirect indexing).
     * For this kind of resources we must send some additional SNMP queries to build a unique name.
     */ 
    public void setStorageStrategyService(StorageStrategyService agent);

}
