/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.soa.lookup.ServiceRegistryLookup;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.ThresholdingConfigFactory;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.threshd.api.ThresholdInitializationException;
import org.opennms.netmgt.threshd.api.ThresholdingEventProxy;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.netmgt.threshd.api.ThresholdingSessionKey;
import org.opennms.netmgt.threshd.api.ThresholdingSetPersister;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

/**
 * Thresholding Service.
 */
public class ThresholdingServiceImpl implements ThresholdingService, EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ThresholdingServiceImpl.class);

    public static final List<String> UEI_LIST =
            Lists.newArrayList(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI,
                               EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI,
                               EventConstants.RELOAD_DAEMON_CONFIG_UEI,
                               EventConstants.THRESHOLDCONFIG_CHANGED_EVENT_UEI);

    @Autowired
    private ThresholdingEventProxy eventProxy;

    @Autowired
    private ThresholdingSetPersister thresholdingSetPersister;

    @Autowired
    private ResourceStorageDao resourceStorageDao;

    @Autowired
    private EventIpcManager eventIpcManager;
    
    private final AtomicReference<BlobStore> kvStore = new AtomicReference<>();

    private static final ServiceLookup<Class<?>, String> SERVICE_LOOKUP = new ServiceLookupBuilder(new ServiceRegistryLookup(DefaultServiceRegistry.INSTANCE))
            .blocking()
            .build();

    @PostConstruct
    private void init() {
        try {
            ThreshdConfigFactory.init();
            ThresholdingConfigFactory.init();
            eventIpcManager.addEventListener(this, UEI_LIST);
        } catch (final Exception e) {
            throw new RuntimeException("Unable to initialize thresholding.", e);
        }
    }

    @Override
    public String getName() {
        return "ThresholdingService";
    }

    @Override
    public void onEvent(Event e) {
        switch (e.getUei()) {
        case EventConstants.NODE_GAINED_SERVICE_EVENT_UEI:
            nodeGainedService(e);
            break;
        case EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI:
            handleNodeCategoryChanged(e);
            break;
        case EventConstants.RELOAD_DAEMON_CONFIG_UEI:
            daemonReload(e);
            break;
        case EventConstants.THRESHOLDCONFIG_CHANGED_EVENT_UEI:
            reinitializeThresholdingSets(e);
            break;
        default:
            LOG.debug("Unexpected Event for Thresholding: {}", e);
            break;
        }
    }

    public void nodeGainedService(Event event) {
        LOG.debug(event.toString());
        // Trigger re-evaluation of Threshold Packages, re-evaluating Filters.
        ThreshdConfigFactory.getInstance().rebuildPackageIpListMap();
        reinitializeThresholdingSets(event);
    }

    public void handleNodeCategoryChanged(Event event) {
        LOG.debug(event.toString());
        // Trigger re-evaluation of Threshold Packages, re-evaluating Filters.
        ThreshdConfigFactory.getInstance().rebuildPackageIpListMap();
        reinitializeThresholdingSets(event);
    }

    @Override
    public ThresholdingSession createSession(int nodeId, String hostAddress, String serviceName, RrdRepository repository, ServiceParameters serviceParams)
            throws ThresholdInitializationException {
        Objects.requireNonNull(repository, "RrdRepository must not be null");
        Objects.requireNonNull(serviceParams, "ServiceParameters must not be null");

        synchronized (kvStore) {
            if (kvStore.get() == null) {
                waitForKvStore();
            }
        }
        
        String resource = "";
        if (repository.getRrdBaseDir() != null && repository.getRrdBaseDir().getPath() != null) {
            resource = repository.getRrdBaseDir().getPath();
        }
        ThresholdingSessionKey sessionKey = new ThresholdingSessionKeyImpl(nodeId, hostAddress, serviceName, resource);
        return new ThresholdingSessionImpl(this, sessionKey, resourceStorageDao, repository, serviceParams, kvStore.get());
    }

    public ThresholdingVisitorImpl getThresholdingVistor(ThresholdingSession session) throws ThresholdInitializationException {
        ThresholdingSetImpl thresholdingSet = (ThresholdingSetImpl) thresholdingSetPersister.getThresholdingSet(session, eventProxy);
        return new ThresholdingVisitorImpl(thresholdingSet, ((ThresholdingSessionImpl) session).getResourceDao(), eventProxy);
    }

    public EventIpcManager getEventIpcManager() {
        return eventIpcManager;
    }

    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        this.eventIpcManager = eventIpcManager;
    }

    public ThresholdingEventProxy getEventProxy() {
        return eventProxy;
    }

    public void setEventProxy(ThresholdingEventProxy eventProxy) {
        this.eventProxy = eventProxy;
    }

    public ThresholdingSetPersister getThresholdingSetPersister() {
        return thresholdingSetPersister;
    }

    public void setThresholdingSetPersister(ThresholdingSetPersister thresholdingSetPersister) {
        this.thresholdingSetPersister = thresholdingSetPersister;
    }

    public void close(ThresholdingSessionImpl session) {
        thresholdingSetPersister.clear(session);
    }

    private void daemonReload(Event event) {
        final String thresholdsDaemonName = "Threshd";
        boolean isThresholds = false;
        for (Parm parm : event.getParmCollection()) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && thresholdsDaemonName.equalsIgnoreCase(parm.getValue().getContent())) {
                isThresholds = true;
                break;
            }
        }
        if (isThresholds) {
            try {
                ThreshdConfigFactory.reload();
                ThresholdingConfigFactory.reload();
                thresholdingSetPersister.reinitializeThresholdingSets();
            } catch (final Exception e) {
                throw new RuntimeException("Unable to reload thresholding.", e);
            }
        }
    }

    private void reinitializeThresholdingSets(Event e) {
        thresholdingSetPersister.reinitializeThresholdingSets();
    }

    private void waitForKvStore() {
        BlobStore osgiKvStore = SERVICE_LOOKUP.lookup(BlobStore.class, null);

        if (osgiKvStore == null) {
            throw new RuntimeException("Timed out waiting for a key value store");
        } else {
            kvStore.set(osgiKvStore);
        }
    }
}
