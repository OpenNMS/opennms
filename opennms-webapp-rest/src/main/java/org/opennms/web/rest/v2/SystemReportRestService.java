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

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.systemreport.SystemReport;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Backs the Generate System Report page: the set of report plugins (data
 * sources) and formatters (output types), and generation of the report itself,
 * streamed back as a file attachment. Replaces the legacy SystemReportController
 * / FormatterView path.
 */
@Component
@Path("system-report")
@Produces(MediaType.APPLICATION_JSON)
public class SystemReportRestService {

    private static final Logger LOG = LoggerFactory.getLogger(SystemReportRestService.class);

    @Autowired
    private SystemReport m_systemReport;

    // SystemReport already filters by isVisible internally; the filter here is a
    // deliberate boundary guard so this public endpoint never leaks a hidden plugin
    // even if that internal contract changes.
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

    /**
     * Generate the report and stream it back as a file attachment. Mirrors the
     * legacy FormatterView: resolve the formatter, run the selected plugins in
     * order, and write to the response. Only stream-producing formatters are
     * downloadable; a non-streaming one (e.g. FTP upload) is rejected here.
     */
    @POST
    @Path("generate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response generate(final GenerateRequest request) {
        if (request == null || request.getFormatter() == null || request.getFormatter().isBlank()) {
            throw badRequest("A formatter is required.");
        }
        final SystemReportFormatter formatter = m_systemReport.getFormatters().stream()
                .filter(SystemReportFormatter::isVisible)
                .filter(f -> f.getName().equals(request.getFormatter()))
                .findFirst()
                .orElseThrow(() -> badRequest("Unknown formatter '" + request.getFormatter() + "'."));
        if (!formatter.needsOutputStream() || formatter.getContentType() == null) {
            throw badRequest("Formatter '" + request.getFormatter() + "' does not produce a downloadable report.");
        }

        final List<String> selected = request.getPlugins() == null ? List.of() : request.getPlugins();
        final List<SystemReportPlugin> plugins = m_systemReport.getPlugins().stream()
                .filter(SystemReportPlugin::isVisible)
                .filter(p -> selected.contains(p.getName()))
                .collect(Collectors.toList());
        if (plugins.isEmpty()) {
            throw badRequest("Select at least one report plugin.");
        }

        final String fileName = fileName(request.getOutput(), formatter.getExtension());
        final StreamingOutput body = output -> {
            try {
                formatter.setOutputStream(output);
                formatter.begin();
                for (final SystemReportPlugin plugin : plugins) {
                    formatter.write(plugin);
                    output.flush();
                }
                formatter.end();
            } catch (final Exception e) {
                // The status line is already committed once streaming starts, so
                // this can only truncate the download; log it for the operator.
                LOG.warn("Error generating system report with formatter '{}'", formatter.getName(), e);
                throw new WebApplicationException(e);
            }
        };

        return Response.ok(body, formatter.getContentType())
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .build();
    }

    // Matches FormatterView.getFileName: use the sanitized basename of the
    // requested name, else a default derived from the formatter's extension.
    private static String fileName(final String output, final String extension) {
        if (output != null && !output.matches("^\\s*$")) {
            return new File(output).getName().replaceAll("[^\\w\\.]", "");
        }
        return "opennms-system-report." + extension;
    }

    private static WebApplicationException badRequest(final String message) {
        return new WebApplicationException(
                Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(message).build());
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

    @XmlRootElement(name = "generate")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class GenerateRequest {
        @XmlElement private String formatter;
        @XmlElement private List<String> plugins;
        @XmlElement private String output;

        public String getFormatter() { return formatter; }
        public void setFormatter(final String formatter) { this.formatter = formatter; }

        public List<String> getPlugins() { return plugins; }
        public void setPlugins(final List<String> plugins) { this.plugins = plugins; }

        public String getOutput() { return output; }
        public void setOutput(final String output) { this.output = output; }
    }

    void setSystemReport(final SystemReport systemReport) {
        m_systemReport = systemReport;
    }
}
