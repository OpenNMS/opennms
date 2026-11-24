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
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
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
 * streamed back as a file attachment.
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

    /** Name of the cookie set when a form-posted report starts streaming, so the page can re-enable its button. */
    static final String DOWNLOAD_COOKIE = "systemReportDownload";

    static final int DOWNLOAD_COOKIE_MAX_AGE = 300;

    private static final Pattern DOWNLOAD_TOKEN = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    // Formatters and plugins are shared singletons from the service registry and
    // keep per-report state, so only one report is generated at a time.
    private final ReentrantLock m_generateLock = new ReentrantLock();

    @GET
    @Path("formatters")
    public List<FormatterDTO> getFormatters() {
        return m_systemReport.getFormatters().stream()
                .filter(SystemReportRestService::isDownloadable)
                .map(FormatterDTO::new)
                .collect(Collectors.toList());
    }

    private static boolean isDownloadable(final SystemReportFormatter formatter) {
        return formatter.isVisible() && formatter.needsOutputStream() && formatter.getContentType() != null;
    }

    /**
     * Generate the report and stream it back as a file attachment: resolve the
     * formatter, run the selected plugins in order, and write to the response.
     */
    @POST
    @Path("generate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response generate(final GenerateRequest request) {
        if (request == null) {
            throw badRequest("A formatter is required.");
        }
        return generate(request.getFormatter(), request.getPlugins(), request.getOutput()).build();
    }

    /**
     * Form-post variant used by the web UI, so the browser streams the download to
     * disk. A valid downloadToken is echoed back in a short-lived cookie once the
     * response starts.
     */
    @POST
    @Path("generate")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response generateFromForm(@FormParam("formatter") final String formatter,
                                     @FormParam("plugins") final List<String> plugins,
                                     @FormParam("output") final String output,
                                     @FormParam("downloadToken") final String downloadToken,
                                     @Context final HttpServletRequest servletRequest) {
        // a form post needs no CORS preflight, so refuse ones a browser marks as coming from another site
        if (servletRequest != null && "cross-site".equalsIgnoreCase(servletRequest.getHeader("Sec-Fetch-Site"))) {
            throw new WebApplicationException(Response.status(Status.FORBIDDEN).type(MediaType.TEXT_PLAIN)
                    .entity("Cross-site report requests are not allowed.").build());
        }
        final Response.ResponseBuilder builder = generate(formatter, plugins, output);
        if (downloadToken != null && DOWNLOAD_TOKEN.matcher(downloadToken).matches()) {
            final String contextPath = servletRequest == null ? "" : servletRequest.getContextPath();
            final boolean secure = servletRequest != null && servletRequest.isSecure();
            // long enough to outlast background-tab timer throttling (about one run a minute in Chrome)
            builder.cookie(new NewCookie(DOWNLOAD_COOKIE, downloadToken,
                    contextPath.isEmpty() ? "/" : contextPath, null, null, DOWNLOAD_COOKIE_MAX_AGE, secure));
        }
        return builder.build();
    }

    private Response.ResponseBuilder generate(final String formatterName, final List<String> pluginNames, final String output) {
        if (formatterName == null || formatterName.isBlank()) {
            throw badRequest("A formatter is required.");
        }
        final SystemReportFormatter formatter = m_systemReport.getFormatters().stream()
                .filter(SystemReportFormatter::isVisible)
                .filter(f -> f.getName().equals(formatterName))
                .findFirst()
                .orElseThrow(() -> badRequest("Unknown formatter '" + formatterName + "'."));
        if (!isDownloadable(formatter)) {
            throw badRequest("Formatter '" + formatterName + "' does not produce a downloadable report.");
        }

        final List<String> selected = pluginNames == null ? List.of() : pluginNames;
        final List<SystemReportPlugin> plugins = m_systemReport.getPlugins().stream()
                .filter(SystemReportPlugin::isVisible)
                .filter(p -> selected.contains(p.getName()))
                .collect(Collectors.toList());
        if (plugins.isEmpty()) {
            throw badRequest("Select at least one report plugin.");
        }

        final String fileName = fileName(output, formatter.getExtension());
        disableResponseCompression();
        final StreamingOutput body = out -> {
            if (!m_generateLock.tryLock()) {
                // nothing has been written yet, so this still becomes a plain 503
                throw new WebApplicationException(Response.status(Status.SERVICE_UNAVAILABLE).type(MediaType.TEXT_PLAIN)
                        .entity("Another system report is being generated. Try again once it finishes.").build());
            }
            try {
                formatter.setOutputStream(out);
                formatter.begin();
                // commit the headers now, so the browser hands the response to its download
                // manager and leaving the page no longer cancels it
                out.flush();
                for (final SystemReportPlugin plugin : plugins) {
                    formatter.write(plugin);
                    out.flush();
                }
                formatter.end();
            } catch (final Exception e) {
                // The status line is already committed once streaming starts, so
                // this can only truncate the download; log it for the operator.
                LOG.warn("Error generating system report with formatter '{}'", formatter.getName(), e);
                throw new WebApplicationException(e);
            } finally {
                formatter.setOutputStream(null);
                m_generateLock.unlock();
            }
        };

        return Response.ok(body, formatter.getContentType())
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    }

    // CXF's gzip interceptor holds back the first ~1 KB, and with it the headers, which defeats
    // the early flush; it only skips a response whose request had no Accept-Encoding.
    // The zip is already compressed, so nothing is lost.
    private static void disableResponseCompression() {
        final Message message = PhaseInterceptorChain.getCurrentMessage();
        if (message == null || message.getExchange() == null || message.getExchange().getInMessage() == null) {
            return;
        }
        final Map<String, List<String>> headers = CastUtils.cast((Map<?, ?>) message.getExchange().getInMessage().get(Message.PROTOCOL_HEADERS));
        if (headers != null) {
            headers.keySet().removeIf(HttpHeaders.ACCEPT_ENCODING::equalsIgnoreCase);
        }
    }

    // The sanitized basename of the requested name, else a default from the formatter's extension.
    static String fileName(final String output, final String extension) {
        final String cleaned = output == null ? "" : new File(output.trim()).getName().replaceAll("[^\\w\\.]", "");
        return cleaned.isEmpty() || cleaned.matches("^\\.+$") ? "opennms-system-report." + extension : cleaned;
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
