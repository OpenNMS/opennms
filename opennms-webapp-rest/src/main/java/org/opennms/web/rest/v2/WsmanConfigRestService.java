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
import java.io.StringReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
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

import com.google.common.net.InetAddresses;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Reads and rewrites wsman-config.xml (agent defaults and per-address
 * definitions) for the Manage WS-Man page. The file carries agent
 * credentials, so every method is admin-only and passwords are never
 * returned; an update keeps a stored password unless it is replaced or
 * explicitly cleared, and must present the version it was built from so a
 * stale page cannot overwrite another admin's change. The DAO's file-reload
 * container picks the rewritten file up on its next access, so the daemons
 * see the change without a restart.
 */
@Component
@javax.ws.rs.Path("wsman-config")
@Tag(name = "WsmanConfig", description = "WS-Man agent configuration API")
public class WsmanConfigRestService {

    private static final int MAX_PORT = 65535;

    // wsman-config.xsd's ip-match grammar: four dotted fields, each * or a
    // comma list of numbers and a-b ranges. IPv6 patterns are not allowed.
    private static final String IPLIKE_FIELD = "(\\*|[0-9]{1,3}((,|-)[0-9]{1,3})*)";
    private static final Pattern IPLIKE_V4 = Pattern.compile("^" + IPLIKE_FIELD + "(\\." + IPLIKE_FIELD + "){3}$");

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
        return Response.ok(toDto(readConfig())).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Replace the WS-Man agent configuration", description = "Rewrites wsman-config.xml from the given defaults and definitions; the request must carry the version returned by GET and is refused with 409 if the file changed since. A null password keeps the stored one; clearPassword removes it; a definition's sourceIndex carries its stored password over.", operationId = "WsmanConfigRestServiceUpdateConfig")
    public Response updateConfig(@Context final SecurityContext securityContext, final WsmanConfigUpdate update) {
        requireAdmin(securityContext);
        if (update == null || update.getDefaults() == null) {
            throw badRequest("The configuration and its defaults are required.");
        }
        if (update.getDefinitions() == null) {
            throw badRequest("The definitions list is required; send an empty list to remove every definition.");
        }
        if (update.getVersion() == null || update.getVersion().isBlank()) {
            throw badRequest("The version returned by GET is required.");
        }
        synchronized (this) {
            final Loaded loaded = readConfig();
            if (!loaded.version.equals(update.getVersion())) {
                throw new WebApplicationException(Response.status(Status.CONFLICT).type(MediaType.TEXT_PLAIN)
                        .entity("The WS-Man configuration changed since it was loaded; reload the page and apply the change again.").build());
            }
            final WsmanConfig current = loaded.config;
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
            return Response.ok(toDto(readConfig())).build();
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
            } catch (final IOException | RuntimeException e) {
                // a hand-edited drop-in that no longer parses must name itself, not surface as a bare 500
                throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
                        .entity("Unable to read " + file.getFileName() + ": " + e.getMessage()).build());
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

    private static final class Loaded {
        private final WsmanConfig config;
        private final String version;

        private Loaded(final WsmanConfig config, final String version) {
            this.config = config;
            this.version = version;
        }
    }

    private Loaded readConfig() {
        final Path file = configFile();
        try {
            final byte[] bytes = Files.readAllBytes(file);
            try (Reader reader = new StringReader(new String(bytes, StandardCharsets.UTF_8))) {
                return new Loaded(JaxbUtils.unmarshal(WsmanConfig.class, reader), digest(bytes));
            }
        } catch (final IOException e) {
            throw new WebApplicationException("Unable to read " + file + ": " + e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }
    }

    private static WsmanConfigDto toDto(final Loaded loaded) {
        final WsmanConfigDto dto = WsmanConfigDto.from(loaded.config);
        dto.setVersion(loaded.version);
        return dto;
    }

    private static String digest(final byte[] bytes) {
        try {
            final StringBuilder hex = new StringBuilder();
            for (final byte b : MessageDigest.getInstance("SHA-256").digest(bytes)) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // Marshal (and schema-validate) first, then overwrite the existing file in
    // place like the other config writers, so its mode, ownership and any
    // symlink survive; a marshal failure never reaches the file.
    private void writeConfig(final WsmanConfig config) {
        final Path file = configFile();
        final String xml;
        try {
            // JaxbUtils marshals a fragment; keep the declaration the shipped file carries
            xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + JaxbUtils.marshal(config);
        } catch (final RuntimeException e) {
            throw badRequest("The configuration does not satisfy wsman-config.xsd: " + rootMessage(e));
        }
        try {
            Files.write(file, xml.getBytes(StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new WebApplicationException("Unable to write " + file + ": " + e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }
    }

    private static String rootMessage(final Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
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
            // canonical literal text (IPv6 compressed), never the resolved form
            final Range range = new Range();
            range.setBegin(InetAddresses.toAddrString(begin));
            range.setEnd(InetAddresses.toAddrString(end));
            d.getRange().add(range);
        }
        for (final String specific : u.getSpecifics()) {
            d.getSpecific().add(InetAddresses.toAddrString(parseAddress(specific, label + " has an invalid specific address")));
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
        if (u.isClearPassword() && u.getPassword() != null && !u.getPassword().isEmpty()) {
            throw badRequest(what + ": send a new password or clear the stored one, not both.");
        }
        // only what the daemon itself cannot use: WSManEndpoint rejects negative
        // timeouts, a port outside the TCP range makes an unusable URL, and the
        // endpoint builder prepends the '/' a path may lack
        if (u.getRetry() != null && u.getRetry() < 0) {
            throw badRequest(what + ": retries must be 0 or more.");
        }
        if (u.getTimeout() != null && u.getTimeout() < 0) {
            throw badRequest(what + ": the timeout must be 0 or more milliseconds.");
        }
        if (u.getPort() != null && (u.getPort() < 1 || u.getPort() > MAX_PORT)) {
            throw badRequest(what + ": the port must be between 1 and " + MAX_PORT + ".");
        }
        if (u.getMaxElements() != null && u.getMaxElements() < 0) {
            throw badRequest(what + ": max elements must be 0 or more.");
        }
        final String path = u.getPath();
        if (path != null && !path.isBlank() && path.chars().anyMatch(Character::isWhitespace)) {
            throw badRequest(what + ": the path must not contain whitespace.");
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
        // literal only: InetAddressUtils.addr would resolve a host name through DNS
        final String literal = value.trim();
        if (!InetAddresses.isInetAddress(literal)) {
            throw badRequest(problem + ": " + literal);
        }
        return InetAddresses.forString(literal);
    }

    // The schema admits only the IPv4 grammar, and IPLike silently never
    // matches a reversed range, a multi-dash field or an octet above 255, so
    // those are rejected here rather than stored as a criterion that is dead.
    private static void validateIpLike(final String pattern, final String label) {
        if (pattern == null || pattern.isBlank()) {
            throw badRequest(label + " has an empty IPLIKE pattern.");
        }
        final String p = pattern.trim();
        if (p.contains(":")) {
            throw badRequest(label + " has an IPv6 IPLIKE pattern; wsman-config.xml only allows IPv4 patterns (use a range or address for IPv6).");
        }
        if (!IPLIKE_V4.matcher(p).matches()) {
            throw badRequest(label + " has an invalid IPLIKE pattern: " + p + " (use four fields of *, a number, a-b, or a comma list).");
        }
        for (final String field : p.split("\\.")) {
            if ("*".equals(field)) {
                continue;
            }
            for (final String part : field.split(",")) {
                final String[] bounds = part.split("-");
                if (bounds.length > 2) {
                    throw badRequest(label + " has an IPLIKE field with more than one range: " + field);
                }
                final int low = Integer.parseInt(bounds[0]);
                final int high = Integer.parseInt(bounds[bounds.length - 1]);
                if (low > 255 || high > 255) {
                    throw badRequest(label + " has an IPLIKE octet above 255: " + field);
                }
                if (low > high) {
                    throw badRequest(label + " has a reversed IPLIKE range: " + part);
                }
            }
        }
    }

    // not trimmed: an unrelated save must not rewrite a value it did not touch
    private static String blankToNull(final String s) {
        return s == null || s.isBlank() ? null : s;
    }
}
