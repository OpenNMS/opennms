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

import java.io.IOException;
import java.io.Reader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.opennms.core.utils.IPLike;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.AbstractMergingJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.wsman.WsmanDatacollectionConfig;
import org.opennms.netmgt.config.wsman.credentials.Definition;
import org.opennms.netmgt.config.wsman.credentials.Range;
import org.opennms.netmgt.config.wsman.credentials.WsmanConfig;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.dao.WSManDataCollectionConfigDao;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.model.WsmanConfigDto;
import org.opennms.web.rest.v2.model.WsmanConfigUpdate;
import org.opennms.web.rest.v2.model.WsmanConfigUpdate.DefinitionUpdate;
import org.opennms.web.rest.v2.model.WsmanConfigUpdate.RangeUpdate;
import org.opennms.web.rest.v2.model.WsmanConfigUpdate.SettingsUpdate;
import org.opennms.web.rest.v2.model.WsmanDataCollectionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Reads and rewrites wsman-config.xml (agent defaults and per-address
 * definitions) for the Manage WS-Man page. The file carries agent
 * credentials, so every method is admin-only and passwords are never
 * returned; an update keeps a stored password unless it is replaced or
 * explicitly cleared. The DAO's file-reload container picks the rewritten
 * file up on its next access, so the daemons see the change without a
 * restart.
 */
@Component
@javax.ws.rs.Path("wsman-config")
@Tag(name = "WsmanConfig", description = "WS-Man agent configuration API")
public class WsmanConfigRestService {

    private static final int MAX_PORT = 65535;

    // relative to opennms.home, as WSManDataCollectionConfigDaoJaxb declares them
    private static final Path DATA_COLLECTION_ROOT = Paths.get("etc", "wsman-datacollection-config.xml");
    private static final Path DATA_COLLECTION_DIR = Paths.get("etc", "wsman-datacollection.d");

    @Autowired
    private WSManConfigDao wsManConfigDao;

    @Autowired
    private WSManDataCollectionConfigDao wsManDataCollectionConfigDao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get the WS-Man agent configuration", description = "Agent defaults and definitions from wsman-config.xml; passwords are reported as present or absent only", operationId = "WsmanConfigRestServiceGetConfig")
    public Response getConfig(@Context final SecurityContext securityContext) {
        requireAdmin(securityContext);
        return Response.ok(WsmanConfigDto.from(readConfig())).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Replace the WS-Man agent configuration", description = "Rewrites wsman-config.xml from the given defaults and definitions. A null password keeps the stored one; clearPassword removes it; a definition's sourceIndex carries its stored password over.", operationId = "WsmanConfigRestServiceUpdateConfig")
    public Response updateConfig(@Context final SecurityContext securityContext, final WsmanConfigUpdate update) {
        requireAdmin(securityContext);
        if (update == null || update.getDefaults() == null) {
            throw badRequest("The configuration and its defaults are required.");
        }
        synchronized (this) {
            final WsmanConfig current = readConfig();
            final WsmanConfig next = new WsmanConfig();
            applyDefaults(next, update.getDefaults(), current.getPassword());
            validateSettings(update.getDefaults(), "The defaults");

            final List<Definition> existing = current.getDefinition();
            final Set<Integer> usedSources = new HashSet<>();
            int position = 1;
            for (final DefinitionUpdate du : update.getDefinitions()) {
                final String label = "Definition " + position;
                String keptPassword = null;
                if (du.getSourceIndex() != null) {
                    final int i = du.getSourceIndex();
                    if (i < 0 || i >= existing.size()) {
                        throw badRequest(label + " refers to a stored definition (" + i + ") that does not exist; reload the page and try again.");
                    }
                    if (!usedSources.add(i)) {
                        throw badRequest(label + " refers to the same stored definition as another entry.");
                    }
                    keptPassword = existing.get(i).getPassword();
                }
                validateSettings(du, label);
                next.getDefinition().add(toDefinition(du, keptPassword, label));
                position++;
            }
            writeConfig(next);
            return Response.ok(WsmanConfigDto.from(readConfig())).build();
        }
    }

    @GET
    @javax.ws.rs.Path("data-collection")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get the WS-Man data collection configuration", description = "Collections, groups and system definitions from wsman-datacollection-config.xml and wsman-datacollection.d, each tagged with its source file", operationId = "WsmanConfigRestServiceGetDataCollection")
    public Response getDataCollection(@Context final SecurityContext securityContext) {
        requireAdmin(securityContext);
        final WsmanDataCollectionDto dto = new WsmanDataCollectionDto();
        for (final Path file : dataCollectionFiles()) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                dto.addSource(file.getFileName().toString(), JaxbUtils.unmarshal(WsmanDatacollectionConfig.class, reader));
            } catch (final IOException e) {
                throw new WebApplicationException("Unable to read " + file + ": " + e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
            }
        }
        return Response.ok(dto).build();
    }

    // The same file set, in the same order, that the merging DAO folds
    // together: the root file first, then the drop-ins sorted by name.
    private List<Path> dataCollectionFiles() {
        final Path home = wsManDataCollectionConfigDao instanceof AbstractMergingJaxbConfigDao
                ? ((AbstractMergingJaxbConfigDao<?, ?>) wsManDataCollectionConfigDao).getOpennmsHome()
                : Paths.get(System.getProperty("opennms.home"));
        final List<Path> files = new ArrayList<>();
        final Path root = home.resolve(DATA_COLLECTION_ROOT);
        if (Files.isReadable(root)) {
            files.add(root);
        }
        final Path dir = home.resolve(DATA_COLLECTION_DIR);
        if (Files.isDirectory(dir)) {
            try (Stream<Path> stream = Files.list(dir)) {
                stream.filter(Files::isRegularFile)
                        .filter(Files::isReadable)
                        .filter(f -> f.getFileName().toString().endsWith(".xml"))
                        .sorted()
                        .forEach(files::add);
            } catch (final IOException e) {
                throw new WebApplicationException("Unable to list " + dir + ": " + e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
            }
        }
        return files;
    }

    private static void requireAdmin(final SecurityContext securityContext) {
        if (securityContext == null || !securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw new WebApplicationException(Response.status(Status.FORBIDDEN).build());
        }
    }

    private static WebApplicationException badRequest(final String message) {
        return new WebApplicationException(Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(message).build());
    }

    // --- file access -------------------------------------------------------

    private Path configFile() {
        if (wsManConfigDao instanceof AbstractJaxbConfigDao) {
            try {
                return ((AbstractJaxbConfigDao<?, ?>) wsManConfigDao).getConfigResource().getFile().toPath();
            } catch (final IOException e) {
                // not a file resource; fall through to the conventional location
            }
        }
        return Paths.get(System.getProperty("opennms.home"), "etc", "wsman-config.xml");
    }

    private WsmanConfig readConfig() {
        final Path file = configFile();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JaxbUtils.unmarshal(WsmanConfig.class, reader);
        } catch (final IOException e) {
            throw new WebApplicationException("Unable to read " + file + ": " + e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }
    }

    // Marshal first, then replace the file atomically, so a marshal failure or
    // a crash mid-write can never leave a truncated wsman-config.xml behind.
    private void writeConfig(final WsmanConfig config) {
        final Path file = configFile();
        final String xml = JaxbUtils.marshal(config);
        final Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.write(temp, xml.getBytes(StandardCharsets.UTF_8));
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (final IOException e) {
            throw new WebApplicationException("Unable to write " + file + ": " + e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }
    }

    // --- mapping -----------------------------------------------------------

    private static String resolvePassword(final SettingsUpdate update, final String kept) {
        if (update.isClearPassword()) {
            return null;
        }
        if (update.getPassword() != null && !update.getPassword().isEmpty()) {
            return update.getPassword();
        }
        return kept;
    }

    private static void applyDefaults(final WsmanConfig target, final SettingsUpdate u, final String keptPassword) {
        target.setRetry(u.getRetry());
        target.setTimeout(u.getTimeout());
        target.setUsername(blankToNull(u.getUsername()));
        target.setPassword(resolvePassword(u, keptPassword));
        target.setPort(u.getPort());
        target.setMaxElements(u.getMaxElements());
        target.setSsl(u.getSsl());
        target.setStrictSsl(u.getStrictSsl());
        target.setPath(blankToNull(u.getPath()));
        target.setProductVendor(blankToNull(u.getProductVendor()));
        target.setProductVersion(blankToNull(u.getProductVersion()));
        target.setGssAuth(u.getGssAuth());
    }

    private static Definition toDefinition(final DefinitionUpdate u, final String keptPassword, final String label) {
        final Definition d = new Definition();
        d.setRetry(u.getRetry());
        d.setTimeout(u.getTimeout());
        d.setUsername(blankToNull(u.getUsername()));
        d.setPassword(resolvePassword(u, keptPassword));
        d.setPort(u.getPort());
        d.setMaxElements(u.getMaxElements());
        d.setSsl(u.getSsl());
        d.setStrictSsl(u.getStrictSsl());
        d.setPath(blankToNull(u.getPath()));
        d.setProductVendor(blankToNull(u.getProductVendor()));
        d.setProductVersion(blankToNull(u.getProductVersion()));
        d.setGssAuth(u.getGssAuth());

        for (final RangeUpdate r : u.getRanges()) {
            final InetAddress begin = parseAddress(r.getBegin(), label + " has a range with an invalid begin address");
            final InetAddress end = parseAddress(r.getEnd(), label + " has a range with an invalid end address");
            if (begin.getAddress().length != end.getAddress().length) {
                throw badRequest(label + " has a range mixing IPv4 and IPv6 addresses.");
            }
            if (InetAddressUtils.toInteger(begin).compareTo(InetAddressUtils.toInteger(end)) > 0) {
                throw badRequest(label + " has a range whose end address is before its begin address.");
            }
            final Range range = new Range();
            range.setBegin(InetAddressUtils.str(begin));
            range.setEnd(InetAddressUtils.str(end));
            d.getRange().add(range);
        }
        for (final String specific : u.getSpecifics()) {
            d.getSpecific().add(InetAddressUtils.str(parseAddress(specific, label + " has an invalid specific address")));
        }
        for (final String ipMatch : u.getIpMatches()) {
            validateIpLike(ipMatch, label);
            d.getIpMatch().add(ipMatch.trim());
        }
        if (d.getRange().isEmpty() && d.getSpecific().isEmpty() && d.getIpMatch().isEmpty()) {
            throw badRequest(label + " must match at least one range, address, or IPLIKE pattern; otherwise it never applies.");
        }
        return d;
    }

    // --- validation --------------------------------------------------------

    private static void validateSettings(final SettingsUpdate u, final String what) {
        if (u.getRetry() != null && u.getRetry() < 0) {
            throw badRequest(what + ": retries must be 0 or more.");
        }
        if (u.getTimeout() != null && u.getTimeout() < 1) {
            throw badRequest(what + ": the timeout must be at least 1 millisecond.");
        }
        if (u.getPort() != null && (u.getPort() < 1 || u.getPort() > MAX_PORT)) {
            throw badRequest(what + ": the port must be between 1 and " + MAX_PORT + ".");
        }
        if (u.getMaxElements() != null && u.getMaxElements() < 1) {
            throw badRequest(what + ": max elements must be at least 1.");
        }
        final String path = u.getPath();
        if (path != null && !path.isBlank() && (path.chars().anyMatch(Character::isWhitespace) || !path.trim().startsWith("/"))) {
            throw badRequest(what + ": the path must start with / and contain no whitespace.");
        }
        for (final String s : new String[] { u.getUsername(), u.getPassword(), u.getProductVendor(), u.getProductVersion() }) {
            if (s != null && s.chars().anyMatch(Character::isISOControl)) {
                throw badRequest(what + ": values must not contain control characters.");
            }
        }
    }

    private static InetAddress parseAddress(final String value, final String problem) {
        if (value == null || value.isBlank()) {
            throw badRequest(problem + " (empty).");
        }
        try {
            return InetAddressUtils.addr(value.trim());
        } catch (final RuntimeException e) {
            throw badRequest(problem + ": " + value.trim());
        }
    }

    // IPLike only reports a malformed pattern while matching, so probe it with
    // an address of the pattern's own family.
    private static void validateIpLike(final String pattern, final String label) {
        if (pattern == null || pattern.isBlank()) {
            throw badRequest(label + " has an empty IPLIKE pattern.");
        }
        final String probe = pattern.contains(":") ? "::1" : "127.0.0.1";
        try {
            IPLike.matches(probe, pattern.trim());
        } catch (final RuntimeException e) {
            throw badRequest(label + " has an invalid IPLIKE pattern: " + pattern.trim());
        }
    }

    private static String blankToNull(final String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
