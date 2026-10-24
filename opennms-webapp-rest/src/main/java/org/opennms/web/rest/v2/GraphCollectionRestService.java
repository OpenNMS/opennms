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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.config.GraphCollectionConfigFactory;
import org.opennms.netmgt.config.graphcollections.Graph;
import org.opennms.netmgt.config.graphcollections.GraphCollection;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.api.GraphCollectionRestApi;
import org.opennms.web.rest.v2.model.graphCollection.GraphCollectionDto;
import org.opennms.web.rest.v2.model.graphCollection.GraphCollectionGraphDto;
import org.opennms.web.rest.v2.model.graphCollection.GraphCollectionResolvedDto;
import org.opennms.web.rest.v2.model.graphCollection.GraphCollectionResolvedGraphDto;
import org.opennms.web.rest.v2.model.graphCollection.GraphCollectionResourceDto;
import org.opennms.web.rest.v2.model.graphCollection.GraphCollectionTimespanDto;
import org.opennms.web.svclayer.api.GraphCollectionService;
import org.opennms.web.svclayer.api.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Component;

/**
 * Graph collections (formerly KSC reports) over {@link GraphCollectionConfigFactory}, which keeps
 * ksc-performance-reports.xml as the system of record so hand edits and the config tester keep
 * working.
 *
 * The collection is the unit of update: a PUT replaces the whole thing, which gives the editor
 * reorder, edit and delete in one call and keeps the file writes to one per save. Check-then-act
 * sequences synchronize on the factory instance, whose own methods are synchronized on it too,
 * and the v1 service and the legacy controllers take the same monitor around their own
 * read-change-save sequences. The factory hands out its live objects, so this service never
 * changes a stored collection in place: writes build a copy and swap it in under the lock, and
 * reads copy under the lock before resolving outside it.
 *
 * The file is validated against its XSD on load, so title and timespan are never null on a stored
 * graph; the null guards in the mappers only matter where no XSD can be found. The
 * link rule is applied on the way out as well as on the way in, since the v1 API and hand edits
 * store links without it.
 *
 * Single-collection reads carry an ETag over the stored content, and the writes honour If-Match
 * with a 412, so two editors cannot overwrite each other unnoticed and a stale page cannot act on
 * a collection whose id was reused after a delete. The header is optional: a write without it
 * always goes through.
 *
 * No event is sent on writes. The historical kscReportUpdated event announces that graphs were
 * removed by the view page's cleanup, which this API deliberately does not do.
 *
 * Permissions match the legacy pages: any authenticated user may write, except ROLE_READONLY. The
 * security layer enforces that in its own expression block for /api/v2/graph-collections in
 * applicationContext-spring-security.xml; the check repeated here is defense in depth, and it is
 * what the REST test harness, which runs without that layer, exercises.
 */
@Component("graphCollectionRestServiceV2")
public class GraphCollectionRestService implements GraphCollectionRestApi {
    private static final Logger LOG = LoggerFactory.getLogger(GraphCollectionRestService.class);

    static final String TYPE_CUSTOM = "custom";
    static final String TYPE_NODE = "node";
    static final String TYPE_NODE_SOURCE = "nodeSource";
    static final String TYPE_DOMAIN = "domain";

    /** Graph entry kinds: a prefabricated graph of one resource, or a graph from the ad-hoc graph builder. */
    static final String KIND_PREFAB = "prefab";
    static final String KIND_ADHOC = "adhoc";

    /**
     * The builder's share link is capped at 6000 characters of query string on the client; this leaves
     * room for a longer one from another client without letting the file grow without bound.
     */
    static final int MAX_ADHOC_STATE_LENGTH = 8192;

    /** The share link's time window keys. The entry's timespan sets the window, so the state carries none. */
    static final Set<String> ADHOC_TIME_KEYS = Set.of("range", "start", "end", "fmt");

    /**
     * The share link keys that name what is graphed: filters, picks, exclusions, series edits,
     * expressions and the legacy series form. A state without any of them is not a graph.
     */
    static final Set<String> ADHOC_SOURCE_KEYS = Set.of("nf", "rf", "df", "pn", "pr", "pd", "xd", "o", "e", "s");

    /** Legacy pages capped titles at 80 and 40 characters; this is a looser bound on the stored strings. */
    static final int MAX_TITLE_LENGTH = 255;

    /** Kept under its historical key: it is user configuration in opennms.properties. */
    static final String DEFAULT_GRAPHS_PER_LINE_PROPERTY = "ksc.default.graphsPerLine";

    @Autowired
    private GraphCollectionConfigFactory m_configFactory;

    @Autowired
    private GraphCollectionService m_graphCollectionService;

    @Autowired
    private ResourceService m_resourceService;

    @Override
    public Response listCollections() {
        final List<GraphCollectionDto> collections = new ArrayList<>();
        synchronized (m_configFactory) {
            for (final GraphCollection collection : m_configFactory.getCollectionMap().values()) {
                collections.add(toResponse(collection));
            }
        }
        collections.sort(Comparator.comparing(GraphCollectionDto::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return Response.ok(collections).build();
    }

    @Override
    public Response listTimespans() {
        final List<GraphCollectionTimespanDto> timespans = new ArrayList<>();
        for (final String timespan : GraphCollectionConfigFactory.TIMESPAN_OPTIONS) {
            timespans.add(new GraphCollectionTimespanDto(timespan, timespan.replace('_', ' ')));
        }
        return Response.ok(timespans).build();
    }

    @Override
    public Response getCollection(final Request request, final int id) {
        synchronized (m_configFactory) {
            final GraphCollection collection = m_configFactory.getCollectionById(id);
            if (collection == null) {
                return notFound(id);
            }
            return Response.ok(toResponse(collection)).tag(etagOf(collection)).build();
        }
    }

    @Override
    public Response getResolvedCollection(final Request request, final int id, final String timespan, final String graphtype) {
        final String timespanProblem = validateOverrideTimespan(timespan);
        if (timespanProblem != null) {
            return badRequest(timespanProblem);
        }
        final GraphCollection snapshot;
        final EntityTag etag;
        synchronized (m_configFactory) {
            final GraphCollection collection = m_configFactory.getCollectionById(id);
            if (collection == null) {
                return notFound(id);
            }
            // Resolve a copy: the legacy view page still prunes graphs from the live list.
            snapshot = toModel(toDto(collection), id);
            etag = etagOf(collection);
        }
        return Response.ok(resolve(snapshot, TYPE_CUSTOM, timespan, graphtype)).tag(etag).build();
    }

    @Override
    public Response getResolvedNodeCollection(final int nodeId, final String timespan, final String graphtype) {
        return resolveSynthetic(TYPE_NODE, "Node " + nodeId, ResourceId.get("node", Integer.toString(nodeId)), timespan, graphtype,
                () -> m_graphCollectionService.buildNodeCollection(nodeId));
    }

    @Override
    public Response getResolvedNodeSourceCollection(final String nodeSource, final String timespan, final String graphtype) {
        return resolveSynthetic(TYPE_NODE_SOURCE, "Node " + nodeSource, ResourceId.get("nodeSource", nodeSource), timespan, graphtype,
                () -> m_graphCollectionService.buildNodeSourceCollection(nodeSource));
    }

    @Override
    public Response getResolvedDomainCollection(final String domain, final String timespan, final String graphtype) {
        return resolveSynthetic(TYPE_DOMAIN, "Domain " + domain, ResourceId.get("domain", domain), timespan, graphtype,
                () -> m_graphCollectionService.buildDomainCollection(domain));
    }

    @Override
    public Response createCollection(final SecurityContext securityContext, final UriInfo uriInfo, final GraphCollectionDto dto) {
        final Response denied = denyReadOnly(securityContext);
        if (denied != null) {
            return denied;
        }
        final List<String> problems = validate(dto);
        if (!problems.isEmpty()) {
            return badRequest(problems);
        }
        synchronized (m_configFactory) {
            final GraphCollection collection = toModel(dto, null);
            m_configFactory.addCollection(collection);
            final int id = collection.getId();
            try {
                m_configFactory.saveCurrent();
            } catch (final Exception e) {
                reloadQuietly();
                return saveFailed(e);
            }
            final GraphCollection saved = m_configFactory.getCollectionById(id);
            final URI location = uriInfo.getBaseUriBuilder().path("graph-collections").path(Integer.toString(id)).build();
            return Response.created(location).entity(toResponse(saved)).tag(etagOf(saved)).build();
        }
    }

    @Override
    public Response updateCollection(final SecurityContext securityContext, final Request request, final int id, final GraphCollectionDto dto) {
        final Response denied = denyReadOnly(securityContext);
        if (denied != null) {
            return denied;
        }
        final List<String> problems = validate(dto);
        if (!problems.isEmpty()) {
            return badRequest(problems);
        }
        synchronized (m_configFactory) {
            final GraphCollection current = m_configFactory.getCollectionById(id);
            if (current == null) {
                return notFound(id);
            }
            final Response stale = precondition(request, current);
            if (stale != null) {
                return stale;
            }
            m_configFactory.setCollection(id, toModel(dto, id));
            try {
                m_configFactory.saveCurrent();
            } catch (final Exception e) {
                reloadQuietly();
                return saveFailed(e);
            }
            final GraphCollection saved = m_configFactory.getCollectionById(id);
            return Response.ok(toResponse(saved)).tag(etagOf(saved)).build();
        }
    }

    @Override
    public Response deleteCollection(final SecurityContext securityContext, final Request request, final int id) {
        final Response denied = denyReadOnly(securityContext);
        if (denied != null) {
            return denied;
        }
        synchronized (m_configFactory) {
            final GraphCollection collection = m_configFactory.getCollectionById(id);
            if (collection == null) {
                return notFound(id);
            }
            final Response stale = precondition(request, collection);
            if (stale != null) {
                return stale;
            }
            try {
                m_configFactory.deleteCollectionAndSave(id);
            } catch (final Exception e) {
                reloadQuietly();
                return saveFailed(e);
            }
            return Response.noContent().build();
        }
    }

    @Override
    public Response addGraph(final SecurityContext securityContext, final Request request, final int id, final Integer index, final GraphCollectionGraphDto graphDto) {
        final Response denied = denyReadOnly(securityContext);
        if (denied != null) {
            return denied;
        }
        if (graphDto == null) {
            return badRequest("A graph body is required.");
        }
        final String problem = validateGraph(graphDto, null);
        if (problem != null) {
            return badRequest(problem);
        }
        synchronized (m_configFactory) {
            final GraphCollection collection = m_configFactory.getCollectionById(id);
            if (collection == null) {
                return notFound(id);
            }
            final Response stale = precondition(request, collection);
            if (stale != null) {
                return stale;
            }
            final int size = collection.getGraphs().size();
            if (index != null && (index < 0 || index > size)) {
                return badRequest("index must be between 0 and " + size + ".");
            }
            // Change a copy, not the stored object: readers that do not take the lock must never
            // see a graph that has not been saved yet.
            final GraphCollection changed = toModel(toDto(collection), id);
            final Graph graph = toModel(graphDto);
            if (index == null) {
                changed.addGraph(graph);
            } else {
                changed.addGraph(index, graph);
            }
            m_configFactory.setCollection(id, changed);
            try {
                m_configFactory.saveCurrent();
            } catch (final Exception e) {
                reloadQuietly();
                return saveFailed(e);
            }
            final GraphCollection saved = m_configFactory.getCollectionById(id);
            return Response.ok(toResponse(saved)).tag(etagOf(saved)).build();
        }
    }

    @Override
    public Response reload(final SecurityContext securityContext) {
        final Response denied = denyReadOnly(securityContext);
        if (denied != null) {
            return denied;
        }
        synchronized (m_configFactory) {
            try {
                m_configFactory.reload();
            } catch (final Exception e) {
                LOG.error("Can't reload the graph collections configuration", e);
                return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity("Can't reload the graph collections configuration: " + e.getMessage()).build();
            }
        }
        return Response.noContent().build();
    }

    // -- resolution ------------------------------------------------------------------------------

    private Response resolveSynthetic(final String type, final String what, final ResourceId parentId, final String timespan, final String graphtype, final Supplier<GraphCollection> builder) {
        final String timespanProblem = validateOverrideTimespan(timespan);
        if (timespanProblem != null) {
            return badRequest(timespanProblem);
        }
        final GraphCollection collection;
        try {
            // The resource DAO answers an unknown parent with null, and the builder then quietly
            // produces an empty collection; a 404 is more useful to the caller.
            if (m_resourceService.getResourceById(parentId) == null) {
                return Response.status(Status.NOT_FOUND).entity(what + " was not found or has no resources.").build();
            }
            collection = builder.get();
        } catch (final ObjectRetrievalFailureException | IllegalArgumentException e) {
            LOG.debug("No resources for {}: {}", what, e.getMessage());
            return Response.status(Status.NOT_FOUND).entity(what + " was not found or has no resources.").build();
        } catch (final RuntimeException e) {
            LOG.error("Can't look up the resources for {}", what, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Can't look up the resources for " + what + ": " + e.getMessage()).build();
        }
        if (collection == null) {
            return Response.status(Status.NOT_FOUND).entity(what + " was not found or has no resources.").build();
        }
        return Response.ok(resolve(collection, type, timespan, graphtype)).build();
    }

    /**
     * Ported from the legacy CustomViewController, minus its habit of deleting graphs whose
     * resource could not be found: those are reported with valid=false instead.
     */
    GraphCollectionResolvedDto resolve(final GraphCollection collection, final String type, final String overrideTimespan, final String overrideGraphtype) {
        final String timespan = normalizeOverride(overrideTimespan);
        final String graphtype = normalizeOverride(overrideGraphtype);

        final GraphCollectionResolvedDto resolved = new GraphCollectionResolvedDto();
        resolved.setId(TYPE_CUSTOM.equals(type) ? collection.getId() : null);
        resolved.setType(type);
        resolved.setTitle(collection.getTitle());
        resolved.setShowTimespanButton(collection.getShowTimespanButton().orElse(false));
        resolved.setShowGraphtypeButton(collection.getShowGraphtypeButton().orElse(false));
        resolved.setGraphsPerLine(collection.getGraphsPerLine().filter(n -> n > 0).orElseGet(GraphCollectionRestService::defaultGraphsPerLine));
        resolved.setTimespan(timespan);
        resolved.setGraphtype(graphtype);

        final Set<String> graphTypes = new TreeSet<>();
        int index = 0;
        for (final Graph graph : collection.getGraphs()) {
            final GraphCollectionResolvedGraphDto dto = new GraphCollectionResolvedGraphDto();
            dto.setIndex(index++);
            dto.setKind(kindOf(graph));
            dto.setTitle(graph.getTitle());
            dto.setExtlink(safeLink(graph.getExtlink().orElse(null)));
            dto.setTimespan(timespan != null ? timespan : graph.getTimespan());

            // What the editor would reject on save is flagged here too, so a hand-edited entry shows
            // its problem where the user can fix it instead of blocking the next save unexplained.
            final String invalid = validateGraph(toDto(graph), null);

            if (KIND_ADHOC.equals(dto.getKind())) {
                // The client draws an ad-hoc graph from the builder state over the window computed here;
                // there is no resource to look up, and the graphtype override does not apply to it.
                dto.setAdhocState(graph.getAdhocState().orElse(null));
                dto.setValid(invalid == null);
                dto.setError(invalid);
                applyWindow(dto);
                resolved.getGraphs().add(dto);
                continue;
            }

            dto.setGraphtype(graphtype != null ? graphtype : graph.getGraphtype().orElse(null));
            dto.setResourceId(graph.getResourceId().orElse(null));

            OnmsResource resource = null;
            String error = null;
            try {
                resource = m_graphCollectionService.getResourceFromGraph(graph);
                if (resource == null) {
                    error = "The resource this graph refers to no longer exists.";
                }
            } catch (final ObjectRetrievalFailureException | IllegalArgumentException e) {
                LOG.debug("Could not resolve the resource for graph '{}' in collection '{}'", graph.getTitle(), collection.getTitle(), e);
                error = "The resource this graph refers to no longer exists: " + e.getMessage();
            } catch (final RuntimeException e) {
                // One broken graph must not take the whole collection down, as the legacy page's per-graph catch did not.
                LOG.warn("Unexpected error resolving the resource for graph '{}' in collection '{}'", graph.getTitle(), collection.getTitle(), e);
                error = "The resource this graph refers to could not be looked up: " + e.getMessage();
            }

            PrefabGraph available = null;
            if (resource != null) {
                dto.setResourceId(resource.getId().toString());
                dto.setResource(toDto(resource, true));
                try {
                    for (final PrefabGraph prefab : m_resourceService.findPrefabGraphsForResource(resource)) {
                        graphTypes.add(prefab.getName());
                        if (prefab.getName().equals(dto.getGraphtype())) {
                            available = prefab;
                        }
                    }
                    if (available == null) {
                        error = "The prefabricated graph '" + dto.getGraphtype() + "' is not available for this resource.";
                    }
                } catch (final RuntimeException e) {
                    LOG.warn("Unexpected error listing the prefabricated graphs for resource '{}' in collection '{}'", resource.getId(), collection.getTitle(), e);
                    error = "The prefabricated graphs for this resource could not be listed: " + e.getMessage();
                }
            }
            if (available != null) {
                dto.setPrefabGraphTitle(available.getTitle());
            }
            dto.setValid(resource != null && available != null && invalid == null);
            dto.setError(joinErrors(invalid, error));
            applyWindow(dto);

            resolved.getGraphs().add(dto);
        }
        resolved.setGraphTypes(new ArrayList<>(graphTypes));
        return resolved;
    }

    static int defaultGraphsPerLine() {
        final Integer configured = SystemProperties.getInteger(DEFAULT_GRAPHS_PER_LINE_PROPERTY, 1);
        return configured == null || configured < 1 ? 1 : configured;
    }

    /** Turn the effective timespan into start and end instants, falling back to the default for an unknown one. */
    private static void applyWindow(final GraphCollectionResolvedGraphDto dto) {
        Calendar begin = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        try {
            GraphCollectionConfigFactory.getBeginEndTime(dto.getTimespan(), begin, end);
        } catch (final IllegalArgumentException e) {
            // A timespan the file carries but the list does not know: fall back to the default, on
            // fresh calendars since the failed call has already zeroed the time fields on these.
            begin = Calendar.getInstance();
            end = Calendar.getInstance();
            GraphCollectionConfigFactory.getBeginEndTime("7_day", begin, end);
            dto.setValid(false);
            if (dto.getError() == null) {
                dto.setError("Unknown timespan '" + dto.getTimespan() + "'.");
            }
        }
        dto.setStart(begin.getTimeInMillis());
        dto.setEnd(end.getTimeInMillis());
    }

    private static String normalizeOverride(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        if (trimmed.isEmpty() || "none".equals(trimmed) || "null".equals(trimmed)) {
            return null;
        }
        return trimmed;
    }

    private static String validateOverrideTimespan(final String timespan) {
        final String normalized = normalizeOverride(timespan);
        if (normalized != null && !GraphCollectionConfigFactory.isValidTimespan(normalized)) {
            return "Unknown timespan '" + normalized + "'.";
        }
        return null;
    }

    // -- validation and mapping ------------------------------------------------------------------

    /**
     * Every problem with the collection, one per graph at most, so a client can show them all at
     * once; an older file with several hand-edited graphs would otherwise be fixed one save at a time.
     */
    static List<String> validate(final GraphCollectionDto dto) {
        final List<String> problems = new ArrayList<>();
        if (dto == null) {
            problems.add("A graph collection body is required.");
            return problems;
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            problems.add("A title is required.");
        } else if (dto.getTitle().trim().length() > MAX_TITLE_LENGTH) {
            problems.add("The title must be at most " + MAX_TITLE_LENGTH + " characters.");
        }
        if (dto.getGraphsPerLine() != null && dto.getGraphsPerLine() < 0) {
            problems.add("graphsPerLine must be 0 or greater.");
        }
        int index = 0;
        for (final GraphCollectionGraphDto graph : dto.getGraphs()) {
            final String problem = validateGraph(graph, index++);
            if (problem != null) {
                problems.add(problem);
            }
        }
        return problems;
    }

    /** index is the zero-based position in a collection body, or null for a single graph on its own. */
    static String validateGraph(final GraphCollectionGraphDto graph, final Integer index) {
        final String where = index == null ? "" : "Graph " + (index + 1) + ": ";
        if (graph == null) {
            return where + (index == null ? "A graph body is required." : "the graph is empty.");
        }
        final String kind = kindOf(graph);
        if (!KIND_PREFAB.equals(kind) && !KIND_ADHOC.equals(kind)) {
            return where + "unknown kind '" + kind + "'; expected '" + KIND_PREFAB + "' or '" + KIND_ADHOC + "'.";
        }
        final boolean hasLegacyLocator = !StringUtils.isBlank(graph.getNodeId()) || !StringUtils.isBlank(graph.getNodeSource()) || !StringUtils.isBlank(graph.getDomain());
        if (KIND_ADHOC.equals(kind)) {
            if (!StringUtils.isBlank(graph.getResourceId()) || !StringUtils.isBlank(graph.getGraphtype()) || hasLegacyLocator || !StringUtils.isBlank(graph.getInterfaceId())) {
                return where + "an ad-hoc graph takes no resourceId, graphtype or node address; its sources are in adhocState.";
            }
            final String stateProblem = validateAdhocState(graph.getAdhocState());
            if (stateProblem != null) {
                return where + stateProblem;
            }
        } else {
            if (!StringUtils.isBlank(graph.getAdhocState())) {
                return where + "adhocState belongs to an ad-hoc graph only.";
            }
            if (StringUtils.isBlank(graph.getGraphtype())) {
                return where + "a graphtype is required.";
            }
        }
        if (StringUtils.isBlank(graph.getTimespan())) {
            return where + "a timespan is required.";
        }
        if (!GraphCollectionConfigFactory.isValidTimespan(graph.getTimespan())) {
            return where + "unknown timespan '" + graph.getTimespan() + "'.";
        }
        if (graph.getTitle() != null && graph.getTitle().length() > MAX_TITLE_LENGTH) {
            return where + "the title must be at most " + MAX_TITLE_LENGTH + " characters.";
        }
        if (KIND_PREFAB.equals(kind)) {
            if (StringUtils.isBlank(graph.getResourceId()) && !hasLegacyLocator) {
                return where + "a resourceId is required.";
            }
            if (!StringUtils.isBlank(graph.getResourceId()) && !isParseableResourceId(graph.getResourceId())) {
                return where + "'" + graph.getResourceId() + "' is not a well-formed resourceId.";
            }
        }
        if (!StringUtils.isBlank(graph.getExtlink()) && !isSafeLink(graph.getExtlink())) {
            return where + "extlink must be a relative link or an http, https or mailto URL.";
        }
        return null;
    }

    /**
     * The builder state is opaque to the server, which only checks that it is the query string of a
     * share link: a bounded, decodable `key=value&key=value` string without the leading `?`, naming
     * at least one source, and without a time window, since the entry's timespan sets the window
     * the graph is drawn over.
     */
    static String validateAdhocState(final String state) {
        if (StringUtils.isBlank(state)) {
            return "an ad-hoc graph needs the builder state in adhocState.";
        }
        if (state.length() > MAX_ADHOC_STATE_LENGTH) {
            return "adhocState must be at most " + MAX_ADHOC_STATE_LENGTH + " characters.";
        }
        if (state.startsWith("?") || state.startsWith("#")) {
            return "adhocState must be a URL query string without the leading '?'.";
        }
        if (!state.chars().allMatch(c -> c > ' ' && c < 127)) {
            return "adhocState must be percent-encoded: it may hold only printable ASCII.";
        }
        boolean namesSources = false;
        for (final String pair : state.split("&")) {
            final int eq = pair.indexOf('=');
            final String key = eq < 0 ? pair : pair.substring(0, eq);
            if (key.isEmpty()) {
                return "adhocState must be a URL query string without the leading '?'.";
            }
            try {
                URLDecoder.decode(pair, StandardCharsets.UTF_8);
            } catch (final IllegalArgumentException e) {
                return "adhocState is not a well-formed URL query string: " + e.getMessage();
            }
            if (ADHOC_TIME_KEYS.contains(key)) {
                return "adhocState must not carry a time window (" + key + "); the entry's timespan sets it.";
            }
            namesSources |= ADHOC_SOURCE_KEYS.contains(key);
        }
        if (!namesSources) {
            return "adhocState names no sources; a graph's share link carries at least one of nf, rf, df, pn, pr, pd, xd, o or e.";
        }
        return null;
    }

    /** Same decoding rule as the resolver, see NMS-10309: stored ids may be URL-encoded. */
    private static boolean isParseableResourceId(final String resourceId) {
        try {
            return ResourceId.fromString(URLDecoder.decode(resourceId, StandardCharsets.UTF_8)) != null;
        } catch (final IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * extlink ends up as a link on the view page. Relative links (what hand-edited files tend to
     * carry), web URLs and mailto links are fine; anything with another scheme, javascript: above
     * all, is not.
     */
    public static boolean isSafeLink(final String value) {
        final String trimmed = value.trim();
        if (trimmed.startsWith("//") || trimmed.startsWith("\\")) {
            // Protocol-relative: no scheme, yet it leaves the site.
            return false;
        }
        try {
            final URI uri = new URI(trimmed);
            final String scheme = uri.getScheme();
            if (scheme == null) {
                return uri.getAuthority() == null;
            }
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme) || "mailto".equalsIgnoreCase(scheme);
        } catch (final URISyntaxException e) {
            return false;
        }
    }

    /** The link if it passes the rule above, else null: the v1 API and hand edits store links without the rule. */
    static String safeLink(final String link) {
        return StringUtils.isBlank(link) || isSafeLink(link) ? link : null;
    }

    /** An absent kind means prefab, so entries written before the field existed keep working. */
    private static String kindOf(final GraphCollectionGraphDto graph) {
        return StringUtils.isBlank(graph.getKind()) ? KIND_PREFAB : graph.getKind().trim();
    }

    private static String kindOf(final Graph graph) {
        return graph.getKind().orElse(KIND_PREFAB);
    }

    static GraphCollection toModel(final GraphCollectionDto dto, final Integer id) {
        final GraphCollection collection = new GraphCollection();
        collection.setId(id);
        collection.setTitle(StringUtils.trimToEmpty(dto.getTitle()));
        collection.setShowTimespanButton(dto.getShowTimespanButton());
        collection.setShowGraphtypeButton(dto.getShowGraphtypeButton());
        collection.setGraphsPerLine(dto.getGraphsPerLine());
        for (final GraphCollectionGraphDto graph : dto.getGraphs()) {
            collection.addGraph(toModel(graph));
        }
        return collection;
    }

    static Graph toModel(final GraphCollectionGraphDto dto) {
        final Graph graph = new Graph();
        graph.setTitle(dto.getTitle() == null ? "" : dto.getTitle());
        graph.setResourceId(dto.getResourceId());
        graph.setGraphtype(StringUtils.trimToNull(dto.getGraphtype()));
        graph.setTimespan(dto.getTimespan());
        graph.setExtlink(dto.getExtlink() == null ? null : dto.getExtlink().trim());
        graph.setNodeId(dto.getNodeId());
        graph.setNodeSource(dto.getNodeSource());
        graph.setDomain(dto.getDomain());
        graph.setInterfaceId(dto.getInterfaceId());
        // written only when it says something, so files with prefab graphs look as they always did
        graph.setKind(KIND_ADHOC.equals(kindOf(dto)) ? KIND_ADHOC : null);
        graph.setAdhocState(StringUtils.trimToNull(dto.getAdhocState()));
        return graph;
    }

    static GraphCollectionDto toDto(final GraphCollection collection) {
        final GraphCollectionDto dto = new GraphCollectionDto();
        dto.setId(collection.getId());
        dto.setTitle(collection.getTitle());
        dto.setShowTimespanButton(collection.getShowTimespanButton().orElse(null));
        dto.setShowGraphtypeButton(collection.getShowGraphtypeButton().orElse(null));
        dto.setGraphsPerLine(collection.getGraphsPerLine().orElse(null));
        for (final Graph graph : collection.getGraphs()) {
            dto.getGraphs().add(toDto(graph));
        }
        return dto;
    }

    /** What leaves the service: the stored collection with any link that fails the link rule withheld. */
    private static GraphCollectionDto toResponse(final GraphCollection collection) {
        final GraphCollectionDto dto = toDto(collection);
        for (final GraphCollectionGraphDto graph : dto.getGraphs()) {
            graph.setExtlink(safeLink(graph.getExtlink()));
        }
        return dto;
    }

    static GraphCollectionGraphDto toDto(final Graph graph) {
        final GraphCollectionGraphDto dto = new GraphCollectionGraphDto();
        dto.setKind(kindOf(graph));
        dto.setTitle(graph.getTitle());
        dto.setResourceId(graph.getResourceId().orElse(null));
        dto.setGraphtype(graph.getGraphtype().orElse(null));
        dto.setTimespan(graph.getTimespan());
        dto.setExtlink(graph.getExtlink().orElse(null));
        dto.setNodeId(graph.getNodeId().orElse(null));
        dto.setNodeSource(graph.getNodeSource().orElse(null));
        dto.setDomain(graph.getDomain().orElse(null));
        dto.setInterfaceId(graph.getInterfaceId().orElse(null));
        dto.setAdhocState(graph.getAdhocState().orElse(null));
        return dto;
    }

    private static GraphCollectionResourceDto toDto(final OnmsResource resource, final boolean withParent) {
        final GraphCollectionResourceDto dto = new GraphCollectionResourceDto();
        dto.setId(resource.getId() == null ? null : resource.getId().toString());
        dto.setLabel(resource.getLabel());
        if (resource.getResourceType() != null) {
            dto.setTypeName(resource.getResourceType().getName());
            dto.setTypeLabel(resource.getResourceType().getLabel());
        }
        dto.setLink(resource.getLink());
        if (withParent && resource.getParent() != null) {
            dto.setParent(toDto(resource.getParent(), false));
        }
        return dto;
    }

    // -- plumbing --------------------------------------------------------------------------------

    /**
     * Error bodies are one plain-text message, or one problem per line, and say so: the class-level
     * JSON content type would otherwise be stamped on them, and a client that goes by the header
     * would not read them.
     */
    private static Response.ResponseBuilder plainText(final Response.ResponseBuilder builder, final String message) {
        return builder.entity(message).type(MediaType.TEXT_PLAIN);
    }

    private static Response denyReadOnly(final SecurityContext securityContext) {
        if (securityContext != null && securityContext.isUserInRole(Authentication.ROLE_READONLY)) {
            return plainText(Response.status(Status.FORBIDDEN), "Graph collections are read-only for this user.").build();
        }
        return null;
    }

    /**
     * A strong tag over the stored content, id included, so the same id reused after a delete does
     * not match a tag taken before it.
     */
    static EntityTag etagOf(final GraphCollection collection) {
        final GraphCollectionDto dto = toDto(collection);
        final StringBuilder canonical = new StringBuilder()
                .append(dto.getId()).append('|').append(dto.getTitle()).append('|')
                .append(dto.getShowTimespanButton()).append('|').append(dto.getShowGraphtypeButton()).append('|')
                .append(dto.getGraphsPerLine());
        for (final GraphCollectionGraphDto graph : dto.getGraphs()) {
            canonical.append('\n').append(graph.getKind()).append('|').append(graph.getTitle()).append('|')
                    .append(graph.getResourceId()).append('|').append(graph.getGraphtype()).append('|')
                    .append(graph.getTimespan()).append('|').append(graph.getExtlink()).append('|')
                    .append(graph.getNodeId()).append('|').append(graph.getNodeSource()).append('|')
                    .append(graph.getDomain()).append('|').append(graph.getInterfaceId()).append('|')
                    .append(graph.getAdhocState());
        }
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            final StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                hex.append(String.format("%02x", digest[i]));
            }
            return new EntityTag(hex.toString());
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /** 412 when the caller's If-Match no longer names the stored collection; null to go ahead. */
    private static Response precondition(final Request request, final GraphCollection collection) {
        if (request == null) {
            return null;
        }
        final Response.ResponseBuilder failed = request.evaluatePreconditions(etagOf(collection));
        if (failed == null) {
            return null;
        }
        return plainText(failed, "Graph collection " + collection.getId() + " has changed since it was read; fetch it again before writing.").build();
    }

    private static Response notFound(final int id) {
        return plainText(Response.status(Status.NOT_FOUND), "Graph collection " + id + " was not found.").build();
    }

    private static Response badRequest(final String message) {
        return plainText(Response.status(Status.BAD_REQUEST), message).build();
    }

    /** One problem per line, so a single-problem body reads exactly as before. */
    private static Response badRequest(final List<String> problems) {
        return badRequest(String.join("\n", problems));
    }

    private static String joinErrors(final String first, final String second) {
        if (first == null) {
            return second;
        }
        return second == null ? first : first + " " + second;
    }

    private static Response saveFailed(final Exception e) {
        LOG.error("Can't save the graph collections configuration", e);
        return plainText(Response.status(Status.INTERNAL_SERVER_ERROR), "Can't save the graph collections configuration: " + e.getMessage()).build();
    }

    /** After a failed save, drop the unsaved in-memory change so the next read matches the file. */
    private void reloadQuietly() {
        try {
            m_configFactory.reload();
        } catch (final IOException e) {
            LOG.warn("Can't reload the graph collections configuration after a failed save", e);
        }
    }
}
