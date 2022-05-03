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
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.opennms.web.api.Authentication;
import org.springframework.stereotype.Component;

@Component
@Path("logs")
public class LogRestService {

    public static final int DEFAULT_NUM_LINES = 5000;

    public static final int MAX_NUM_LINES = 10000;

    private static final String LOG_FILE_EXTENSION = ".log";

    private final java.nio.file.Path logFolder = Paths.get(System.getProperty("opennms.home"), "logs");

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getLogFiles(@Context SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw new ForbiddenException("ADMIN role is required for enumerating log files.");
        }

        try {
            return Files.find(logFolder, 1,
                    (path, basicFileAttributes) -> path.getFileName().toString().endsWith(LOG_FILE_EXTENSION), FileVisitOption.FOLLOW_LINKS)
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to enumerate log files in path: " + logFolder, e);
        }
    }

    @GET
    @Path("/contents")
    public Response getFileContents(@QueryParam("f") String fileName, @QueryParam("n") Integer numLines, @QueryParam("reverse") @DefaultValue("true") boolean reverse, @Context SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw new ForbiddenException("ADMIN role is required for reading log files.");
        }

        // Ensure that the filename ends with the extension
        if (!fileName.endsWith(LOG_FILE_EXTENSION)) {
            throw new BadRequestException("Invalid log file extension access files outside of log folder! Filename given: " + fileName);
        }

        // Ensure that the file is in the given folder
        final java.nio.file.Path logFilePath = logFolder.resolve(fileName);
        if (!Objects.equals(logFolder, logFilePath.getParent())) {
            throw new BadRequestException("Cannot access files outside of log folder! Filename given: " + fileName);
        }

        int N = DEFAULT_NUM_LINES;
        if (numLines != null) {
            N = numLines;
        }
        N = Math.min(MAX_NUM_LINES, Math.max(N, 1)); // make sure the value is within [1, MAX_NUM_LINES]

        return logFileContents(logFilePath, N, reverse);
    }

    public static Response logFileContents(final java.nio.file.Path path, int numLastLinesToRead, boolean reverse) {
        if (!Files.exists(path)) {
            return Response.noContent().build();
        }
        try {
            final String mimeType = Files.probeContentType(path);

            final List<String> lines = new LinkedList<>();
            try (ReversedLinesFileReader reader = new ReversedLinesFileReader(path.toFile(), StandardCharsets.UTF_8)) {
                for (int k = 0; k < numLastLinesToRead; k++) {
                    final String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    lines.add(line);
                }
            }

            final StringBuilder sb = new StringBuilder();
            if (!reverse) {
                // we read in reversed order, so if we don't want the results to be reversed, we reverse again :)
                Collections.reverse(lines);
            }
            for (String line : lines) {
                sb.append(line);
                sb.append("\n");
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

}
