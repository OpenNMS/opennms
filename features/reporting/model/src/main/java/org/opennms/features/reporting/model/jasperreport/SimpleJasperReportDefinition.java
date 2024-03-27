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
package org.opennms.features.reporting.model.jasperreport;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;

@XmlRootElement(name = "simple-jasper-report")
public class SimpleJasperReportDefinition implements BasicReportDefinition,
        JasperReportDefinition {

    private String m_id;
    private String m_repositoryId;
    private String m_engine;
    private String m_template;
    private String m_description;
    private String m_displayName;
    private String m_reportService;
    private boolean m_online;
    private boolean m_allowAccess;
    
    @XmlElement(name = "description")
    @Override
    public String getDescription() {
        return m_description;
    }
    @XmlElement(name = "display-name")
    @Override
    public String getDisplayName() {
        return m_displayName;
    }
    @XmlElement(name = "engine")
    @Override
    public String getEngine() {
        return m_engine;
    }
    @XmlElement(name = "id")
    @Override
    public String getId() {
        return m_id;
    }
    @XmlElement(name = "online")
    @Override
    public boolean getOnline() {
        return m_online;
    }
    @XmlElement(name = "report-service")
    @Override
    public String getReportService() {
        return m_reportService;
    }
    @Override
    public String getRepositoryId() {
        return m_repositoryId;
    }
    @XmlElement(name = "template")
    @Override
    public String getTemplate() {
        return m_template;
    }
    @Override
    public void setDescription(String description) {
        m_description = description;
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
    public void setDisplayName(String displayName) {
        m_displayName = displayName;
    }
    @Override
    public void setEngine(String engine) {
        m_engine = engine;
    }
    @Override
    public void setId(String id) {
        m_id = id;
    }
    @Override
    public void setOnline(boolean online) {
        m_online = online;
    }
    @Override
    public void setReportService(String reportService) {
        m_reportService = reportService;
    }
    @Override
    public void setTemplate(String template) {
        m_template = template;
    }

    @Override
    public String toString() {
        return "SimpleJasperReportDefinition [m_id=" + m_id
                + ", m_repositoryId=" + m_repositoryId + ", m_engine="
                + m_engine + ", m_template=" + m_template
                + ", m_description=" + m_description + ", m_displayName="
                + m_displayName + ", m_reportService=" + m_reportService
                + ", m_online=" + m_online + "]";
    }
}
