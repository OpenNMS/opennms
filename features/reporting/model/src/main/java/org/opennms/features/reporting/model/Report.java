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

package org.opennms.features.reporting.model;

import javax.xml.bind.annotation.*;

/**
 * Class Report.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name = "report")
public class Report {

    /**
     * the name of this report as defined in engine
     *  configuration
     */
    private String id;

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

    /**
     * Returns the value of field 'description'. The field
     * 'description' has the following description: report
     * description
     * 
     * @return the value of field 'Description'.
     */
    @XmlAttribute(name = "description")
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the value of field 'displayName'. The field
     * 'displayName' has the following description: the name of
     * this report as displayed in the webui
     *  
     * 
     * @return the value of field 'DisplayName'.
     */
    @XmlAttribute(name = "display-name")
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Returns the value of field 'id'. The field 'id' has the
     * following description: the name of this report as defined in
     * engine
     *  configuration
     * 
     * @return the value of field 'Id'.
     */
    @XmlAttribute(name = "id")
    public String getId() {
        return this.id;
    }

    /**
     * Returns the value of field 'online'. The field 'online' has
     * the following description: determines if the report may be
     * executed and immediately
     *  displayed in the browser. If not set OpenNMS assumes that
     * the report
     *  must be executed in batch mode.
     * 
     * @return the value of field 'Online'.
     */
    @XmlAttribute(name = "online")
    public boolean getOnline() {
        return this.online;
    }

    /**
     * Returns the value of field 'reportService'. The field
     * 'reportService' has the following description: the name of
     * the engine to use to process and
     *  render this report
     * 
     * @return the value of field 'ReportService'.
     */
    @XmlAttribute(name = "report-service")
    public String getReportService() {
        return this.reportService;
    }

    /**
     * Returns the value of field 'online'. The field 'online' has
     * the following description: determines if the report may be
     * executed and immediately
     *  displayed in the browser. If not set OpenNMS assumes that
     * the report
     *  must be executed in batch mode.
     * 
     * @return the value of field 'Online'.
     */
    public boolean isOnline() {
        return this.online;
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

    public void setId(String id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setReportService(String reportService) {
        this.reportService = reportService;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
