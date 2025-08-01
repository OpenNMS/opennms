/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
