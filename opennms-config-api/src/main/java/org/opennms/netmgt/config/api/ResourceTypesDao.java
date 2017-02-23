/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.api;

import java.util.Date;
import java.util.Map;

import org.opennms.netmgt.config.datacollection.ResourceType;

/**
 * Used to retrieve the aggregated list of resource types stored in:
 *   * $OPENNMS_HOME/etc/resource-types/
 *   * $OPENNMS_HOME/etc/datacollection/
 *
 * Previously, all of the generic resource type definition were
 * stored in 'etc/datacollection/', which holds SNMP collector
 * specific configuration.
 *
 * Since resource types are shared amongst collectors, we opted
 * to relocate these to their own folder, while continuing to
 * include resource types stored in original directory. 
 *
 * @author jwhite
 */
public interface ResourceTypesDao {

    public Map<String,ResourceType> getResourceTypes();

    public ResourceType getResourceTypeByName(String name);

    public Date getLastUpdate();
}
