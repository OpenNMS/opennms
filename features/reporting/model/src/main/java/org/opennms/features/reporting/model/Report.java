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
import java.util.Objects;

/**
 * Class Report.
 * 
 * @version $Revision$ $Date$
 */

@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.NONE)
public class Report {

    private static final Boolean DEFAULT_ONLINE = false;

    /**
     * the name of this report as defined in engine
     *  configuration
     */
    @XmlAttribute(name = "id")
    private String id;

    /**
     * the name of this report as displayed in the webui
     *  
     */
    @XmlAttribute(name = "display-name")
    private String displayName;

    /**
     * the name of the engine to use to process and
     *  render this report
     */
    @XmlAttribute(name = "report-service")
    private String reportService;

    /**
     * report description
     */
    @XmlAttribute(name = "description")
    private String description;

    /**
     * determines if the report may be executed and immediately
     *  displayed in the browser. If not set OpenNMS assumes that
     * the report
     *  must be executed in batch mode.
     */
    @XmlAttribute(name = "online")
    private Boolean online;

    /**
     * Returns the value of field 'description'. The field
     * 'description' has the following description: report
     * description
     * 
     * @return the value of field 'Description'.
     */
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
    public Boolean getOnline() {
        return this.online != null ? this.online : DEFAULT_ONLINE;
    }

    /**
     * Returns the value of field 'reportService'. The field
     * 'reportService' has the following description: the name of
     * the engine to use to process and
     *  render this report
     * 
     * @return the value of field 'ReportService'.
     */
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
    public Boolean isOnline() {
        return getOnline();
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

    public void setOnline(Boolean online) {
        this.online = online;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Report)) {
            return false;
        }
        Report castOther = (Report) other;
        return Objects.equals(id, castOther.id) && Objects.equals(displayName, castOther.displayName)
                && Objects.equals(reportService, castOther.reportService)
                && Objects.equals(description, castOther.description) && Objects.equals(online, castOther.online);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, reportService, description, online);
    }
}
