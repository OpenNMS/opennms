/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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


public class AdhocGraphType {
    private String m_name;

    private String m_commandPrefix;

    private String m_outputMimeType;

    private String m_titleTemplate;

    private String m_dataSourceTemplate;

    private String m_graphLineTemplate;

    public AdhocGraphType() {
        
    }
    
    public void setName(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
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

    public void setTitleTemplate(String template) {
        m_titleTemplate = template;
    }

    public void setDataSourceTemplate(String template) {
        m_dataSourceTemplate = template;
    }

    public void setGraphLineTemplate(String template) {
        m_graphLineTemplate = template;
    }

    public String getDataSourceTemplate() {
        return m_dataSourceTemplate;
    }

    public String getGraphLineTemplate() {
        return m_graphLineTemplate;
    }

    public String getTitleTemplate() {
        return m_titleTemplate;
    }

}
