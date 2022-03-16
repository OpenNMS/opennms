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

import org.opennms.netmgt.collection.api.*;
import org.opennms.netmgt.collection.core.DefaultCollectionAgent;
import org.opennms.netmgt.collection.support.AbstractCollectionAttribute;
import org.opennms.netmgt.collection.support.AbstractCollectionAttributeType;
import org.opennms.netmgt.collection.support.AbstractCollectionResource;
import org.opennms.netmgt.collection.support.SingleResourceCollectionSet;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
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
import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class EventListenerCollector implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(EventListenerCollector.class);

    private static final String COLLECTION_GROUP_NAME = "Event";

    @Autowired
    private EventConfDao eventConfDao;

    @Autowired
    private EventSubscriptionService eventSubscriptionService;

    @Autowired
    private PersisterFactory persisterFactory;

    @Autowired
    private IpInterfaceDao ifaceDao;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @PostConstruct
    public void init(){
        eventSubscriptionService.addEventListener(this);
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
        List<IParm> parms = e.getParmCollection();

        OnmsIpInterface iface = ifaceDao.findByNodeIdAndIpAddress(e.getNodeid().intValue(),
                e.getInterfaceAddress().getHostAddress());
        final NodeLevelResource nodeResource = new NodeLevelResource(iface.getNodeId());
        CollectionAgent agent = DefaultCollectionAgent.create(iface.getId(), ifaceDao, platformTransactionManager);
        final DeferredGenericTypeResource appResource = new DeferredGenericTypeResource(nodeResource,
                COLLECTION_GROUP_NAME, e.getUei());

        for (IParm parm : parms) {
            Collection collection = getMatchCollection(collections, parm.getParmName());
            if (collection == null) {
                LOG.debug("Drop parm name: {} \t value: {}", parm.getParmName(), parm.getValue());
                continue;
            }
            RrdRepository rrdRepository = this.getRrdRepository(collection);

            CollectionSetVisitor persister = persisterFactory.createPersister(
                    new ServiceParameters(Collections.emptyMap()), rrdRepository, false, false, false);
            AttributeType attType = AttributeType.parse(collection.getType());
            var collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date());

            switch (attType) {
                case GAUGE:
                    collectionSet = collectionSet.withGauge(appResource, COLLECTION_GROUP_NAME, parm.getParmName(),
                            Float.parseFloat(parm.getValue().getContent()));
                    break;
                case COUNTER:
                    collectionSet = collectionSet.withCounter(appResource, COLLECTION_GROUP_NAME, parm.getParmName(),
                            Float.parseFloat(parm.getValue().getContent()));
                    break;
                case STRING:
                    collectionSet = collectionSet.withStringAttribute(appResource, COLLECTION_GROUP_NAME, parm.getParmName(),
                            parm.getValue().getContent());
                    break;
            }
            collectionSet.build().visit(persister);
        }
    }

    public RrdRepository getRrdRepository(Collection collection) {
        RrdRepository repository = new RrdRepository();
        repository.setRrdBaseDir(new File(ResourceTypeUtils.DEFAULT_RRD_ROOT, ResourceTypeUtils.SNMP_DIRECTORY));
        repository.setStep(collection.getStep());
        repository.setHeartBeat(repository.getStep() * 2);
        repository.setRraList(collection.getRras());
        return repository;
    }

    private Collection getMatchCollection(List<Collection> collections, String name) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(collections);
        for (var collection : collections) {
            if (name.equals(collection.getName()))
                return collection;
        }
        return null;
    }
}
