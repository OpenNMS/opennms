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
package org.opennms.web.rest.v2;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.systemreport.SystemReport;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Read-only metadata for the Generate System Report page: the set of report
 * plugins (data sources) and formatters (output types). The report itself is still
 * produced by the streaming SystemReportController; this only drives the form.
 */
@Component
@Path("system-report")
@Produces(MediaType.APPLICATION_JSON)
public class SystemReportRestService {

    @Autowired
    private SystemReport m_systemReport;

    @GET
    @Path("plugins")
    public List<PluginDTO> getPlugins() {
        return m_systemReport.getPlugins().stream()
                .filter(SystemReportPlugin::isVisible)
                .map(PluginDTO::new)
                .collect(Collectors.toList());
    }

    @GET
    @Path("formatters")
    public List<FormatterDTO> getFormatters() {
        return m_systemReport.getFormatters().stream()
                .filter(SystemReportFormatter::isVisible)
                .map(FormatterDTO::new)
                .collect(Collectors.toList());
    }

    @XmlRootElement(name = "plugin")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class PluginDTO {
        @XmlElement private String name;
        @XmlElement private String description;

        public PluginDTO() { }

        public PluginDTO(final SystemReportPlugin plugin) {
            this.name = plugin.getName();
            this.description = plugin.getDescription();
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    @XmlRootElement(name = "formatter")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class FormatterDTO {
        @XmlElement private String name;
        @XmlElement private String description;
        @XmlElement private String extension;

        public FormatterDTO() { }

        public FormatterDTO(final SystemReportFormatter formatter) {
            this.name = formatter.getName();
            this.description = formatter.getDescription();
            this.extension = formatter.getExtension();
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getExtension() { return extension; }
    }

    void setSystemReport(final SystemReport systemReport) {
        m_systemReport = systemReport;
    }
}
