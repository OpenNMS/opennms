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

package org.opennms.netmgt.jasper.resource;

import java.io.File;
import java.util.Arrays;

public class ResourceQuery {
    private String m_rrdDir;
    private String m_node;
    private String m_resourceName;
    private String[] m_filters;
    private String[] m_strProperties;

    public ResourceQuery() {
    }
    
    public String getRrdDir() {
        return m_rrdDir;
    }
    public void setRrdDir(String rrdDir) {
        m_rrdDir = rrdDir;
    }
    public String getNodeId() {
        return m_node;
    }
    public void setNodeId(String node) {
        m_node = node;
    }
    public String getResourceName() {
        return m_resourceName;
    }
    public void setResourceName(String resourceName) {
        m_resourceName = resourceName;
    }
    public String[] getFilters() {
        return m_filters;
    }
    public void setFilters(String[] filters) {
        m_filters = Arrays.copyOf(filters, filters.length);
    }
    
    public String constructBasePath() {
        return getRrdDir() + File.separator + getNodeId() + File.separator + getResourceName();
    }

    public String[] getStringProperties() {
        return m_strProperties;
    }
    
    public void setStringProperties(String[] strProperties) {
        m_strProperties = Arrays.copyOf(strProperties, strProperties.length);
    }
}