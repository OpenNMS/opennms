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
package org.opennms.netmgt.collection;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.threshd.api.ThresholdInitializationException;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.netmgt.xml.eventconf.CollectionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * This collector will convert event into time series data. It depends on eventconf.xsd's collection tag.
 */
public class EventMetricsCollector implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(EventMetricsCollector.class);

    public static final int NAME_MAX_LENGTH = 19;
    public static final String NODE_SNMP = "nodeSnmp";
    public static final String INTERFACE_SNMP = "interfaceSnmp";

    private final EventSubscriptionService eventSubscriptionService;

    private final EventConfDao eventConfDao;

    private final PersisterFactory persisterFactory;

    private final IpInterfaceDao ipInterfaceDao;

    private final SnmpInterfaceDao snmpInterfaceDao;

    private final CollectionAgentFactory collectionAgentFactory;

    private final ThresholdingService thresholdingService;

    public EventMetricsCollector(
            EventConfDao eventConfDao, EventSubscriptionService eventSubscriptionService,
            PersisterFactory persisterFactory, IpInterfaceDao ipInterfaceDao, SnmpInterfaceDao snmpInterfaceDao,
            CollectionAgentFactory collectionAgentFactory, ThresholdingService thresholdingService) {
        this.eventConfDao = Objects.requireNonNull(eventConfDao);
        this.eventSubscriptionService = Objects.requireNonNull(eventSubscriptionService);
        this.persisterFactory = Objects.requireNonNull(persisterFactory);
        this.ipInterfaceDao = Objects.requireNonNull(ipInterfaceDao);
        this.snmpInterfaceDao = Objects.requireNonNull(snmpInterfaceDao);
        this.collectionAgentFactory = Objects.requireNonNull(collectionAgentFactory);
        this.thresholdingService = Objects.requireNonNull(thresholdingService);
    }

    public void start() {
        eventSubscriptionService.addEventListener(this);
    }

    public void stop() {
        eventSubscriptionService.removeEventListener(this);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onEvent(IEvent e) {
        var eventconf = eventConfDao.findByUei(e.getUei());
        if (eventconf == null || eventconf.getCollectionGroup() == null || eventconf.getCollectionGroup().isEmpty()) {
            return;
        }
        List<CollectionGroup> collectionGroups = eventconf.getCollectionGroup();

        OnmsIpInterface iface = ipInterfaceDao.findByNodeIdAndIpAddress(e.getNodeid().intValue(),
                InetAddressUtils.str(e.getInterfaceAddress()));
        CollectionAgent agent = collectionAgentFactory.createCollectionAgent(iface);

        for (var collectionGroup : collectionGroups) {
            CollectionSetBuilder collectionSetBuilder = this.createCollectionSetBuilder(collectionGroup, e, iface, agent);
            if (collectionSetBuilder == null) {
                continue;
            }
            if (collectionGroup.getRrd() != null) {
                CollectionSet collectionSet = collectionSetBuilder.build();
                this.handleThresholding(collectionSet, agent);
                collectionSet.visit(this.createRrdPersister(collectionGroup.getRrd()));
            } else {
                LOG.warn("Missing rrd config. {}", collectionGroup);
            }
        }
    }

    private void handleThresholding(CollectionSet collectionSet, CollectionAgent agent) {
        try {
            int nodeId = agent.getNodeId();
            String hostAddress = agent.getHostAddress();
            ThresholdingSession session = thresholdingService.createSession(nodeId, hostAddress,
                    EventMetricsCollector.class.getSimpleName(), new ServiceParameters(Collections.emptyMap()));
            session.accept(collectionSet);
        } catch (ThresholdInitializationException | NullPointerException e) {
            LOG.warn("FAIL to initialize Thresholding. Error: {}", e.getMessage());
        }
    }

    private CollectionSetBuilder createCollectionSetBuilder(CollectionGroup collectionGroup, IEvent e, OnmsIpInterface iface, CollectionAgent agent) {
        Objects.requireNonNull(collectionGroup);
        Objects.requireNonNull(e);
        Objects.requireNonNull(iface);
        Objects.requireNonNull(agent);

        var nodeLevelResource = new NodeLevelResource(iface.getNodeId());
        var collectionSetBuilder = new CollectionSetBuilder(agent).withTimestamp(new Date());
        final var groupName = collectionGroup.getName();

        final Resource resource;
        if (NODE_SNMP.equals(collectionGroup.getResourceType())) {
            resource = nodeLevelResource;
        } else if (INTERFACE_SNMP.equals(collectionGroup.getResourceType())) {
            String instanceName = getInstanceName(collectionGroup, e);
            resource = new InterfaceLevelResource(nodeLevelResource, this.getInterfaceName(instanceName, iface));
        } else {
            String instanceName = getInstanceName(collectionGroup, e);
            if (instanceName == null) {
                LOG.warn("Skip parm due to missing instance param {}. uei: {}", collectionGroup.getInstance(), e.getUei());
                return null;
            }
            resource = new DeferredGenericTypeResource(nodeLevelResource, collectionGroup.getResourceType(), instanceName);
            collectionSetBuilder.withStringAttribute(resource, collectionGroup.getName(), "instance", instanceName);
        }
        for (var collection : collectionGroup.getCollection()) {
            IParm parm = e.getParm(collection.getName());
            if (parm == null) {
                continue;
            }
            collectionSetBuilder = setupCollectionSet(collectionSetBuilder, resource, groupName, collection, parm);
        }
        return collectionSetBuilder;
    }

    /**
     * Return instanceName from the event parameter. Collection's instance as parameter key.
     *
     * @param collectionGroup
     * @param e
     * @return
     */
    private String getInstanceName(CollectionGroup collectionGroup, IEvent e) {
        if (collectionGroup.getInstance() == null) {
            return null;
        }
        IParm instanceName = e.getParm(collectionGroup.getInstance());
        if (instanceName == null) {
            return null;
        }
        return instanceName.getValue().getContent();
    }

    /**
     * It will try to find the ifIndex, if not return iface's snmp interface name. It will return in the following order.
     * 1. snmp interface name
     * 2. ifIndex
     * 3. interface ip
     *
     * @param ifIndex
     * @param iface
     * @return interfaceName
     */
    private String getInterfaceName(String ifIndex, OnmsIpInterface iface) {
        OnmsSnmpInterface snmpInterface;
        try {
            snmpInterface = snmpInterfaceDao.findByNodeIdAndIfIndex(iface.getNodeId(), Integer.parseInt(ifIndex));
        } catch (NumberFormatException e) {
            snmpInterface = null;
        }
        if (snmpInterface != null) {
            return snmpInterface.getIfName();
        } else {
            return ifIndex != null ? ifIndex : InetAddressUtils.str(iface.getIpAddress());
        }
    }

    private CollectionSetBuilder setupCollectionSet(CollectionSetBuilder collectionSet, Resource resource,
                                                    String groupName, CollectionGroup.Collection collection, IParm parm) {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(parm);
        String value = parm.getValue().getContent();
        try {
            switch (collection.getType()) {
                case GAUGE:
                    return collectionSet.withGauge(resource, groupName,
                            this.getCollectionName(collection), convertParamValue(collection, value));
                case COUNTER:
                    return collectionSet.withCounter(resource, groupName,
                            this.getCollectionName(collection), convertParamValue(collection, value));
                case STRING:
                    return collectionSet.withStringAttribute(resource, groupName,
                            this.getCollectionName(collection), value);
            }
        } catch (NumberFormatException ex) {
            LOG.warn("Skip invalid value exist. value = {}", value);
        }
        return collectionSet;
    }

    private String getCollectionName(CollectionGroup.Collection collection) {
        String name = collection.getRename();
        if (name == null) {
            name = collection.getName();
        }
        String sanitized = GenericTypeResource.sanitizeInstanceStrict(name);
        // trim to 19 due to rrd name length limitation
        return (sanitized.length() > NAME_MAX_LENGTH) ? sanitized.substring(0, NAME_MAX_LENGTH) : sanitized;
    }

    /**
     * It will convert param values base on the eventconf's paramValues mapping. If nothing match, it returns the original value.
     *
     * @param collection
     * @param value
     * @return converted value
     */
    private double convertParamValue(CollectionGroup.Collection collection, String value) throws NumberFormatException {
        Objects.requireNonNull(value);
        Objects.requireNonNull(collection);
        var found = collection.getParamValue().stream()
                .filter(p -> p.getName().equals(value)).findFirst();
        return (found.isPresent()) ? found.get().getValue() : Double.parseDouble(value);
    }

    private CollectionSetVisitor createRrdPersister(CollectionGroup.Rrd rrd) {
        RrdRepository repository = new RrdRepository();
        repository.setRrdBaseDir(rrd.getBaseDir());
        repository.setStep(rrd.getStep());
        repository.setHeartBeat(rrd.getHeartBeat());
        repository.setRraList(rrd.getRras());
        return persisterFactory.createPersister(
                new ServiceParameters(Collections.emptyMap()), repository);
    }
}