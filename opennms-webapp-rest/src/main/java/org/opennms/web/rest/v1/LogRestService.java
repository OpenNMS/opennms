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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.stereotype.Component;

@Component
@Path("logs")
public class LogRestService {

    public static final int DEFAULT_NUM_LINES = 5000;

    public static final int MAX_NUM_LINES = 10000;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getLogFiles() {
        return LOGFILES;
    }

    @GET
    @Path("/contents")
    public Response getFileContents(@QueryParam("f") String fileName, @QueryParam("n") Integer numLines) {
        if (!LOGFILES.contains(fileName)) {
            throw new RuntimeException("Unsupported filename: '" + fileName + "'");
        }

        int N = DEFAULT_NUM_LINES;
        if (numLines != null) {
            N = numLines;
        }
        N = Math.min(MAX_NUM_LINES, Math.max(N, 1)); // make sure the value is within [1, MAX_NUM_LINES]

        return logFileContents(Paths.get(System.getProperty("opennms.home"), "logs", fileName), N);
    }

    public static Response logFileContents(final java.nio.file.Path path, int numLastLinesToRead) {
        if (!Files.exists(path)) {
            return Response.noContent().build();
        }
        try {
            final String mimeType = Files.probeContentType(path);

            final StringBuilder sb = new StringBuilder();
            try (ReversedLinesFileReader reader = new ReversedLinesFileReader(path.toFile(), StandardCharsets.UTF_8)) {
                for (int k = 0; k < numLastLinesToRead; k++) {
                    final String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                    sb.append("\n");
                }
            }

            return Response.ok(sb.toString())
                    .type(mimeType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, path.getFileName().toString())
                    .header(HttpHeaders.LAST_MODIFIED, new Date(Files.getLastModifiedTime(path).toMillis()))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final List<String> LOGFILES = Arrays.asList(
            "ackd.log",
            "actiond.log",
            "alarmd.log",
            "bsmd.log",
            "collectd.log",
            "discovery.log",
            "enlinkd.log",
            "event-translator.log",
            "eventd.log",
            "icmp.log",
            "ipc.log",
            "jetty-server.log",
            "karaf.log",
            "karafStartupMonitor.log",
            "manager.log",
            "minion.log",
            "notifd.log",
            "output.log",
            "passive.log",
            "perspectivepollerd.log",
            "poller.log",
            "provisiond.log",
            "queued.log",
            "reportd.log",
            "rtc.log",
            "scriptd.log",
            "statsd.log",
            "telemetryd.log",
            "trapd.log",
            "trouble-ticketer.log",
            "vacuumd.log",
            "web.log"
    ); static {
        LOGFILES.sort(Comparator.naturalOrder());
    }
}
