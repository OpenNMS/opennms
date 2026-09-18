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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.wsman.eventlog.WsmanEventlogConfiguration;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.dao.api.EventConfEventDao;
import org.opennms.netmgt.dao.api.EventConfSourceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.model.EventConfEvent;
import org.opennms.netmgt.model.EventConfSource;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.wsman.eventlog.Durations;
import org.opennms.netmgt.wsman.eventlog.EventLogLevel;
import org.opennms.netmgt.wsman.eventlog.EventLogStatusStore;
import org.opennms.netmgt.wsman.eventlog.EventLogTarget;
import org.opennms.netmgt.wsman.eventlog.EventLogTargetResolver;
import org.opennms.netmgt.wsman.eventlog.WsManEventLogd;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.model.EventConfEventEditRequest;
import org.opennms.web.rest.v2.model.WsmanEventLogConfigDto;
import org.opennms.web.rest.v2.model.WsmanEventLogDefinitionDto;
import org.opennms.web.rest.v2.model.WsmanEventLogFilterPreviewDto;
import org.opennms.web.rest.v2.model.WsmanEventLogStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Reads and rewrites wsman-eventlog-configuration.xml for the Event Logs tab of the
 * Manage WS-Man page, then asks WsManEventLogd to reload. The daemon's own context is
 * not part of the webapp, so the file is addressed under opennms.home directly and
 * a SHA-256 of its bytes serves as the optimistic-locking version.
 */
@Component
@javax.ws.rs.Path("wsman-config/event-log")
@Tag(name = "WsmanConfig", description = "WS-Man agent configuration API")
public class WsmanEventLogConfigRestService {

    private static final Logger LOG = LoggerFactory.getLogger(WsmanEventLogConfigRestService.class);

    static final String FILE_NAME = "wsman-eventlog-configuration.xml";

    private static final long MIN_INTERVAL_MS = 1_000L;
    private static final int MAX_RECORDS_LIMIT = 10_000;
    private static final int MAX_TEXT_LENGTH = 256;

    @Autowired
    private EventProxy eventProxy;

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private JsonStore jsonStore;

    @Autowired
    private EventConfEventDao eventConfEventDao;

    @Autowired
    private EventConfSourceDao eventConfSourceDao;

    @Autowired
    private EventConfPersistenceService eventConfPersistenceService;

    /** The source the liquibase seed created for the daemon's own definitions; new ones from the tab join it. */
    static final String DEFINITION_SOURCE = "opennms.wsman.eventlog.events";

    private static final int PREVIEW_LIMIT = 50;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public WsmanEventLogConfigDto getConfig(@Context final SecurityContext securityContext) {
        requireAdmin(securityContext);
        final byte[] bytes = readBytes();
        return WsmanEventLogConfigDto.from(unmarshal(bytes), digest(bytes));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WsmanEventLogConfigDto updateConfig(@Context final SecurityContext securityContext, final WsmanEventLogConfigDto update) {
        requireAdmin(securityContext);
        if (update == null) {
            throw badRequest("A configuration is required.");
        }
        if (update.version == null || update.version.trim().isEmpty()) {
            throw badRequest("The version of the loaded configuration is required.");
        }
        validate(update);
        synchronized (this) {
            final byte[] current = readBytes();
            if (!update.version.equals(digest(current))) {
                throw new WebApplicationException(Response.status(Status.CONFLICT).type(MediaType.TEXT_PLAIN)
                        .entity(FILE_NAME + " changed since it was loaded; reload the page and apply the change again.").build());
            }
            final WsmanEventlogConfiguration config = update.toConfig();
            writeConfig(config);
        }
        sendDaemonReload();
        final byte[] bytes = readBytes();
        return WsmanEventLogConfigDto.from(unmarshal(bytes), digest(bytes));
    }

    /** Which nodes a package filter would read, before it is saved. */
    @POST
    @javax.ws.rs.Path("preview-filter")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WsmanEventLogFilterPreviewDto previewFilter(@Context final SecurityContext securityContext, final WsmanEventLogFilterPreviewDto.Request request) {
        requireAdmin(securityContext);
        final WsmanEventLogFilterPreviewDto result = new WsmanEventLogFilterPreviewDto();
        final String filter = request == null ? null : request.filter;
        if (filter == null || filter.trim().isEmpty()) {
            result.error = "A filter is required.";
            return result;
        }
        try {
            filterDao.validateRule(filter);
        } catch (final FilterParseException e) {
            result.error = rootMessage(e);
            return result;
        }
        result.valid = true;
        try {
            result.matchedNodes = filterDao.getNodeMap(filter).size();
            final List<EventLogTarget> targets = new EventLogTargetResolver(filterDao, nodeDao, sessionUtils).resolve(filter);
            result.readableNodes = targets.size();
            for (final EventLogTarget target : targets) {
                if (result.matches.size() >= PREVIEW_LIMIT) {
                    break;
                }
                final WsmanEventLogFilterPreviewDto.Match match = new WsmanEventLogFilterPreviewDto.Match();
                match.nodeId = target.getNodeId();
                match.label = target.getNodeLabel();
                match.ipAddress = target.getAddress().getHostAddress();
                match.location = target.getLocation();
                result.matches.add(match);
            }
        } catch (final RuntimeException e) {
            result.error = "The filter is valid, but the preview could not be built: " + rootMessage(e);
        }
        return result;
    }

    /** The daemon's last read per node and log, as it records them in the key-value store. */
    @GET
    @javax.ws.rs.Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public WsmanEventLogStatusDto getStatus(@Context final SecurityContext securityContext) {
        requireAdmin(securityContext);
        return WsmanEventLogStatusDto.from(new EventLogStatusStore(jsonStore).getAll());
    }

    /** The event definition behind every UEI the mappings use. */
    @GET
    @javax.ws.rs.Path("definitions")
    @Produces(MediaType.APPLICATION_JSON)
    public WsmanEventLogDefinitionDto.Rows getDefinitions(@Context final SecurityContext securityContext) {
        requireAdmin(securityContext);
        final Set<String> ueis = new LinkedHashSet<>();
        for (final WsmanEventLogConfigDto.PackageDto pkg : WsmanEventLogConfigDto.from(unmarshal(readBytes()), "").packages) {
            for (final WsmanEventLogConfigDto.EventMappingDto mapping : pkg.eventMappings) {
                if (mapping.uei != null && !mapping.uei.trim().isEmpty()) {
                    ueis.add(mapping.uei.trim());
                }
            }
        }
        final WsmanEventLogDefinitionDto.Rows rows = new WsmanEventLogDefinitionDto.Rows();
        sessionUtils.withReadOnlyTransaction(() -> {
            for (final String uei : ueis) {
                rows.rows.add(readDefinition(uei));
            }
            return null;
        });
        return rows;
    }

    @GET
    @javax.ws.rs.Path("definition")
    @Produces(MediaType.APPLICATION_JSON)
    public WsmanEventLogDefinitionDto getDefinition(@Context final SecurityContext securityContext, @QueryParam("uei") final String uei) {
        requireAdmin(securityContext);
        if (uei == null || uei.trim().isEmpty()) {
            throw badRequest("A uei is required.");
        }
        return sessionUtils.withReadOnlyTransaction(() -> readDefinition(uei.trim()));
    }

    /**
     * Creates or updates the definition for a UEI. Only the daemon's own source is written: a new
     * definition joins it, an existing one there is rewritten in place keeping the parts this tab
     * does not edit, and a UEI defined by any other source is refused.
     */
    @PUT
    @javax.ws.rs.Path("definition")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public WsmanEventLogDefinitionDto saveDefinition(@Context final SecurityContext securityContext, final WsmanEventLogDefinitionDto update) {
        requireAdmin(securityContext);
        if (update == null || update.uei == null || !update.uei.trim().startsWith("uei.")) {
            throw badRequest("A UEI starting with 'uei.' is required.");
        }
        final String uei = update.uei.trim();
        if (uei.length() > MAX_TEXT_LENGTH) {
            throw badRequest("The UEI cannot be longer than " + MAX_TEXT_LENGTH + " characters.");
        }
        if (uei.chars().anyMatch(Character::isWhitespace)) {
            throw badRequest("The UEI cannot contain whitespace.");
        }
        if (update.label == null || update.label.trim().isEmpty()) {
            throw badRequest("The definition needs a label.");
        }
        if (update.label.trim().length() > MAX_TEXT_LENGTH) {
            throw badRequest("The label cannot be longer than " + MAX_TEXT_LENGTH + " characters.");
        }
        if (update.severity == null || update.severity.trim().isEmpty()
                || (OnmsSeverity.get(update.severity.trim()) == OnmsSeverity.INDETERMINATE && !"Indeterminate".equalsIgnoreCase(update.severity.trim()))) {
            throw badRequest("Unknown severity '" + update.severity + "'.");
        }
        if (update.alarm && update.alarmType != null
                && update.alarmType != WsmanEventLogDefinitionDto.ALARM_TYPE_PROBLEM
                && update.alarmType != WsmanEventLogDefinitionDto.ALARM_TYPE_RESOLUTION
                && update.alarmType != WsmanEventLogDefinitionDto.ALARM_TYPE_PROBLEM_WITHOUT_RESOLUTION) {
            throw badRequest("The alarm type must be 1 (problem), 2 (resolution) or 3 (problem without resolution).");
        }
        update.uei = uei;
        update.label = update.label.trim();
        update.severity = OnmsSeverity.get(update.severity.trim()).getLabel();
        final String user = securityContext.getUserPrincipal() != null ? securityContext.getUserPrincipal().getName() : "admin";
        synchronized (this) {
            final EventConfSource source = sessionUtils.withReadOnlyTransaction(() -> eventConfSourceDao.findByName(DEFINITION_SOURCE));
            final Long sourceId = source != null ? source.getId() : null;
            final EventConfEvent existing = sessionUtils.withReadOnlyTransaction(() -> {
                if (sourceId != null) {
                    final List<EventConfEvent> own = eventConfEventDao.findByUeiAndSourceId(uei, sourceId);
                    if (!own.isEmpty()) {
                        return own.get(0);
                    }
                }
                final EventConfEvent other = eventConfEventDao.findByUei(uei);
                if (other != null) {
                    throw new WebApplicationException(Response.status(Status.CONFLICT).type(MediaType.TEXT_PLAIN)
                            .entity(uei + " is defined in source '" + other.getSource().getName() + "'; edit it on the Event Configuration page.").build());
                }
                return null;
            });
            try {
                if (existing != null) {
                    final Event event = parseDefinition(existing);
                    update.applyTo(event);
                    final EventConfEventEditRequest request = new EventConfEventEditRequest();
                    request.setEnabled(existing.getEnabled() == null || existing.getEnabled());
                    request.setEvent(event);
                    eventConfPersistenceService.updateEventConfEvent(sourceId, existing.getId(), request);
                } else {
                    final Event event = new Event();
                    update.applyTo(event);
                    eventConfPersistenceService.addEventConfSourceEvent(sourceId != null ? sourceId : createDefinitionSource(user), user, event);
                }
            } catch (final RuntimeException e) {
                throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
                        .entity("The definition could not be saved: " + rootMessage(e)).build());
            }
        }
        try {
            eventConfPersistenceService.reloadEventsIntoMemory();
        } catch (final RuntimeException e) {
            LOG.warn("The definition of {} is saved, but the in-memory event configuration could not be reloaded", uei, e);
        }
        return sessionUtils.withReadOnlyTransaction(() -> readDefinition(uei));
    }

    private WsmanEventLogDefinitionDto readDefinition(final String uei) {
        final EventConfEvent row = eventConfEventDao.findByUei(uei);
        if (row == null) {
            return WsmanEventLogDefinitionDto.missing(uei);
        }
        return WsmanEventLogDefinitionDto.from(row, parseDefinition(row), DEFINITION_SOURCE);
    }

    private static Event parseDefinition(final EventConfEvent row) {
        try {
            return JaxbUtils.unmarshal(Event.class, row.getXmlContent());
        } catch (final RuntimeException e) {
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
                    .entity("The stored definition of " + row.getUei() + " could not be parsed: " + rootMessage(e)).build());
        }
    }

    private Long createDefinitionSource(final String user) {
        final EventConfSource created = new EventConfSource();
        created.setName(DEFINITION_SOURCE);
        created.setDescription("Windows event log records read by WsManEventLogd");
        created.setVendor("opennms");
        created.setEnabled(true);
        created.setEventCount(0);
        created.setCreatedTime(new Date());
        created.setLastModified(new Date());
        created.setUploadedBy(user);
        return eventConfPersistenceService.createEventConfSource(created);
    }

    private void validate(final WsmanEventLogConfigDto update) {
        if (update.threads != null && update.threads < 1) {
            throw badRequest("threads must be at least 1.");
        }
        if (update.retries != null && update.retries < 0) {
            throw badRequest("retries cannot be negative.");
        }
        if (update.targetRefreshInterval != null && !update.targetRefreshInterval.trim().isEmpty()) {
            parseDuration(update.targetRefreshInterval, "target-refresh-interval");
        }
        final Set<String> packageNames = new HashSet<>();
        for (final WsmanEventLogConfigDto.PackageDto pkg : update.packages) {
            if (pkg.name == null || pkg.name.trim().isEmpty()) {
                throw badRequest("Every package needs a name.");
            }
            if (!packageNames.add(pkg.name.trim())) {
                throw badRequest("Package '" + pkg.name + "' is listed twice.");
            }
            if (pkg.filter == null || pkg.filter.trim().isEmpty()) {
                throw badRequest("Package '" + pkg.name + "' needs a filter.");
            }
            try {
                filterDao.validateRule(pkg.filter);
            } catch (final FilterParseException e) {
                throw badRequest("The filter of package '" + pkg.name + "' is invalid: " + rootMessage(e));
            }
            final Set<String> logNames = new HashSet<>();
            for (final WsmanEventLogConfigDto.LogDto log : pkg.logs) {
                if (log.name == null || log.name.trim().isEmpty()) {
                    throw badRequest("Every log in package '" + pkg.name + "' needs a name.");
                }
                if (!logNames.add(log.name.trim().toLowerCase())) {
                    throw badRequest("Log '" + log.name + "' is listed twice in package '" + pkg.name + "'.");
                }
                if (log.interval != null && log.interval < MIN_INTERVAL_MS) {
                    throw badRequest("The interval of log '" + log.name + "' must be at least " + MIN_INTERVAL_MS + " ms.");
                }
                if (log.maxRecords != null && (log.maxRecords < 1 || log.maxRecords > MAX_RECORDS_LIMIT)) {
                    throw badRequest("max-records of log '" + log.name + "' must be between 1 and " + MAX_RECORDS_LIMIT + ".");
                }
                if (log.lookback != null && !log.lookback.trim().isEmpty()) {
                    parseDuration(log.lookback, "lookback of log '" + log.name + "'");
                }
                try {
                    EventLogLevel.parseEventTypes(log.levels);
                } catch (final IllegalArgumentException e) {
                    throw badRequest("Log '" + log.name + "': " + e.getMessage());
                }
                final Set<Integer> included = checkIds(log.includeEventIds, "include-event-ids of log '" + log.name + "'");
                final Set<Integer> excluded = checkIds(log.excludeEventIds, "exclude-event-ids of log '" + log.name + "'");
                for (final Integer id : included) {
                    if (excluded.contains(id)) {
                        throw badRequest("Log '" + log.name + "': Event ID " + id + " is both included and excluded.");
                    }
                }
                if (log.mode != null && !log.mode.trim().isEmpty() && !"wql".equalsIgnoreCase(log.mode.trim())) {
                    throw badRequest("The mode of log '" + log.name + "' must be wql; reading through Get-WinEvent is not available in this release.");
                }
            }
            final Set<String> mappingKeys = new HashSet<>();
            for (final WsmanEventLogConfigDto.EventMappingDto mapping : pkg.eventMappings) {
                if (mapping.eventId == null || mapping.eventId < 1) {
                    throw badRequest("Every event mapping in package '" + pkg.name + "' needs an Event ID of 1 or more.");
                }
                if (!mappingKeys.add(mapping.eventId + "|" + normalized(mapping.logfile) + "|" + normalized(mapping.source))) {
                    throw badRequest("Package '" + pkg.name + "' maps Event ID " + mapping.eventId + " twice.");
                }
                if (mapping.uei == null || !mapping.uei.trim().startsWith("uei.")) {
                    throw badRequest("The mapping for Event ID " + mapping.eventId + " needs a UEI starting with 'uei.'.");
                }
                if (mapping.severity != null && !mapping.severity.trim().isEmpty()
                        && OnmsSeverity.get(mapping.severity.trim()) == OnmsSeverity.INDETERMINATE
                        && !"Indeterminate".equalsIgnoreCase(mapping.severity.trim())) {
                    throw badRequest("Unknown severity '" + mapping.severity + "' on the mapping for Event ID " + mapping.eventId + ".");
                }
            }
        }
    }

    private static String normalized(final String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static Set<Integer> checkIds(final String csv, final String what) {
        final Set<Integer> ids = new HashSet<>();
        if (csv == null || csv.trim().isEmpty()) {
            return ids;
        }
        for (final String token : csv.split(",")) {
            if (token.trim().isEmpty()) {
                continue;
            }
            try {
                ids.add(Integer.parseInt(token.trim()));
            } catch (final NumberFormatException e) {
                throw badRequest("The " + what + " must be comma-separated numbers.");
            }
        }
        return ids;
    }

    private static void parseDuration(final String value, final String what) {
        try {
            Durations.parse(value);
        } catch (final IllegalArgumentException e) {
            throw badRequest("The " + what + " must be a number of milliseconds or a value like 30s, 15m, 1h or 2d.");
        }
    }

    private void sendDaemonReload() {
        try {
            eventProxy.send(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "Web UI")
                    .addParam(EventConstants.PARM_DAEMON_NAME, WsManEventLogd.NAME).getEvent());
        } catch (final EventProxyException e) {
            // the file is saved; the daemon picks it up on its next reload
        }
    }

    static Path configFile() {
        return Paths.get(Objects.requireNonNull(System.getProperty("opennms.home"), "opennms.home"), "etc", FILE_NAME);
    }

    private static byte[] readBytes() {
        try {
            return Files.readAllBytes(configFile());
        } catch (final IOException e) {
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
                    .entity("Could not read " + FILE_NAME + ": " + e.getMessage()).build());
        }
    }

    private static WsmanEventlogConfiguration unmarshal(final byte[] bytes) {
        try {
            return JaxbUtils.unmarshal(WsmanEventlogConfiguration.class, new String(bytes, StandardCharsets.UTF_8));
        } catch (final RuntimeException e) {
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
                    .entity(FILE_NAME + " could not be parsed: " + rootMessage(e)).build());
        }
    }

    private static void writeConfig(final WsmanEventlogConfiguration config) {
        final String xml;
        try {
            xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + JaxbUtils.marshal(config);
        } catch (final RuntimeException e) {
            throw badRequest("The configuration could not be written: " + rootMessage(e));
        }
        final Path target = configFile();
        final Path tmp = target.resolveSibling(FILE_NAME + ".tmp");
        try {
            Files.write(tmp, xml.getBytes(StandardCharsets.UTF_8));
            Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            try {
                Files.deleteIfExists(tmp);
            } catch (final IOException ignored) {
            }
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
                    .entity("Could not write " + FILE_NAME + ": " + e.getMessage()).build());
        }
    }

    private static void requireAdmin(final SecurityContext securityContext) {
        if (securityContext == null || !securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw new WebApplicationException(Response.status(Status.FORBIDDEN).build());
        }
    }

    private static WebApplicationException badRequest(final String message) {
        return new WebApplicationException(Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(message).build());
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

    private static String rootMessage(final Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }
}
