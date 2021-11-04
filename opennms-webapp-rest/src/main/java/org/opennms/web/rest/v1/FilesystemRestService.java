/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.opennms.core.daemon.DaemonReloadEnum;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

@Component
@Path("filesystem")
public class FilesystemRestService {

    @Autowired
    private EventIpcManager eventForwarder;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getFiles() {
        return FILES;
    }

    @GET
    @Path("/help")
    @Produces("text/markdown")
    public InputStream getFileHelp(@QueryParam("f") String fileName) throws IOException {
        if (!FILES.contains(fileName)) {
            throw new RuntimeException("Unsupported filename: '" + fileName + "'");
        }
        return this.getClass().getResourceAsStream("/help/" + fileName + ".md");
    }

    @GET
    @Path("/contents")
    public Response getFileContents(@QueryParam("f") String fileName) throws IOException {
        if (!FILES.contains(fileName)) {
            throw new RuntimeException("Unsupported filename: '" + fileName + "'");
        }
        final File file = Paths.get(System.getProperty("opennms.home"), "etc", fileName).toFile();
        final String mimeType = Files.probeContentType(file.toPath());
        return streamAll(file, mimeType, fileName);
    }

    @POST
    @Path("/contents")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String uploadFile(@QueryParam("f") String fileName,
                             @Multipart("upload") Attachment attachment) throws IOException {
        if (!FILES.contains(fileName)) {
            throw new RuntimeException("Unsupported filename: '" + fileName + "'");
        }

        // Write the contents a temporary file
        final File tempFile = File.createTempFile("upload-",fileName);
        try {
            tempFile.deleteOnExit();
            final InputStream in = attachment.getObject(InputStream.class);
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Validate it
            maybeValidateXml(tempFile);

            // Copy it to the right place
            final java.nio.file.Path targetPath = Paths.get(System.getProperty("opennms.home"), "etc", fileName);
            Files.copy(tempFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Reload the associated daemon
            boolean didReloadDaemon = maybeReloadDaemon(fileName);

            // Build our message
            String message = String.format("Successfully wrote to '%s'.", targetPath);
            if (didReloadDaemon) {
                message += " The associated daemon was also reloaded.";
            }

            return message;
        } finally {
            // Delete the temporary file
            tempFile.delete();
        }
    }

    public static Response streamAll(final File file, String mimeType, String fileName) {
        return Response.ok(file)
                .type(mimeType)
                .header(HttpHeaders.CONTENT_DISPOSITION, fileName)
                .header(HttpHeaders.CONTENT_LENGTH, file.length())
                .header(HttpHeaders.LAST_MODIFIED, new Date(file.lastModified()))
                .build();
    }

    private boolean maybeReloadDaemon(String fileName) {
        if (POLLER_CONFIGURATION_XML.equals(fileName)) {
            EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, FilesystemRestService.class.getName());
            eventBuilder.addParam(EventConstants.PARM_DAEMON_NAME, DaemonReloadEnum.POLLERD.getDaemonName());
            eventForwarder.sendNow(eventBuilder.getEvent());
            return true;
        }
        return false;
    }

    private void maybeValidateXml(File file) {
        if (!file.getName().endsWith(".xml")) {
            return;
        }

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        final CapturingErrorHandler errorHandler = new CapturingErrorHandler();
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(errorHandler);
            builder.parse(file);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Validation failed: " + e.getMessage()).build());
        }
    }

    public static class CapturingErrorHandler implements ErrorHandler {
        final StringBuilder sb = new StringBuilder();

        public void warning(SAXParseException e) {
            sb.append("WARNING: ");
            sb.append(e.getMessage());
            sb.append("\n");
        }

        public void error(SAXParseException e) {
            sb.append("ERROR: ");
            sb.append(e.getMessage());
            sb.append("\n");
        }

        public void fatalError(SAXParseException e) {
            sb.append("FATAL ERROR: ");
            sb.append(e.getMessage());
            sb.append("\n");
        }

        public String toString() {
            return sb.toString();
        }
    }

    public static final String POLLER_CONFIGURATION_XML = "poller-configuration.xml";

    private static final List<String> FILES = Arrays.asList(
            "availability-reports.xml",
            "bsf-northbounder-configuration.xml",
            "collectd-configuration.xml",
            "discovery-configuration.xml",
            "drools-northbounder-configuration.xml",
            "http-datacollection-config.xml",
            "jdbc-datacollection-config.xml",
            "log4j2.xml",
            POLLER_CONFIGURATION_XML,
            "provisiond-configuration.xml",
            "rancid-configuration.xml",
            "snmp-asset-adapter-configuration.xml",
            "statsd-configuration.xml",
            "syslogd-configuration.xml",
            "telemetryd-configuration.xml",
            "trend-configuration.xml",
            "viewsdisplay.xml",
            "wmi-config.xml",
            "wsman-asset-adapter-configuration.xml",
            "wsman-config.xml",
            "snmp-config.xml",
            "notifd-configuration.xml",
            "snmptrap-northbounder-configuration.xml",
            "elastic-credentials.xml",
            "ksc-performance-reports.xml",
            "ifttt-config.xml",
            "rtc-configuration.xml",
            "enlinkd-configuration.xml",
            "microblog-configuration.xml",
            "snmp-metadata-adapter-configuration.xml",
            "datacollection-config.xml",
            "javamail-configuration.xml",
            "threshd-configuration.xml",
            "ackd-configuration.xml",
            "tl1d-configuration.xml",
            "log4j2-tools.xml",
            "eventd-configuration.xml",
            "surveillance-views.xml",
            "opennms-activemq.xml",
            "nsclient-config.xml",
            "trapd-configuration.xml",
            "vmware-datacollection-config.xml",
            "nsclient-datacollection-config.xml",
            "jmx-datacollection-config.xml",
            "users.xml",
            "destinationPaths.xml",
            "snmp-hardware-inventory-adapter-configuration.xml",
            "prometheus-datacollection.d/node-exporter.xml",
            "xmp-datacollection-config.xml",
            "thresholds.xml",
            "translator-configuration.xml",
            "vacuumd-configuration.xml",
            "eventconf.xml",
            "site-status-views.xml",
            "email-northbounder-configuration.xml",
            "notifications.xml",
            "xml-datacollection-config.xml",
            "remote-repository.xml",
            "jmx-config.xml",
            "vmware-config.xml",
            "ami-config.xml",
            "syslog-northbounder-configuration.xml",
            "service-configuration.xml",
            "notificationCommands.xml",
            "actiond-configuration.xml",
            "prometheus-datacollection-config.xml",
            "vmware-cim-datacollection-config.xml",
            "jasper-reports.xml",
            "scriptd-configuration.xml",
            "reportd-configuration.xml",
            "tca-datacollection-config.xml",
            "categories.xml",
            "database-reports.xml",
            "wsman-datacollection-config.xml",
            "poll-outages.xml",
            "rws-configuration.xml",
            "snmp-interface-poller-configuration.xml",
            "wmi-datacollection-config.xml",
            "chart-configuration.xml",
            "search-actions.xml",
            "xmp-config.xml",
            // non-xml
            "opennms.properties",
            "javamail-configuration.properties",
            "org.opennms.features.topology.app.cfg"
    ); static {
        FILES.sort(Comparator.naturalOrder());
    }
}
