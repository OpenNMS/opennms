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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

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
 * sequences synchronize on the factory instance, whose own methods are synchronized on it too, so
 * the legacy controllers that still write through the same singleton cannot interleave with a
 * REST write inside a single factory call.
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

    /** Graph entry kinds. Only prefab is stored today; adhoc is reserved for the ad-hoc graph builder. */
    static final String KIND_PREFAB = "prefab";
    static final String KIND_ADHOC = "adhoc";

    /** Legacy pages capped titles at 80 and 40 characters; this is a looser bound on the stored strings. */
    static final int MAX_TITLE_LENGTH = 255;

    /**
     * Shared with the legacy view page, which reads it through a Spring placeholder that cannot fall
     * back between keys, so the key is not renamed until that page goes.
     */
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
                collections.add(toDto(collection));
            }
        }
        collections.sort(Comparator.comparing(GraphCollectionDto::getTitle, String.CASE_INSENSITIVE_ORDER));
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
    public Response getCollection(final int id) {
        synchronized (m_configFactory) {
            final GraphCollection collection = m_configFactory.getCollectionById(id);
            if (collection == null) {
                return notFound(id);
            }
            return Response.ok(toDto(collection)).build();
        }
    }

    @Override
    public Response getResolvedCollection(final int id, final String timespan, final String graphtype) {
        final String timespanProblem = validateOverrideTimespan(timespan);
        if (timespanProblem != null) {
            return badRequest(timespanProblem);
        }
        final GraphCollection snapshot;
        synchronized (m_configFactory) {
            final GraphCollection collection = m_configFactory.getCollectionById(id);
            if (collection == null) {
                return notFound(id);
            }
            // Resolve a copy: the legacy view page still prunes graphs from the live list.
            snapshot = toModel(toDto(collection), id);
        }
        return Response.ok(resolve(snapshot, TYPE_CUSTOM, timespan, graphtype)).build();
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
        final String problem = validate(dto);
        if (problem != null) {
            return badRequest(problem);
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
            return Response.created(location).entity(toDto(saved)).build();
        }
    }

    @Override
    public Response updateCollection(final SecurityContext securityContext, final int id, final GraphCollectionDto dto) {
        final Response denied = denyReadOnly(securityContext);
        if (denied != null) {
            return denied;
        }
        final String problem = validate(dto);
        if (problem != null) {
            return badRequest(problem);
        }
        synchronized (m_configFactory) {
            if (m_configFactory.getCollectionById(id) == null) {
                return notFound(id);
            }
            m_configFactory.setCollection(id, toModel(dto, id));
            try {
                m_configFactory.saveCurrent();
            } catch (final Exception e) {
                reloadQuietly();
                return saveFailed(e);
            }
            final GraphCollection saved = m_configFactory.getCollectionById(id);
            return Response.ok(toDto(saved)).build();
        }
    }

    @Override
    public Response deleteCollection(final SecurityContext securityContext, final int id) {
        final Response denied = denyReadOnly(securityContext);
        if (denied != null) {
            return denied;
        }
        synchronized (m_configFactory) {
            final GraphCollection collection = m_configFactory.getCollectionById(id);
            if (collection == null) {
                return notFound(id);
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
    public Response addGraph(final SecurityContext securityContext, final int id, final Integer index, final GraphCollectionGraphDto graphDto) {
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
            final int size = collection.getGraphs().size();
            if (index != null && (index < 0 || index > size)) {
                return badRequest("index must be between 0 and " + size + ".");
            }
            final Graph graph = toModel(graphDto);
            if (index == null) {
                collection.addGraph(graph);
            } else {
                collection.addGraph(index, graph);
            }
            m_configFactory.setCollection(id, collection);
            try {
                m_configFactory.saveCurrent();
            } catch (final Exception e) {
                reloadQuietly();
                return saveFailed(e);
            }
            final GraphCollection saved = m_configFactory.getCollectionById(id);
            return Response.ok(toDto(saved)).build();
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
            dto.setKind(KIND_PREFAB);
            dto.setTitle(graph.getTitle());
            dto.setExtlink(graph.getExtlink().orElse(null));
            dto.setTimespan(timespan != null ? timespan : graph.getTimespan());
            dto.setGraphtype(graphtype != null ? graphtype : graph.getGraphtype());
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
            dto.setValid(resource != null && available != null);
            dto.setError(error);

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
                dto.setError(error != null ? error : "Unknown timespan '" + dto.getTimespan() + "'.");
            }
            dto.setStart(begin.getTimeInMillis());
            dto.setEnd(end.getTimeInMillis());

            resolved.getGraphs().add(dto);
        }
        resolved.setGraphTypes(new ArrayList<>(graphTypes));
        return resolved;
    }

    static int defaultGraphsPerLine() {
        final Integer configured = SystemProperties.getInteger(DEFAULT_GRAPHS_PER_LINE_PROPERTY, 1);
        return configured == null || configured < 1 ? 1 : configured;
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

    static String validate(final GraphCollectionDto dto) {
        if (dto == null) {
            return "A graph collection body is required.";
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return "A title is required.";
        }
        if (dto.getTitle().trim().length() > MAX_TITLE_LENGTH) {
            return "The title must be at most " + MAX_TITLE_LENGTH + " characters.";
        }
        if (dto.getGraphsPerLine() != null && dto.getGraphsPerLine() < 0) {
            return "graphsPerLine must be 0 or greater.";
        }
        int index = 0;
        for (final GraphCollectionGraphDto graph : dto.getGraphs()) {
            final String problem = validateGraph(graph, index++);
            if (problem != null) {
                return problem;
            }
        }
        return null;
    }

    /** index is the zero-based position in a collection body, or null for a single graph on its own. */
    static String validateGraph(final GraphCollectionGraphDto graph, final Integer index) {
        final String where = index == null ? "" : "Graph " + (index + 1) + ": ";
        if (graph == null) {
            return where + (index == null ? "A graph body is required." : "the graph is empty.");
        }
        final String kind = kindOf(graph);
        if (KIND_ADHOC.equals(kind)) {
            return where + "ad-hoc graphs are not supported in collections yet.";
        }
        if (!KIND_PREFAB.equals(kind)) {
            return where + "unknown kind '" + kind + "'; expected '" + KIND_PREFAB + "'.";
        }
        if (StringUtils.isBlank(graph.getGraphtype())) {
            return where + "a graphtype is required.";
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
        final boolean hasLegacyLocator = !StringUtils.isBlank(graph.getNodeId()) || !StringUtils.isBlank(graph.getNodeSource()) || !StringUtils.isBlank(graph.getDomain());
        if (StringUtils.isBlank(graph.getResourceId()) && !hasLegacyLocator) {
            return where + "a resourceId is required.";
        }
        if (!StringUtils.isBlank(graph.getResourceId()) && !isParseableResourceId(graph.getResourceId())) {
            return where + "'" + graph.getResourceId() + "' is not a well-formed resourceId.";
        }
        if (!StringUtils.isBlank(graph.getExtlink()) && !isSafeLink(graph.getExtlink())) {
            return where + "extlink must be a relative link or an http or https URL.";
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
     * carry) and web URLs are fine; anything with another scheme, javascript: above all, is not.
     */
    private static boolean isSafeLink(final String value) {
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
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (final URISyntaxException e) {
            return false;
        }
    }

    /** An absent kind means prefab, so entries written before the field existed keep working. */
    private static String kindOf(final GraphCollectionGraphDto graph) {
        return StringUtils.isBlank(graph.getKind()) ? KIND_PREFAB : graph.getKind().trim();
    }

    static GraphCollection toModel(final GraphCollectionDto dto, final Integer id) {
        final GraphCollection collection = new GraphCollection();
        collection.setId(id);
        collection.setTitle(dto.getTitle().trim());
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
        graph.setGraphtype(dto.getGraphtype().trim());
        graph.setTimespan(dto.getTimespan());
        graph.setExtlink(dto.getExtlink() == null ? null : dto.getExtlink().trim());
        graph.setNodeId(dto.getNodeId());
        graph.setNodeSource(dto.getNodeSource());
        graph.setDomain(dto.getDomain());
        graph.setInterfaceId(dto.getInterfaceId());
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

    static GraphCollectionGraphDto toDto(final Graph graph) {
        final GraphCollectionGraphDto dto = new GraphCollectionGraphDto();
        dto.setKind(KIND_PREFAB);
        dto.setTitle(graph.getTitle());
        dto.setResourceId(graph.getResourceId().orElse(null));
        dto.setGraphtype(graph.getGraphtype());
        dto.setTimespan(graph.getTimespan());
        dto.setExtlink(graph.getExtlink().orElse(null));
        dto.setNodeId(graph.getNodeId().orElse(null));
        dto.setNodeSource(graph.getNodeSource().orElse(null));
        dto.setDomain(graph.getDomain().orElse(null));
        dto.setInterfaceId(graph.getInterfaceId().orElse(null));
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

    private static Response denyReadOnly(final SecurityContext securityContext) {
        if (securityContext != null && securityContext.isUserInRole(Authentication.ROLE_READONLY)) {
            return Response.status(Status.FORBIDDEN).entity("Graph collections are read-only for this user.").build();
        }
        return null;
    }

    private static Response notFound(final int id) {
        return Response.status(Status.NOT_FOUND).entity("Graph collection " + id + " was not found.").build();
    }

    private static Response badRequest(final String message) {
        return Response.status(Status.BAD_REQUEST).entity(message).build();
    }

    private static Response saveFailed(final Exception e) {
        LOG.error("Can't save the graph collections configuration", e);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity("Can't save the graph collections configuration: " + e.getMessage()).build();
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
