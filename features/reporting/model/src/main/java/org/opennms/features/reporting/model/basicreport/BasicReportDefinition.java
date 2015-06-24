/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.model.basicreport;

public interface BasicReportDefinition {
    
    public abstract String getDescription();

    public abstract String getDisplayName();

    public abstract String getId();
    
    public abstract String getRepositoryId();

    public abstract boolean getOnline();

    public abstract String getReportService();

    public abstract void setId(String id);

    public abstract void setDisplayName(String displayName);

    public abstract void setReportService(String reportService);

    public abstract void setDescription(String description);

    public abstract void setOnline(boolean online);

    public abstract boolean getAllowAccess ();

    public abstract void setAllowAccess(boolean allowAccess);
}
