/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;


/**
 * <p>AdhocGraphType class.</p>
 */
public class AdhocGraphType {
    private String m_name;

    private String m_commandPrefix;

    private String m_outputMimeType;

    private String m_titleTemplate;

    private String m_dataSourceTemplate;

    private String m_graphLineTemplate;

    /**
     * <p>Constructor for AdhocGraphType.</p>
     */
    public AdhocGraphType() {
        
    }
    
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setCommandPrefix</p>
     *
     * @param commandPrefix a {@link java.lang.String} object.
     */
    public void setCommandPrefix(String commandPrefix) {
        m_commandPrefix = commandPrefix;
    }
    
    /**
     * <p>getCommandPrefix</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCommandPrefix() {
        return m_commandPrefix;
    }

    /**
     * <p>setOutputMimeType</p>
     *
     * @param outputMimeType a {@link java.lang.String} object.
     */
    public void setOutputMimeType(String outputMimeType) {
        m_outputMimeType = outputMimeType;
    }
    
    /**
     * <p>getOutputMimeType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOutputMimeType() {
        return m_outputMimeType;
    }

    /**
     * <p>setTitleTemplate</p>
     *
     * @param template a {@link java.lang.String} object.
     */
    public void setTitleTemplate(String template) {
        m_titleTemplate = template;
    }

    /**
     * <p>setDataSourceTemplate</p>
     *
     * @param template a {@link java.lang.String} object.
     */
    public void setDataSourceTemplate(String template) {
        m_dataSourceTemplate = template;
    }

    /**
     * <p>setGraphLineTemplate</p>
     *
     * @param template a {@link java.lang.String} object.
     */
    public void setGraphLineTemplate(String template) {
        m_graphLineTemplate = template;
    }

    /**
     * <p>getDataSourceTemplate</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDataSourceTemplate() {
        return m_dataSourceTemplate;
    }

    /**
     * <p>getGraphLineTemplate</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGraphLineTemplate() {
        return m_graphLineTemplate;
    }

    /**
     * <p>getTitleTemplate</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTitleTemplate() {
        return m_titleTemplate;
    }

}
