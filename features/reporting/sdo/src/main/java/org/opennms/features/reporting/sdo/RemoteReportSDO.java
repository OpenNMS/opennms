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
package org.opennms.features.reporting.sdo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "remoteReportSDO")
public class RemoteReportSDO {

    private String m_id;
    private String m_engine;
    private String m_template;
    private String m_description;
    private String m_displayName;
    private String m_reportService;
    private boolean m_allowAccess;
    private boolean m_online;
    private boolean m_subreport;

    @XmlElement(name = "description")
    public String getDescription() {
        return m_description;
    }

    @XmlElement(name = "display-name")
    public String getDisplayName() {
        return m_displayName;
    }

    @XmlElement(name = "engine")
    public String getEngine() {
        return m_engine;
    }

    @XmlElement(name = "id")
    public String getId() {
        return m_id;
    }

    @XmlElement(name = "report-service")
    public String getReportService() {
        return m_reportService;
    }

    @XmlElement(name = "template")
    public String getTemplate() {
        return m_template;
    }

    @XmlElement(name = "online")
    public boolean getOnline() {
        return m_online;
    }

    @XmlElement(name = "subreport")
    public boolean getSubreport() {
        return m_subreport;
    }

    @XmlElement(name = "allow-access")
    public boolean getAllowAccess() {
        return m_allowAccess;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public void setDisplayName(String displayName) {
        m_displayName = displayName;
    }

    public void setEngine(String engine) {
        m_engine = engine;
    }

    public void setId(String id) {
        m_id = id;
    }

    public void setOnline(boolean online) {
        m_online = online;
    }

    public void setSubreport(boolean subreport) {
        m_subreport = subreport;
    }
    
    public void setAllowAccess(boolean allowAccess) {
        m_allowAccess = allowAccess;
    }

    public void setReportService(String reportService) {
        m_reportService = reportService;
    }

    public void setTemplate(String template) {
        m_template = template;
    }

    @Override
    public String toString() {
        return "RemoteReportSDO [m_id=" + m_id + ", m_engine=" + m_engine
                + ", m_template=" + m_template + ", m_description="
                + m_description + ", m_displayName=" + m_displayName
                + ", m_reportService=" + m_reportService + ", m_allowAccess="
                + m_allowAccess + ", m_online=" + m_online + "]";
    }
}
