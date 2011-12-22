/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.reporting.model.basicreport;

import javax.xml.bind.annotation.*;

/**
 * Class Report.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name = "report")
public class LegacyLocalReportDefinition implements BasicReportDefinition {

    /**
     * the name of this report as defined in engine
     *  configuration
     */
    private String id;

    /**
     * the name of this report as defined in engine
     *  configuration
     */
    private String repositoryId;
    
    /**
     * the name of this report as displayed in the webui
     *  
     */
    private String displayName;

    /**
     * the name of the engine to use to process and
     *  render this report
     */
    private String reportService;

    /**
     * report description
     */
    private String description;

    /**
     * determines if the report may be executed and immediately
     *  displayed in the browser. If not set OpenNMS assumes that
     * the report
     *  must be executed in batch mode.
     */
    private boolean online;

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getDescription()
     */
    @Override
    @XmlAttribute(name = "description")
    public String getDescription() {
        return this.description;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getDisplayName()
     */
    @Override
    @XmlAttribute(name = "display-name")
    public String getDisplayName() {
        return this.displayName;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getId()
     */
    @Override
    @XmlAttribute(name = "id")
    public String getId() {
        return this.id;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getOnline()
     */
    @Override
    @XmlAttribute(name = "online")
    public boolean getOnline() {
        return this.online;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#getReportService()
     */
    @Override
    @XmlAttribute(name = "report-service")
    public String getReportService() {
        return this.reportService;
    }

    @Override
    public String toString() {
        return "Report{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", reportService='" + reportService + '\'' +
                ", description='" + description + '\'' +
                ", online=" + online +
                '}';
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setId(java.lang.String)
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setDisplayName(java.lang.String)
     */
    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setReportService(java.lang.String)
     */
    @Override
    public void setReportService(String reportService) {
        this.reportService = reportService;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.BasicReportDefinition#setOnline(boolean)
     */
    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public String getRepositoryId() {
        return repositoryId;
    }
}
