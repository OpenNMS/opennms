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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
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
import com.vmware.vim25.mo.ManagedEntity;

/**
 * Collects datastore capacity / utilization from a vCenter once per assignment.
 *
 * Unlike {@link VmwareCollector}, this collector walks every {@code Datastore}
 * managed entity reachable from the vCenter and emits a multi-instance resource
 * per datastore keyed by managed object id. It is intended to run on a single
 * operator-elected anchor node per vCenter — typically an imported HostSystem,
 * but any node with a {@code VMware:managementServer} metadata entry (or with
 * the {@code vmware-management-server} service parameter set) will work.
 *
 * Attribute names recognized in the {@code <vmware-group>} configuration map
 * directly to fields on {@code Datastore.summary}:
 *
 * Numeric: {@code capacity}, {@code freeSpace}, {@code used}, {@code usedPct},
 *          {@code uncommitted}, {@code overcommittedBytes}, {@code accessible},
 *          {@code multipleHostAccess}.
 *
 * String:  {@code name}, {@code type}, {@code url}.
 */
public class VmwareDatastoreCollector extends AbstractRemoteServiceCollector {

    private static final Logger logger = LoggerFactory.getLogger(VmwareDatastoreCollector.class);

    private static final String PARAM_MANAGEMENT_SERVER = "vmware-management-server";

    private static final Map<String, Class<?>> TYPE_MAP = Collections.unmodifiableMap(Stream.of(
            new SimpleEntry<>(VmwareImporter.VMWARE_COLLECTION_KEY, VmwareCollection.class),
            new SimpleEntry<>(VmwareImporter.VMWARE_SERVER_KEY, VmwareServer.class))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    // Package-private to allow unit tests in the same package to verify edge cases
    // (null uncommitted, capacity zero, overcommit floor, etc.) without going
    // through the full collect() path.
    static final Map<String, ToLongFunction<DatastoreSummary>> NUMERIC_EXTRACTORS;
    static final Map<String, Function<DatastoreSummary, String>> STRING_EXTRACTORS;

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
        NUMERIC_EXTRACTORS = Collections.unmodifiableMap(numeric);

        final Map<String, Function<DatastoreSummary, String>> string = new HashMap<>();
        string.put("name", DatastoreSummary::getName);
        string.put("type", DatastoreSummary::getType);
        string.put("url", DatastoreSummary::getUrl);
        STRING_EXTRACTORS = Collections.unmodifiableMap(string);
    }

    private NodeDao m_nodeDao;
    private TransactionTemplate m_transactionTemplate;
    private VmwareDatacollectionConfigDao m_vmwareDatacollectionConfigDao;
    private VmwareConfigDao m_vmwareConfigDao;

    public VmwareDatastoreCollector() {
        super(TYPE_MAP);
    }

    @Override
    public void initialize() throws CollectionInitializationException {
        if (m_nodeDao == null) {
            m_nodeDao = BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class);
        }
        if (m_vmwareDatacollectionConfigDao == null) {
            m_vmwareDatacollectionConfigDao = BeanUtils.getBean("daoContext", "vmwareDatacollectionConfigDao", VmwareDatacollectionConfigDao.class);
        }
        if (m_vmwareConfigDao == null) {
            m_vmwareConfigDao = BeanUtils.getBean("daoContext", "vmwareConfigDao", VmwareConfigDao.class);
        }
        if (m_transactionTemplate == null) {
            m_transactionTemplate = BeanUtils.getBean("daoContext", "transactionTemplate", TransactionTemplate.class);
        }
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(final CollectionAgent agent, final Map<String, Object> parameters) {
        final Map<String, Object> runtimeAttributes = new HashMap<>();

        m_transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(final TransactionStatus transactionStatus) {
                final OnmsNode onmsNode = m_nodeDao.get(agent.getNodeId());
                if (onmsNode == null) {
                    throw new IllegalArgumentException(String.format("VmwareDatastoreCollector: No node found with id: %d", agent.getNodeId()));
                }

                String vmwareManagementServer = ParameterMap.getKeyedString(parameters, PARAM_MANAGEMENT_SERVER, null);
                if (Strings.isNullOrEmpty(vmwareManagementServer)) {
                    vmwareManagementServer = VmwareImporter.getManagementServer(onmsNode);
                }
                if (Strings.isNullOrEmpty(vmwareManagementServer)) {
                    throw new IllegalArgumentException(String.format(
                            "VmwareDatastoreCollector: No management server set on node %d (no '%s' parameter and no VMware:managementServer metadata).",
                            onmsNode.getId(), PARAM_MANAGEMENT_SERVER));
                }
                runtimeAttributes.put(VmwareImporter.METADATA_MANAGEMENT_SERVER, vmwareManagementServer);

                final String collectionName = ParameterMap.getKeyedString(parameters, "collection",
                        ParameterMap.getKeyedString(parameters, "vmware-collection", null));
                final VmwareCollection collection = m_vmwareDatacollectionConfigDao.getVmwareCollection(collectionName);
                if (collection == null) {
                    throw new IllegalArgumentException(String.format(
                            "VmwareDatastoreCollector: No collection found with name '%s'.", collectionName));
                }
                runtimeAttributes.put(VmwareImporter.VMWARE_COLLECTION_KEY, collection);

                final Map<String, VmwareServer> serverMap = m_vmwareConfigDao.getServerMap();
                if (serverMap == null) {
                    throw new IllegalStateException("VmwareDatastoreCollector: Error getting vmware-config.xml's server map.");
                }
                final VmwareServer vmwareServer = serverMap.get(vmwareManagementServer);
                if (vmwareServer == null) {
                    throw new IllegalStateException(String.format(
                            "VmwareDatastoreCollector: No credentials for VMware management server '%s' in vmware-config.xml.",
                            vmwareManagementServer));
                }
                runtimeAttributes.put(VmwareImporter.VMWARE_SERVER_KEY, Interpolator.pleaseInterpolate(vmwareServer));

                return null;
            }
        });

        return runtimeAttributes;
    }

    @Override
    public CollectionSet collect(final CollectionAgent agent, final Map<String, Object> parameters) throws CollectionException {
        final VmwareCollection collection = (VmwareCollection) parameters.get(VmwareImporter.VMWARE_COLLECTION_KEY);
        final String vmwareManagementServer = (String) parameters.get(VmwareImporter.METADATA_MANAGEMENT_SERVER);
        final VmwareServer vmwareServer = (VmwareServer) parameters.get(VmwareImporter.VMWARE_SERVER_KEY);
        final CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        builder.withStatus(CollectionStatus.FAILED);

        final List<VmwareGroup> validGroups = new ArrayList<>();
        for (final VmwareGroup vmwareGroup : collection.getVmwareGroup()) {
            if ("node".equalsIgnoreCase(vmwareGroup.getResourceType())) {
                logger.warn("VmwareDatastoreCollector: group '{}' uses resourceType=node which is not supported; skipping.",
                        vmwareGroup.getName());
            } else {
                validGroups.add(vmwareGroup);
            }
        }
        if (validGroups.isEmpty()) {
            logger.info("VmwareDatastoreCollector: no usable groups defined; returning empty collection set.");
            builder.withStatus(CollectionStatus.SUCCEEDED);
            return builder.build();
        }

        try (final VmwareViJavaAccess vmwareViJavaAccess = createVmwareViJavaAccess(vmwareServer)) {
            vmwareViJavaAccess.connect(ParameterMap.getKeyedInteger(parameters, "timeout", VmwareViJavaAccess.DEFAULT_TIMEOUT));

            final ManagedEntity[] datastores;
            try {
                datastores = vmwareViJavaAccess.searchManagedEntities("Datastore");
            } catch (final RemoteException e) {
                logger.warn("VmwareDatastoreCollector: error enumerating datastores from '{}': {}",
                        vmwareManagementServer, e.getMessage());
                return builder.build();
            }

            if (datastores == null || datastores.length == 0) {
                logger.info("VmwareDatastoreCollector: no datastores returned from '{}'.", vmwareManagementServer);
                builder.withStatus(CollectionStatus.SUCCEEDED);
                return builder.build();
            }

            final NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());

            for (final ManagedEntity me : datastores) {
                final Datastore ds = (Datastore) me;
                final String moid = ds.getMOR().getVal();

                final DatastoreSummary summary;
                try {
                    summary = ds.getSummary();
                } catch (final Exception e) {
                    logger.warn("VmwareDatastoreCollector: error reading summary for datastore '{}' ({}): {}",
                            ds.getName(), moid, e.getMessage());
                    continue;
                }
                if (summary == null) {
                    logger.debug("VmwareDatastoreCollector: null summary for datastore '{}' ({}); skipping.",
                            ds.getName(), moid);
                    continue;
                }

                final String label = summary.getName() == null ? moid : summary.getName();
                boolean labelEmitted = false;

                for (final VmwareGroup vmwareGroup : validGroups) {
                    final Resource resource = new DeferredGenericTypeResource(nodeResource,
                            vmwareGroup.getResourceType(), moid);

                    if (!labelEmitted) {
                        // Resource label property — UI uses this via resourceLabel="${<resourceType>Name}".
                        builder.withStringAttribute(resource, vmwareGroup.getName(),
                                vmwareGroup.getResourceType() + "Name", label);
                        labelEmitted = true;
                    }

                    for (final Attrib attrib : vmwareGroup.getAttrib()) {
                        final String name = attrib.getName();
                        if (NUMERIC_EXTRACTORS.containsKey(name)) {
                            try {
                                final long value = NUMERIC_EXTRACTORS.get(name).applyAsLong(summary);
                                final AttributeType type = attrib.getType();
                                if (type.isNumeric()) {
                                    builder.withNumericAttribute(resource, vmwareGroup.getName(),
                                            attrib.getAlias(), value, type);
                                } else {
                                    logger.warn("VmwareDatastoreCollector: numeric attribute '{}' (alias '{}') in group '{}' is configured with non-numeric type '{}'; skipping. Configure type as Gauge or Counter.",
                                            name, attrib.getAlias(), vmwareGroup.getName(), type);
                                }
                            } catch (final Exception e) {
                                logger.debug("VmwareDatastoreCollector: failed to extract '{}' from datastore '{}' ({}): {}",
                                        name, ds.getName(), moid, e.getMessage());
                            }
                        } else if (STRING_EXTRACTORS.containsKey(name)) {
                            try {
                                final String value = STRING_EXTRACTORS.get(name).apply(summary);
                                builder.withStringAttribute(resource, vmwareGroup.getName(),
                                        attrib.getAlias(), value == null ? "" : value);
                            } catch (final Exception e) {
                                logger.debug("VmwareDatastoreCollector: failed to extract '{}' from datastore '{}' ({}): {}",
                                        name, ds.getName(), moid, e.getMessage());
                            }
                        } else {
                            logger.warn("VmwareDatastoreCollector: unknown attribute '{}' configured in group '{}'; ignoring.",
                                    name, vmwareGroup.getName());
                        }
                    }
                }
            }
            builder.withStatus(CollectionStatus.SUCCEEDED);
        } catch (final MalformedURLException | RemoteException e) {
            logger.warn("VmwareDatastoreCollector: error connecting to '{}': {} ({})",
                    vmwareManagementServer, e.getMessage(), e.getClass().getName());
            return builder.build();
        }

        return builder.build();
    }

    @Override
    public RrdRepository getRrdRepository(final String collectionName) {
        return m_vmwareDatacollectionConfigDao.getRrdRepository(collectionName);
    }

    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    // Factory seam to allow tests to substitute a mocked VmwareViJavaAccess
    // without pulling in mockito-inline for constructor mocking.
    protected VmwareViJavaAccess createVmwareViJavaAccess(final VmwareServer vmwareServer) {
        return new VmwareViJavaAccess(vmwareServer);
    }
}
