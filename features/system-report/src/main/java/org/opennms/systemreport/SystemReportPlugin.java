/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.systemreport;

import java.util.Map;
import org.springframework.core.io.Resource;

public interface SystemReportPlugin extends Comparable<SystemReportPlugin> {
    /**
     * Get the name of this report plugin.
     * @return the name
     */
    public String getName();

    /**
     * Get a short description of the plugin's operation.
     * @return the description
     */
    public String getDescription();
    
    /**
     * Get the priority of this plugin.  This will be used to sort the various plugins' output when creating an aggregate report.
     * 1-10: system-level plugins
     * 11-50: related to core system functionality (eg, events, alarms, notifications)
     * 51-98: related to non-essential system functionality (eg, UI, reporting)
     * 99: unknown priority
     * @return the priority, from 1 to 99
     */
    public int getPriority();

    /**
     * Get a map of key/value pairs of data exposed by the plugin.
     * @return the plugin's data
     */
    public Map<String,Resource> getEntries();
}
