/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.model;

import java.util.Map;

public class PrefabGraphType {
    private Map<String, PrefabGraph> m_reportMap;

    private String m_defaultReport;

    private String m_name;

    private String m_commandPrefix;

    private String m_outputMimeType;

    private String m_graphWidth;

    private String m_graphHeight;

    public PrefabGraphType() {
        
    }
    
    public void setName(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

    public void setDefaultReport(String defaultReport) {
        m_defaultReport = defaultReport;
    }

    public String getDefaultReport() {
        return m_defaultReport;
    }
    
    public void setReportMap(Map<String, PrefabGraph> reportMap) {
        m_reportMap = reportMap;
    }
    
    public Map<String, PrefabGraph> getReportMap() {
        return m_reportMap;
    }

    public PrefabGraph getQuery(String queryName) {
        return m_reportMap.get(queryName);
    }
    
    public void setGraphWidth(String graphWidth) {
        m_graphWidth = graphWidth;
    }

    public String getGraphWidth() {
        return m_graphWidth;
    }
    
    public void setGraphHeight(String graphHeight) {
        m_graphHeight = graphHeight;
    }

    public String getGraphHeight() {
        return m_graphHeight;
    }

    public void setCommandPrefix(String commandPrefix) {
        m_commandPrefix = commandPrefix;
    }
    
    public String getCommandPrefix() {
        return m_commandPrefix;
    }

    public void setOutputMimeType(String outputMimeType) {
        m_outputMimeType = outputMimeType;
    }
    
    public String getOutputMimeType() {
        return m_outputMimeType;
    }

}
