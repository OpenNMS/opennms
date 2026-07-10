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
package org.opennms.netmgt.collectd;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.vmware.vijava.VmwarePerformanceValues;
import org.opennms.netmgt.collection.api.AbstractRemoteServiceCollector;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.config.vmware.vijava.Attrib;
import org.opennms.netmgt.config.vmware.vijava.VmwareCollection;
import org.opennms.netmgt.config.vmware.vijava.VmwareGroup;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.vmware.VmwareConfigDao;
import org.opennms.netmgt.dao.vmware.VmwareDatacollectionConfigDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.service.vmware.VmwareImporter;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.protocols.vmware.VmwareViJavaAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Strings;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ManagedEntity;

/**
 * The Class VmwareCollector
 * <p/>
 * This class is used to collect data from a Vmware vCenter server.
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public class VmwareCollector extends AbstractRemoteServiceCollector {
    /**
     * logging for VMware data collection
     */
    private static final Logger logger = LoggerFactory.getLogger(VmwareCollector.class);

    private static final Map<String, Class<?>> TYPE_MAP = Collections.unmodifiableMap(Stream.of(
            new SimpleEntry<>(VmwareImporter.VMWARE_COLLECTION_KEY, VmwareCollection.class),
            new SimpleEntry<>(VmwareImporter.VMWARE_SERVER_KEY, VmwareServer.class))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    /** Resource type that triggers datastore-capacity dispatch instead of the PerformanceManager path. */
    static final String DATASTORE_RESOURCE_TYPE = "vmwareDatastoreCapacity";

    // Per-attribute extractors against Datastore.summary. Package-private for tests.
    static final Map<String, ToLongFunction<DatastoreSummary>> DATASTORE_NUMERIC_EXTRACTORS;
    static final Map<String, Function<DatastoreSummary, String>> DATASTORE_STRING_EXTRACTORS;

    static {
        final Map<String, ToLongFunction<DatastoreSummary>> numeric = new HashMap<>();
        numeric.put("capacity", DatastoreSummary::getCapacity);
        numeric.put("freeSpace", DatastoreSummary::getFreeSpace);
        numeric.put("used", s -> s.getCapacity() - s.getFreeSpace());
        numeric.put("usedPct", s -> {
            final long cap = s.getCapacity();
            return cap > 0L ? ((cap - s.getFreeSpace()) * 100L) / cap : 0L;
        });
        numeric.put("uncommitted", s -> {
            final Long u = s.getUncommitted();
            return u == null ? 0L : u.longValue();
        });
        numeric.put("overcommittedBytes", s -> {
            final long cap = s.getCapacity();
            final long used = cap - s.getFreeSpace();
            final Long u = s.getUncommitted();
            final long unc = u == null ? 0L : u.longValue();
            return Math.max(0L, used + unc - cap);
        });
        numeric.put("accessible", s -> s.isAccessible() ? 1L : 0L);
        numeric.put("multipleHostAccess", s -> {
            final Boolean b = s.getMultipleHostAccess();
            return b != null && b ? 1L : 0L;
        });
        DATASTORE_NUMERIC_EXTRACTORS = Collections.unmodifiableMap(numeric);

        final Map<String, Function<DatastoreSummary, String>> string = new HashMap<>();
        string.put("name", DatastoreSummary::getName);
        string.put("type", DatastoreSummary::getType);
        string.put("url", DatastoreSummary::getUrl);
        DATASTORE_STRING_EXTRACTORS = Collections.unmodifiableMap(string);
    }

    /**
     * the node dao object for retrieving assets
     */
    private NodeDao m_nodeDao = null;

    private TransactionTemplate m_transactionTemplate = null;

    /**
     * the config dao
     */
    private VmwareDatacollectionConfigDao m_vmwareDatacollectionConfigDao;

    /**
     * the config dao
     */
    private VmwareConfigDao m_vmwareConfigDao = null;

    public VmwareCollector() {
        super(TYPE_MAP);
    }

    /**
     * Initializes this instance with a given parameter map.
     *
     * @throws CollectionInitializationException
     *
     */
    @Override
    public void initialize() throws CollectionInitializationException {
        if (m_nodeDao == null) {
            m_nodeDao = BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class);
        }

        if (m_nodeDao == null) {
            logger.error("Node dao should be a non-null value.");
        }

        if (m_vmwareDatacollectionConfigDao == null) {
            m_vmwareDatacollectionConfigDao = BeanUtils.getBean("daoContext", "vmwareDatacollectionConfigDao", VmwareDatacollectionConfigDao.class);
        }

        if (m_vmwareDatacollectionConfigDao == null) {
            logger.error("vmwareDatacollectionConfigDao should be a non-null value.");
        }

        if (m_vmwareConfigDao == null) {
            m_vmwareConfigDao = BeanUtils.getBean("daoContext", "vmwareConfigDao", VmwareConfigDao.class);
        }

        if (m_transactionTemplate == null) {
            m_transactionTemplate = BeanUtils.getBean("daoContext", "transactionTemplate", TransactionTemplate.class);
        }
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters) {
        final Map<String, Object> runtimeAttributes = new HashMap<>();

        m_transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {
                final OnmsNode onmsNode = m_nodeDao.get(agent.getNodeId());
                if (onmsNode == null) {
                    throw new IllegalArgumentException(String.format("VmwareCollector: No node found with id: %d", agent.getNodeId()));
                }

                // retrieve the metadata
                final String vmwareManagementServer = VmwareImporter.getManagementServer(onmsNode);

                if (Strings.isNullOrEmpty(vmwareManagementServer)) {
                    throw new IllegalArgumentException(String.format("VmwareCollector: No management server is set on node with id %d.",  onmsNode.getId()));
                }
                runtimeAttributes.put(VmwareImporter.METADATA_MANAGEMENT_SERVER, vmwareManagementServer);

                final String vmwareManagedObjectId = onmsNode.getForeignId();
                if (Strings.isNullOrEmpty(vmwareManagedObjectId)) {
                    throw new IllegalArgumentException(String.format("VmwareCollector: No foreign id is set on node with id %d.",  onmsNode.getId()));
                }
                runtimeAttributes.put(VmwareImporter.METADATA_MANAGED_OBJECT_ID, vmwareManagedObjectId);

                // retrieve the collection
                final String collectionName = ParameterMap.getKeyedString(parameters, "collection", ParameterMap.getKeyedString(parameters, "vmware-collection", null));
                final VmwareCollection collection = m_vmwareDatacollectionConfigDao.getVmwareCollection(collectionName);
                if (collection == null) {
                    throw new IllegalArgumentException(String.format("VmwareCollector: No collection found with name '%s'.",  collectionName));
                }
                runtimeAttributes.put(VmwareImporter.VMWARE_COLLECTION_KEY, collection);

                // retrieve the server configuration
                final Map<String, VmwareServer> serverMap = m_vmwareConfigDao.getServerMap();
                if (serverMap == null) {
                    throw new IllegalStateException(String.format("VmwareCollector: Error getting vmware-config.xml's server map."));
                }
                final VmwareServer vmwareServer = serverMap.get(vmwareManagementServer);
                if (vmwareServer == null) {
                    throw new IllegalStateException(String.format("VmwareCollector: Error getting credentials for VMware management server: %s", vmwareManagementServer));
                }
                runtimeAttributes.put(VmwareImporter.VMWARE_SERVER_KEY, Interpolator.pleaseInterpolate(vmwareServer));

                return null;
            }
        });

        return runtimeAttributes;
    }

    /**
     * This method collect the data for a given collection agent.
     *
     * @param agent      the collection agent
     * @param parameters the parameters map
     * @return the generated collection set
     * @throws CollectionException
     */
    @Override
    public CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) throws CollectionException {
        final VmwareCollection collection = (VmwareCollection) parameters.get(VmwareImporter.VMWARE_COLLECTION_KEY);
        final String vmwareManagementServer = (String) parameters.get(VmwareImporter.METADATA_MANAGEMENT_SERVER);
        final String vmwareManagedObjectId = (String) parameters.get(VmwareImporter.METADATA_MANAGED_OBJECT_ID);
        final VmwareServer vmwareServer = (VmwareServer) parameters.get(VmwareImporter.VMWARE_SERVER_KEY);
        final CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        builder.withStatus(CollectionStatus.FAILED);

        if (collection.getVmwareGroup().length < 1) {
            logger.info("No groups to collect. Returning empty collection set.");
            builder.withStatus(CollectionStatus.SUCCEEDED);
            return builder.build();
        }

        try (final VmwareViJavaAccess vmwareViJavaAccess = createVmwareViJavaAccess(vmwareServer)) {
            vmwareViJavaAccess.connect(ParameterMap.getKeyedInteger(parameters, "timeout", VmwareViJavaAccess.DEFAULT_TIMEOUT));
            final ManagedEntity managedEntity = vmwareViJavaAccess.getManagedEntityByManagedObjectId(vmwareManagedObjectId);
            VmwarePerformanceValues vmwarePerformanceValues = null;
            try {
                vmwarePerformanceValues = vmwareViJavaAccess.queryPerformanceValues(managedEntity);
            } catch (RemoteException e) {
                logger.warn("Error retrieving performance values from VMware management server '" + vmwareManagementServer + "' for managed object '" + vmwareManagedObjectId + "'", e.getMessage());
                return builder.build();
            }
            for (final VmwareGroup vmwareGroup : collection.getVmwareGroup()) {
                final NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());
                if (DATASTORE_RESOURCE_TYPE.equals(vmwareGroup.getResourceType())) {
                    collectDatastoreCapacity(agent, builder, nodeResource, vmwareViJavaAccess, vmwareManagedObjectId, vmwareGroup);
                    continue;
                }
                if ("node".equalsIgnoreCase(vmwareGroup.getResourceType())) {
                    // single instance value
                    for (final Attrib attrib : vmwareGroup.getAttrib()) {
                        if (!vmwarePerformanceValues.hasSingleValue(attrib.getName())) {
                            // warning
                            logger.debug("Warning! No single value for '{}' defined as single instance attribute for node {}", attrib.getName(), agent.getNodeId());
                        } else {
                            final Long value = vmwarePerformanceValues.getValue(attrib.getName());
                            logger.debug("Storing single instance value {}='{}' for node {}",
                                    attrib.getName(), value, agent.getNodeId());
                            final AttributeType type = attrib.getType();
                            if (type.isNumeric()) {
                                builder.withNumericAttribute(nodeResource, vmwareGroup.getName(), attrib.getAlias(), value, type);
                            } else {
                                builder.withStringAttribute(nodeResource, vmwareGroup.getName(), attrib.getAlias(), String.valueOf(value));
                            }
                        }
                    }
                } else {
                    // multi instance value
                    final Set<String> instanceSet = new TreeSet<>();
                    final HashMap<String, Resource> resources = new HashMap<>();
                    for (final Attrib attrib : vmwareGroup.getAttrib()) {
                        if (!vmwarePerformanceValues.hasInstances(attrib.getName())) {
                            // warning
                            logger.debug("Warning! No multi instance value for '{}' defined as multi instance attribute for node {}", attrib.getName(), agent.getNodeId());
                        } else {
                            final Set<String> newInstances = vmwarePerformanceValues.getInstances(attrib.getName());
                            for (final String instance : newInstances) {
                                if (!instanceSet.contains(instance)) {
                                    resources.put(instance, new DeferredGenericTypeResource(nodeResource, vmwareGroup.getResourceType(), instance));
                                    instanceSet.add(instance);
                                }
                                final AttributeType type = attrib.getType();
                                final Long value = vmwarePerformanceValues.getValue(attrib.getName(), instance);
                                logger.debug("Storing multi instance value {}[{}='{}' for node {}",
                                        attrib.getName(), instance, value, agent.getNodeId());
                                if (type.isNumeric()) {
                                    builder.withNumericAttribute(resources.get(instance), vmwareGroup.getName(), attrib.getAlias(), value, type);
                                } else {
                                    builder.withStringAttribute(resources.get(instance), vmwareGroup.getName(), attrib.getAlias(), Long.toString(value));
                                }
                            }
                        }
                    }
                    for (final String instance : instanceSet) {
                        logger.debug("Storing multi instance value {}[{}='{}' for node {}",
                                vmwareGroup.getResourceType() + "Name", instance, instance, agent.getNodeId());
                        builder.withStringAttribute(resources.get(instance), vmwareGroup.getName(), vmwareGroup.getResourceType() + "Name", instance);
                    }
                }
            }
            builder.withStatus(CollectionStatus.SUCCEEDED);
        } catch (MalformedURLException e) {
            logger.warn("Error connecting VMware management server '{}': '{}' exception: {} cause: '{}'", vmwareManagementServer, e.getMessage(), e.getClass().getName(), e.getCause());
            return builder.build();
        } catch (RemoteException e) {
            logger.warn("Error connecting VMware management server '{}': '{}' exception: {} cause: '{}'", vmwareManagementServer, e.getMessage(), e.getClass().getName(), e.getCause());
            return builder.build();
        }
        return builder.build();
    }

    /**
     * Walks the datastores this HostSystem mounts and emits a multi-instance resource
     * per datastore keyed by managed object id. Attribute names recognized in the
     * configured group map directly to fields on {@code Datastore.summary}:
     *
     * Numeric: {@code capacity}, {@code freeSpace}, {@code used}, {@code usedPct},
     *          {@code uncommitted}, {@code overcommittedBytes}, {@code accessible},
     *          {@code multipleHostAccess}.
     *
     * String:  {@code name}, {@code type}, {@code url}.
     *
     * Looks the HostSystem up explicitly by moid because
     * VmwareViJavaAccess.getManagedEntityByManagedObjectId returns the abstract
     * base class. If the moid does not refer to a HostSystem on the server,
     * getDatastores() will fail and the per-datastore loop is skipped.
     */
    private void collectDatastoreCapacity(final CollectionAgent agent,
                                          final CollectionSetBuilder builder,
                                          final NodeLevelResource nodeResource,
                                          final VmwareViJavaAccess vmwareViJavaAccess,
                                          final String vmwareManagedObjectId,
                                          final VmwareGroup vmwareGroup) {
        final HostSystem hostSystem = vmwareViJavaAccess.getHostSystemByManagedObjectId(vmwareManagedObjectId);
        if (hostSystem == null) {
            logger.warn("VmwareCollector: could not resolve HostSystem for managed object id '{}'; skipping datastore group '{}'.",
                    vmwareManagedObjectId, vmwareGroup.getName());
            return;
        }

        final Datastore[] datastores;
        try {
            datastores = hostSystem.getDatastores();
        } catch (final Exception e) {
            logger.warn("VmwareCollector: error enumerating datastores for host '{}'.", hostSystem.getName(), e);
            return;
        }
        if (datastores == null || datastores.length == 0) {
            logger.debug("VmwareCollector: host '{}' has no mounted datastores; skipping group '{}'.",
                    hostSystem.getName(), vmwareGroup.getName());
            return;
        }

        for (final Datastore ds : datastores) {
            final String moid = ds.getMOR().getVal();

            final DatastoreSummary summary;
            try {
                summary = ds.getSummary();
            } catch (final Exception e) {
                logger.warn("VmwareCollector: error reading summary for datastore '{}' ({}).", ds.getName(), moid, e);
                continue;
            }
            if (summary == null) {
                logger.debug("VmwareCollector: null summary for datastore '{}' ({}); skipping.",
                        ds.getName(), moid);
                continue;
            }

            final Resource resource = new DeferredGenericTypeResource(nodeResource,
                    vmwareGroup.getResourceType(), moid);

            // Resource label property — UI uses this via resourceLabel="${<resourceType>Name}".
            final String label = summary.getName() == null ? moid : summary.getName();
            builder.withStringAttribute(resource, vmwareGroup.getName(),
                    vmwareGroup.getResourceType() + "Name", label);

            for (final Attrib attrib : vmwareGroup.getAttrib()) {
                final String name = attrib.getName();
                if (DATASTORE_NUMERIC_EXTRACTORS.containsKey(name)) {
                    try {
                        final long value = DATASTORE_NUMERIC_EXTRACTORS.get(name).applyAsLong(summary);
                        final AttributeType type = attrib.getType();
                        if (type.isNumeric()) {
                            builder.withNumericAttribute(resource, vmwareGroup.getName(),
                                    attrib.getAlias(), value, type);
                        } else {
                            logger.warn("VmwareCollector: numeric attribute '{}' (alias '{}') in group '{}' is configured with non-numeric type '{}'; skipping. Configure type as Gauge or Counter.",
                                    name, attrib.getAlias(), vmwareGroup.getName(), type);
                        }
                    } catch (final Exception e) {
                        logger.debug("VmwareCollector: failed to extract '{}' from datastore '{}' ({}): {}",
                                name, ds.getName(), moid, e.getMessage());
                    }
                } else if (DATASTORE_STRING_EXTRACTORS.containsKey(name)) {
                    try {
                        final String value = DATASTORE_STRING_EXTRACTORS.get(name).apply(summary);
                        builder.withStringAttribute(resource, vmwareGroup.getName(),
                                attrib.getAlias(), value == null ? "" : value);
                    } catch (final Exception e) {
                        logger.debug("VmwareCollector: failed to extract '{}' from datastore '{}' ({}): {}",
                                name, ds.getName(), moid, e.getMessage());
                    }
                } else {
                    logger.warn("VmwareCollector: unknown datastore attribute '{}' configured in group '{}'; ignoring.",
                            name, vmwareGroup.getName());
                }
            }
        }
    }

    /**
     * Returns the Rrd repository for this object.
     *
     * @param collectionName the collection's name
     * @return the Rrd repository
     */
    @Override
    public RrdRepository getRrdRepository(final String collectionName) {
        return m_vmwareDatacollectionConfigDao.getRrdRepository(collectionName);
    }

    /**
     * Sets the NodeDao object for this instance.
     *
     * @param nodeDao the NodeDao object to use
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    // Factory seam to allow tests to substitute a mocked VmwareViJavaAccess
    // without pulling in mockito-inline for constructor mocking.
    protected VmwareViJavaAccess createVmwareViJavaAccess(final VmwareServer vmwareServer) {
        return new VmwareViJavaAccess(vmwareServer);
    }
}
