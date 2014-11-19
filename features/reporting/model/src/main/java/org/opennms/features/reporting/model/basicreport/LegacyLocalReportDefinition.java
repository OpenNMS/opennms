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

package org.opennms.features.reporting.model.basicreport;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Report.
 *
 * @version $Revision$ $Date$
 */

@XmlRootElement(name = "report")
public class LegacyLocalReportDefinition implements BasicReportDefinition {

    /**
     * the name of this report as defined in engine
     * configuration
     */
    private String m_id;

    /**
     * the name of this report as defined in engine
     * configuration
     */
    private String m_repositoryId;

    /**
     * the name of this report as displayed in the webui
     */
    private String m_displayName;

    /**
     * the name of the engine to use to process and
     * render this report
     */
    private String m_reportService;

    /**
     * report m_description
     */
    private String m_description;

    /**
     * determines if the report may be executed and immediately
     * displayed in the browser. If not set OpenNMS assumes that
     * the report
     * must be executed in batch mode.
     */
    private boolean m_online;

    /**
     * Define the access to this report.
     */
    private boolean m_allowAccess;

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getDescription()
     */
    @Override
    @XmlAttribute(name = "description")
    public String getDescription() {
        return this.m_description;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getDisplayName()
     */
    @Override
    @XmlAttribute(name = "display-name")
    public String getDisplayName() {
        return this.m_displayName;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getId()
     */
    @Override
    @XmlAttribute(name = "id")
    public String getId() {
        return this.m_id;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getOnline()
     */
    @Override
    @XmlAttribute(name = "online")
    public boolean getOnline() {
        return this.m_online;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getReportService()
     */
    @Override
    @XmlAttribute(name = "report-service")
    public String getReportService() {
        return this.m_reportService;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setId(java.lang.String)
     */
    @Override
    public void setId(String id) {
        this.m_id = id;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setDisplayName(java.lang.String)
     */
    @Override
    public void setDisplayName(String displayName) {
        this.m_displayName = displayName;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setReportService(java.lang.String)
     */
    @Override
    public void setReportService(String reportService) {
        this.m_reportService = reportService;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.m_description = description;
    }

    @Override
    public boolean getAllowAccess() {
        return m_allowAccess;
    }

    @Override
    public void setAllowAccess(boolean allowAccess) {
        m_allowAccess = allowAccess;
    }

    @Override
    public void setOnline(boolean online) {
        this.m_online = online;
    }

    @Override
    public String getRepositoryId() {
        return m_repositoryId;
    }
}
