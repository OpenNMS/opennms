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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
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
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.config.wsman.Attrib;
import org.opennms.netmgt.config.wsman.Collection;
import org.opennms.netmgt.config.wsman.Group;
import org.opennms.netmgt.config.wsman.Rrd;
import org.opennms.netmgt.config.wsman.SystemDefinition;
import org.opennms.netmgt.config.wsman.WsmanDatacollectionConfig;
import org.opennms.netmgt.config.wsman.credentials.Definition;
import org.opennms.netmgt.config.wsman.credentials.Range;
import org.opennms.netmgt.config.wsman.credentials.WsmanConfig;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.dao.WSManDataCollectionConfigDao;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.web.svclayer.api.RequisitionAccessService;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.model.WsmanConfigDto;
import org.opennms.web.rest.v2.model.WsmanConfigUpdate;
import org.opennms.web.rest.v2.model.WsmanConfigUpdate.DefinitionUpdate;
import org.opennms.web.rest.v2.model.WsmanConfigUpdate.RangeUpdate;
import org.opennms.web.rest.v2.model.WsmanConfigUpdate.SettingsUpdate;
import org.opennms.web.rest.v2.model.WsmanDataCollectionDto;
import org.opennms.web.rest.v2.model.WsmanDataCollectionFileUpdate;
import org.opennms.web.rest.v2.model.WsmanDataCollectionFileUpdate.AttributeUpdate;
import org.opennms.web.rest.v2.model.WsmanDataCollectionFileUpdate.CollectionUpdate;
import org.opennms.web.rest.v2.model.WsmanDataCollectionFileUpdate.GroupUpdate;
import org.opennms.web.rest.v2.model.WsmanDataCollectionFileUpdate.SystemDefinitionUpdate;
import org.opennms.web.rest.v2.model.WsmanStatusDto;
import org.opennms.web.rest.v2.model.WsmanSyncResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    // a new drop-in file name: plain characters, no path separators, .xml
    private static final Pattern DROP_IN_NAME = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]*\\.xml$");
    private static final Pattern RRA = Pattern.compile("^RRA:(AVERAGE|MIN|MAX|LAST):[0-9.]+:[0-9]+:[0-9]+$", Pattern.CASE_INSENSITIVE);

    @Autowired
    private WSManConfigDao wsManConfigDao;

    @Autowired
    private WSManDataCollectionConfigDao wsManDataCollectionConfigDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    @Autowired
    private OutageDao outageDao;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private RequisitionAccessService requisitionAccessService;

    @Autowired
    private EventProxy eventProxy;

    // the service provisioning gives a WS-Man server, and what the poller checks
    private static final String WSMAN_SERVICE = "WS-Man";
    private static final Pattern REQUISITION_NAME = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._-]*$");

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
    @javax.ws.rs.Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(readOnly = true)
    @Operation(summary = "Get what the poller sees per WS-Man server definition", description = "For every monitored service of the given type (WS-Man by default), which server definition its address matches, whether it has an open outage, when it last responded, and how many are provisioned but not polled because no poller package covers them", operationId = "WsmanConfigRestServiceGetStatus")
    public Response getStatus(@Context final SecurityContext securityContext, @QueryParam("service") @DefaultValue("WS-Man") final String serviceName) {
        requireAdmin(securityContext);
        final List<Definition> definitions = readConfig().config.getDefinition();
        final WsmanStatusDto status = new WsmanStatusDto(serviceName, definitions.size());
        for (int i = 0; i < definitions.size(); i++) {
            final Definition def = definitions.get(i);
            final WsmanStatusDto.DefinitionStatus ds = status.getDefinitions().get(i);
            ds.setRequisition(def.getRequisition());
            // a specific address is provisioned when some node carries it as an
            // interface; with a requisition linked, that node must belong to it
            for (final String specific : def.getSpecific()) {
                boolean provisioned = false;
                for (final OnmsIpInterface iface : ipInterfaceDao.findByIpAddress(specific)) {
                    if (def.getRequisition() == null || def.getRequisition().equals(iface.getNode().getForeignSource())) {
                        provisioned = true;
                        break;
                    }
                }
                ds.countSpecific(provisioned);
            }
        }
        final Set<Integer> downServiceIds = outageDao.currentOutagesByServiceId().keySet();
        for (final OnmsMonitoredService svc : monitoredServiceDao.findByType(serviceName)) {
            if (svc.getIpAddress() == null) {
                continue;
            }
            final int index = matchingDefinition(definitions, svc.getIpAddress());
            final WsmanStatusDto.Bucket bucket = index < 0 ? status.getDefaults() : status.getDefinitions().get(index);
            // anything but A is a service the poller does not check: provisioning
            // marks a service N when no poller package covers it, F when forced off
            if ("A".equals(svc.getStatus())) {
                bucket.count(downServiceIds.contains(svc.getId()), svc.getLastGood());
            } else {
                bucket.countUnpolled();
            }
        }
        return Response.ok(status).build();
    }

    // The same first-match rule as WSManConfigDaoJaxb.getAgentConfig, returning
    // the definition's position so the page can put the count on its row.
    static int matchingDefinition(final List<Definition> definitions, final InetAddress address) {
        final String text = InetAddressUtils.str(address);
        for (int i = 0; i < definitions.size(); i++) {
            final Definition def = definitions.get(i);
            for (final String specific : def.getSpecific()) {
                if (address.equals(InetAddressUtils.addr(specific))) {
                    return i;
                }
            }
            for (final Range range : def.getRange()) {
                if (InetAddressUtils.isInetAddressInRange(text, range.getBegin(), range.getEnd())) {
                    return i;
                }
            }
            for (final String ipMatch : def.getIpMatch()) {
                try {
                    if (IPLike.matches(text, ipMatch)) {
                        return i;
                    }
                } catch (final RuntimeException e) {
                    // a malformed hand-edited pattern matches nothing, as in the daemon
                }
            }
        }
        return -1;
    }

    @POST
    @javax.ws.rs.Path("definitions/{index}/sync")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Provision a server definition's servers into its requisition", description = "Adds each specific address the linked requisition does not yet hold as a node carrying the WS-Man service and imports the requisition; adds each range as a scheduled discovery range with the requisition as foreign source and asks Discovery to reload. IPLIKE patterns cannot be provisioned and are reported as skipped. Never removes anything.", operationId = "WsmanConfigRestServiceSyncDefinition")
    public Response syncDefinition(@Context final SecurityContext securityContext, @PathParam("index") final int index) {
        requireAdmin(securityContext);
        final List<Definition> definitions = readConfig().config.getDefinition();
        if (index < 0 || index >= definitions.size()) {
            throw badRequest("There is no server definition " + (index + 1) + "; reload the page.");
        }
        final Definition def = definitions.get(index);
        final String foreignSource = def.getRequisition();
        if (foreignSource == null || foreignSource.isBlank()) {
            throw badRequest("Server definition " + (index + 1) + " is not linked to a requisition.");
        }
        final WsmanSyncResultDto result = new WsmanSyncResultDto(foreignSource);

        Requisition requisition = requisitionAccessService.getRequisition(foreignSource);
        if (requisition == null) {
            requisition = new Requisition(foreignSource);
            requisitionAccessService.addOrReplaceRequisition(requisition);
        }
        final Set<String> present = new HashSet<>();
        for (final RequisitionNode node : requisition.getNodes()) {
            for (final RequisitionInterface iface : node.getInterfaces()) {
                if (iface.getIpAddr() != null) {
                    present.add(InetAddresses.toAddrString(iface.getIpAddr()));
                }
            }
        }
        for (final String specific : def.getSpecific()) {
            final String address = InetAddresses.toAddrString(InetAddresses.forString(specific.trim()));
            if (present.contains(address)) {
                result.countExistingNode();
                continue;
            }
            final RequisitionInterface iface = new RequisitionInterface();
            iface.setIpAddr(address);
            iface.setManaged(true);
            iface.setStatus(1);
            iface.setSnmpPrimary(PrimaryType.NOT_ELIGIBLE);
            iface.putMonitoredService(new RequisitionMonitoredService(WSMAN_SERVICE));
            final RequisitionNode node = new RequisitionNode();
            node.setForeignId(address);
            node.setNodeLabel(address);
            node.putInterface(iface);
            requisitionAccessService.addOrReplaceNode(foreignSource, node);
            result.getAddedNodes().add(address);
            present.add(address);
        }
        if (!result.getAddedNodes().isEmpty()) {
            // newly added only: existing nodes keep their own rescan schedule
            requisitionAccessService.importRequisition(foreignSource, "false");
            result.setImportRequested(true);
        }

        if (!def.getRange().isEmpty()) {
            final DiscoveryConfigFactory factory = DiscoveryConfigFactory.getInstance();
            final DiscoveryConfiguration config = factory.getConfiguration();
            for (final Range range : def.getRange()) {
                final boolean exists = config.getIncludeRanges().stream().anyMatch(r ->
                        range.getBegin().equals(r.getBegin()) && range.getEnd().equals(r.getEnd())
                        && foreignSource.equals(r.getForeignSource().orElse(null)));
                if (exists) {
                    result.countExistingRange();
                    continue;
                }
                final IncludeRange include = new IncludeRange(range.getBegin(), range.getEnd());
                include.setForeignSource(foreignSource);
                config.addIncludeRange(include);
                result.getAddedRanges().add(range.getBegin() + " - " + range.getEnd());
            }
            if (!result.getAddedRanges().isEmpty()) {
                try {
                    factory.saveConfiguration(config);
                } catch (final IOException e) {
                    throw new WebApplicationException("Unable to save the discovery configuration: " + e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
                }
                try {
                    eventProxy.send(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "Web UI")
                            .addParam(EventConstants.PARM_DAEMON_NAME, "Discovery").getEvent());
                    result.setDiscoveryReloadRequested(true);
                } catch (final EventProxyException e) {
                    // the ranges are saved; Discovery picks them up on its next reload
                }
            }
        }
        result.getSkippedPatterns().addAll(def.getIpMatch());
        return Response.ok(result).build();
    }

    @GET
    @javax.ws.rs.Path("data-collection")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get the WS-Man data collection configuration", description = "Collections, groups and system definitions from wsman-datacollection-config.xml and wsman-datacollection.d, each tagged with its source file", operationId = "WsmanConfigRestServiceGetDataCollection")
    public Response getDataCollection(@Context final SecurityContext securityContext) {
        requireAdmin(securityContext);
        return Response.ok(readDataCollection()).build();
    }

    // the file is a query parameter: a ".xml" path segment would be taken by
    // CXF as a media-type suffix and negotiated as XML
    @PUT
    @javax.ws.rs.Path("data-collection")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Replace one WS-Man data collection file", description = "Rewrites wsman-datacollection-config.xml or one drop-in under wsman-datacollection.d (named by the file query parameter) from the given collections, groups and system definitions. The request must carry the file's version from GET (omit it to create a new drop-in). Names must stay unique across all files and every reference must resolve.", operationId = "WsmanConfigRestServiceUpdateDataCollectionFile")
    public Response updateDataCollectionFile(@Context final SecurityContext securityContext, @QueryParam("file") final String fileName, final WsmanDataCollectionFileUpdate update) {
        requireAdmin(securityContext);
        if (fileName == null || fileName.isBlank()) {
            throw badRequest("The file query parameter names the data collection file to replace.");
        }
        if (update == null || update.getCollections() == null || update.getGroups() == null || update.getSystemDefinitions() == null) {
            throw badRequest("The collections, groups and systemDefinitions lists are all required; send an empty list to remove that kind from the file.");
        }
        synchronized (this) {
            final Map<Path, LoadedDataCollection> files = readDataCollectionFiles();
            final Path target = resolveDataCollectionFile(fileName, files);
            final LoadedDataCollection existing = files.get(target);
            if (existing == null) {
                if (update.getVersion() != null && !update.getVersion().isBlank()) {
                    throw badRequest("There is no file named " + fileName + "; omit the version to create it.");
                }
            } else if (update.getVersion() == null || !existing.version.equals(update.getVersion())) {
                throw new WebApplicationException(Response.status(Status.CONFLICT).type(MediaType.TEXT_PLAIN)
                        .entity(fileName + " changed since it was loaded; reload the page and apply the change again.").build());
            }

            final WsmanDatacollectionConfig next = toDataCollectionConfig(update, existing == null ? null : existing.config, target.equals(dataCollectionHome().resolve(DATA_COLLECTION_ROOT)));
            final Map<Path, WsmanDatacollectionConfig> all = new LinkedHashMap<>();
            for (final Map.Entry<Path, LoadedDataCollection> e : files.entrySet()) {
                all.put(e.getKey(), e.getKey().equals(target) ? next : e.getValue().config);
            }
            if (existing == null) {
                all.put(target, next);
            }
            validateDataCollectionReferences(all);
            writeDataCollectionFile(target, next);
            return Response.ok(readDataCollection()).build();
        }
    }

    private static final class LoadedDataCollection {
        private final WsmanDatacollectionConfig config;
        private final String version;

        private LoadedDataCollection(final WsmanDatacollectionConfig config, final String version) {
            this.config = config;
            this.version = version;
        }
    }

    private Path dataCollectionHome() {
        return wsManDataCollectionConfigDao instanceof AbstractMergingJaxbConfigDao
                ? ((AbstractMergingJaxbConfigDao<?, ?>) wsManDataCollectionConfigDao).getOpennmsHome()
                : Paths.get(System.getProperty("opennms.home"));
    }

    private WsmanDataCollectionDto readDataCollection() {
        final WsmanDataCollectionDto dto = new WsmanDataCollectionDto();
        for (final Map.Entry<Path, LoadedDataCollection> e : readDataCollectionFiles().entrySet()) {
            dto.addSource(e.getKey().getFileName().toString(), e.getValue().version, e.getValue().config);
        }
        return dto;
    }

    private Map<Path, LoadedDataCollection> readDataCollectionFiles() {
        final Map<Path, LoadedDataCollection> files = new LinkedHashMap<>();
        for (final Path file : dataCollectionFiles()) {
            try {
                final byte[] bytes = Files.readAllBytes(file);
                try (Reader reader = new StringReader(new String(bytes, StandardCharsets.UTF_8))) {
                    files.put(file, new LoadedDataCollection(JaxbUtils.unmarshal(WsmanDatacollectionConfig.class, reader), digest(bytes)));
                }
            } catch (final IOException | RuntimeException e) {
                // a hand-edited drop-in that no longer parses must name itself, not surface as a bare 500
                throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
                        .entity("Unable to read " + file.getFileName() + ": " + e.getMessage()).build());
            }
        }
        return files;
    }

    // An existing file by its name, or a new drop-in under wsman-datacollection.d.
    private Path resolveDataCollectionFile(final String fileName, final Map<Path, LoadedDataCollection> files) {
        for (final Path file : files.keySet()) {
            if (file.getFileName().toString().equals(fileName)) {
                return file;
            }
        }
        if (!DROP_IN_NAME.matcher(fileName).matches()) {
            throw badRequest("A new data collection file must be a plain name ending in .xml, e.g. custom.xml.");
        }
        return dataCollectionHome().resolve(DATA_COLLECTION_DIR).resolve(fileName);
    }

    private void writeDataCollectionFile(final Path file, final WsmanDatacollectionConfig config) {
        final String xml;
        try {
            xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + JaxbUtils.marshal(config);
        } catch (final RuntimeException e) {
            throw badRequest("The file does not satisfy wsman-datacollection.xsd: " + rootMessage(e));
        }
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, xml.getBytes(StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new WebApplicationException("Unable to write " + file + ": " + e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
        }
    }

    // --- data collection mapping and validation --------------------------

    private static WsmanDatacollectionConfig toDataCollectionConfig(final WsmanDataCollectionFileUpdate u, final WsmanDatacollectionConfig existing, final boolean isRoot) {
        final WsmanDatacollectionConfig c = new WsmanDatacollectionConfig();
        // the repository lives on the root file; keep it unless the request sets it
        if (isRoot) {
            final String repo = u.getRrdRepository() != null && !u.getRrdRepository().isBlank() ? u.getRrdRepository()
                    : existing == null ? null : existing.getRrdRepository();
            c.setRrdRepository(repo);
        }
        for (final CollectionUpdate cu : u.getCollections()) {
            final String label = "Collection " + requireName(cu.getName(), "collection");
            final Collection col = new Collection();
            col.setName(cu.getName().trim());
            if (cu.getRrdStep() == null || cu.getRrdStep() < 1) {
                throw badRequest(label + " needs an RRD step of at least 1 second.");
            }
            if (cu.getRras() == null || cu.getRras().isEmpty()) {
                throw badRequest(label + " needs at least one RRA.");
            }
            final Rrd rrd = new Rrd();
            rrd.setStep(cu.getRrdStep());
            for (final String rra : cu.getRras()) {
                if (rra == null || !RRA.matcher(rra.trim()).matches()) {
                    throw badRequest(label + " has an invalid RRA: " + rra + " (expected RRA:AVERAGE|MIN|MAX|LAST:xff:steps:rows).");
                }
                rrd.getRra().add(rra.trim());
            }
            col.setRrd(rrd);
            if (cu.isIncludeAllSystemDefinitions()) {
                col.setIncludeAllSystemDefinitions(new Collection.IncludeAllSystemDefinitions());
            }
            for (final String name : orEmpty(cu.getIncludedSystemDefinitions())) {
                col.getIncludeSystemDefinition().add(requireName(name, "included system definition of " + label));
            }
            if (!cu.isIncludeAllSystemDefinitions() && col.getIncludeSystemDefinition().isEmpty()) {
                throw badRequest(label + " must include all system definitions or name at least one; otherwise it collects nothing.");
            }
            c.getCollection().add(col);
        }
        for (final GroupUpdate gu : u.getGroups()) {
            final String label = "Group " + requireName(gu.getName(), "group");
            final Group g = new Group();
            g.setName(gu.getName().trim());
            g.setResourceType(requireName(gu.getResourceType(), "resource type of " + label));
            g.setResourceUri(requireName(gu.getResourceUri(), "resource URI of " + label));
            g.setDialect(blankToNull(gu.getDialect()));
            g.setFilter(blankToNull(gu.getFilter()));
            if (gu.getAttributes() == null || gu.getAttributes().isEmpty()) {
                throw badRequest(label + " needs at least one attribute.");
            }
            for (final AttributeUpdate au : gu.getAttributes()) {
                final Attrib a = new Attrib();
                a.setName(requireName(au.getName(), "attribute name in " + label));
                a.setAlias(requireName(au.getAlias(), "attribute alias in " + label));
                final AttributeType type = au.getType() == null ? null : AttributeType.parse(au.getType().trim());
                if (type == null) {
                    throw badRequest(label + " has an attribute with an unknown type " + au.getType() + "; use gauge, counter or string.");
                }
                a.setType(type);
                a.setIndexOf(blankToNull(au.getIndexOf()));
                a.setFilter(blankToNull(au.getFilter()));
                g.getAttrib().add(a);
            }
            c.getGroup().add(g);
        }
        for (final SystemDefinitionUpdate su : u.getSystemDefinitions()) {
            final String label = "System definition " + requireName(su.getName(), "system definition");
            final SystemDefinition sd = new SystemDefinition();
            sd.setName(su.getName().trim());
            for (final String rule : orEmpty(su.getRules())) {
                sd.getRule().add(requireName(rule, "rule of " + label));
            }
            for (final String group : orEmpty(su.getIncludedGroups())) {
                sd.getIncludeGroup().add(requireName(group, "included group of " + label));
            }
            if (sd.getRule().isEmpty() || sd.getIncludeGroup().isEmpty()) {
                throw badRequest(label + " needs at least one rule and at least one included group.");
            }
            c.getSystemDefinition().add(sd);
        }
        return c;
    }

    // Names are unique per kind across every file, and every reference resolves
    // across every file, so a rename or delete in one drop-in cannot strand an
    // object in another.
    private static void validateDataCollectionReferences(final Map<Path, WsmanDatacollectionConfig> all) {
        final Map<String, String> collectionOwner = new LinkedHashMap<>();
        final Map<String, String> groupOwner = new LinkedHashMap<>();
        final Map<String, String> sysDefOwner = new LinkedHashMap<>();
        for (final Map.Entry<Path, WsmanDatacollectionConfig> e : all.entrySet()) {
            final String file = e.getKey().getFileName().toString();
            for (final Collection c : e.getValue().getCollection()) {
                claim(collectionOwner, "collection", c.getName(), file);
            }
            for (final Group g : e.getValue().getGroup()) {
                claim(groupOwner, "group", g.getName(), file);
            }
            for (final SystemDefinition s : e.getValue().getSystemDefinition()) {
                claim(sysDefOwner, "system definition", s.getName(), file);
            }
        }
        for (final WsmanDatacollectionConfig cfg : all.values()) {
            for (final SystemDefinition s : cfg.getSystemDefinition()) {
                for (final String group : s.getIncludeGroup()) {
                    if (!groupOwner.containsKey(group)) {
                        throw badRequest("System definition " + s.getName() + " includes group " + group + ", which does not exist in any file.");
                    }
                }
            }
            for (final Collection c : cfg.getCollection()) {
                for (final String sd : c.getIncludeSystemDefinition()) {
                    if (!sysDefOwner.containsKey(sd)) {
                        throw badRequest("Collection " + c.getName() + " includes system definition " + sd + ", which does not exist in any file.");
                    }
                }
            }
        }
    }

    private static void claim(final Map<String, String> owners, final String kind, final String name, final String file) {
        final String other = owners.putIfAbsent(name, file);
        if (other != null) {
            throw badRequest("A " + kind + " named " + name + " already exists in " + other + "; names must be unique across all files.");
        }
    }

    private static String requireName(final String value, final String what) {
        if (value == null || value.isBlank()) {
            throw badRequest("A " + what + " is required.");
        }
        if (value.chars().anyMatch(Character::isISOControl)) {
            throw badRequest("The " + what + " must not contain control characters.");
        }
        return value.trim();
    }

    private static <T> List<T> orEmpty(final List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    // The same file set, in the same order, that the merging DAO folds
    // together: the root file first, then the drop-ins sorted by name.
    private List<Path> dataCollectionFiles() {
        final Path home = dataCollectionHome();
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
        final String requisition = blankToNull(u.getRequisition());
        if (requisition != null && !REQUISITION_NAME.matcher(requisition.trim()).matches()) {
            throw badRequest(label + " has an invalid requisition name: " + requisition.trim() + " (letters, digits, dots, dashes and underscores).");
        }
        d.setRequisition(requisition == null ? null : requisition.trim());

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
