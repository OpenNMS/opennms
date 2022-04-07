/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.core.DefaultCollectionAgent;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.xml.eventconf.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * This collector will convert event into time series data. It depends on eventconf.xsd's collection tag.
 */
public class EventListenerCollector implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(EventListenerCollector.class);

    public static final String NODE_SNMP = "nodeSnmp";
    public static final String INTERFACE_SNMP = "interfaceSnmp";

    private final EventSubscriptionService eventSubscriptionService;

    private final EventConfDao eventConfDao;

    private final PersisterFactory persisterFactory;

    private final IpInterfaceDao ipInterfaceDao;

    private final CollectionAgentFactory collectionAgentFactory;

    public EventListenerCollector(
            EventConfDao eventConfDao, EventSubscriptionService eventSubscriptionService,
            PersisterFactory persisterFactory, IpInterfaceDao ipInterfaceDao,
            CollectionAgentFactory collectionAgentFactory) {
        this.eventConfDao = eventConfDao;
        this.eventSubscriptionService = eventSubscriptionService;
        this.persisterFactory = persisterFactory;
        this.ipInterfaceDao = ipInterfaceDao;
        this.collectionAgentFactory = collectionAgentFactory;
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
        if (eventconf == null || eventconf.getCollections().isEmpty()) {
            return;
        }
        List<Collection> collections = eventconf.getCollections();

        OnmsIpInterface iface = ipInterfaceDao.findByNodeIdAndIpAddress(e.getNodeid().intValue(),
                e.getInterfaceAddress().getHostAddress());
        CollectionAgent agent = collectionAgentFactory.createCollectionAgent(iface);

        for (IParm parm : e.getParmCollection()) {
            Collection collection = this.getMatchCollection(e, collections, parm.getParmName());
            if (collection == null) {
                LOG.debug("Drop parm name: {} \t value: {}", parm.getParmName(), parm.getValue());
                continue;
            }

            var nodeLevelResource = new NodeLevelResource(iface.getNodeId());
            var collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date());

            final Resource resource;
            final String groupName;
            if (NODE_SNMP.equals(collection.getTarget())) {
                groupName = NODE_SNMP;
                resource = nodeLevelResource;
            } else if (INTERFACE_SNMP.equals(collection.getTarget())) {
                String instanceName = getInstanceName(collection, e);
                groupName = INTERFACE_SNMP;
                resource = new InterfaceLevelResource(nodeLevelResource, this.getInterfaceName(instanceName, iface));
            } else {
                String instanceName = getInstanceName(collection, e);
                if (instanceName == null) {
                    LOG.warn("Skip parm due to missing instance param {}. uei: {}", collection.getInstance(), e.getUei());
                    continue;
                }
                groupName = instanceName;
                resource = new DeferredGenericTypeResource(nodeLevelResource, collection.getTarget(), groupName);
                collectionSet.withStringAttribute(resource, collection.getTarget(), "instance", groupName);
            }
            collectionSet = setupCollectionSet(collectionSet, resource, groupName, collection, parm);
            collectionSet.build().visit(this.getRrdPersister(collection));
        }
    }

    /**
     * Return instanceName from the event parameter. Collection's instance as parameter key.
     *
     * @param collection
     * @param e
     * @return
     */
    private String getInstanceName(Collection collection, IEvent e) {
        if (collection.getInstance() == null) {
            return null;
        }
        IParm instanceName = e.getParm(collection.getInstance());
        if (instanceName == null) {
            return null;
        }
        return instanceName.getValue().getContent();
    }

    /**
     * It will try to find the ifIndex, if not return iface's snmp interface name. If iface snmp interface not found, it will return interface ip address
     *
     * @param ifIndex
     * @param iface
     * @return interfaceName
     */
    private String getInterfaceName(String ifIndex, OnmsIpInterface iface) {
        OnmsIpInterface targetInterface;
        try {
            int tmpIndex = Integer.parseInt(ifIndex);
            targetInterface = ipInterfaceDao.findByNodeId(iface.getNodeId()).stream()
                    .filter(tmpIf -> tmpIf.getIfIndex() == tmpIndex).findFirst().orElse(iface);
        } catch (NullPointerException | NumberFormatException exception) {
            targetInterface = iface;
        }
        return targetInterface.getSnmpInterface() != null
                ? targetInterface.getSnmpInterface().getIfName() : iface.getIpAddress().getHostAddress();
    }

    private CollectionSetBuilder setupCollectionSet(CollectionSetBuilder collectionSet, Resource resource,
                                                    String groupName, Collection collection, IParm parm) {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(parm);
        String value = convertParamValue(collection, parm);
        switch (collection.getType()) {
            case GAUGE:
                return collectionSet.withGauge(resource, groupName,
                        this.getEscapeParamName(collection.getName()), Float.parseFloat(value));
            case COUNTER:
                return collectionSet.withCounter(resource, groupName,
                        this.getEscapeParamName(collection.getName()), Float.parseFloat(value));
            case STRING:
                return collectionSet.withStringAttribute(resource, groupName,
                        this.getEscapeParamName(collection.getName()), value);
        }
        return collectionSet;
    }

    private String getEscapeParamName(String name) {
        return name.replaceAll(Matcher.quoteReplacement(File.separator), "_");
    }

    /**
     * It will convert param values base on the eventconf's paramValues mapping. If nothing match, it returns the original value.
     *
     * @param collection
     * @param parm
     * @return converted value
     */
    private String convertParamValue(Collection collection, IParm parm) {
        Objects.requireNonNull(parm);
        Objects.requireNonNull(collection);
        String value = parm.getValue().getContent();
        String searchText = value + ":";
        List<String> paramValues = collection.getParamValues();
        for (var s : paramValues) {
            var idx = s.indexOf(searchText);
            if (idx != -1) {
                return s.substring(idx + searchText.length());
            }
        }
        return value;
    }

    private CollectionSetVisitor getRrdPersister(Collection collection) {
        RrdRepository repository = new RrdRepository();
        repository.setRrdBaseDir(new File(ResourceTypeUtils.DEFAULT_RRD_ROOT, ResourceTypeUtils.SNMP_DIRECTORY));
        repository.setStep(collection.getStep());
        repository.setHeartBeat(collection.getHeartBeat());
        repository.setRraList(collection.getRras());
        return persisterFactory.createPersister(
                new ServiceParameters(Collections.emptyMap()), repository, false, false, false);
    }

    private Collection getMatchCollection(IEvent e, List<Collection> collections, String name) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(collections);
        // if user set the collection name as uei, it means user want the whole message as value
        if (collections.size() == 1 && e.getUei().equals(collections.get(0).getName())) {
            return collections.get(0);
        }
        for (var collection : collections) {
            if (name.equals(collection.getName()))
                return collection;
        }
        return null;
    }
}