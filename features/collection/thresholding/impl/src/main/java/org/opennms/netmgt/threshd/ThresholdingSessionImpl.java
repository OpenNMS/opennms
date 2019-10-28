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

import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.threshd.api.ThresholdInitializationException;
import org.opennms.netmgt.threshd.api.ThresholdStateMonitor;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.netmgt.threshd.api.ThresholdingSessionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThresholdingSessionImpl implements ThresholdingSession {

    protected static final Logger LOG = LoggerFactory.getLogger(ThresholdingSessionImpl.class);

    protected final ThresholdingServiceImpl service;

    protected final ThresholdingSessionKey sessionKey;

    protected final ResourceStorageDao resourceStorageDao;

    protected final RrdRepository rrdRepository;

    private ServiceParameters serviceParameters;
    
    private final BlobStore blobStore;
    
    private final boolean isDistributed;
    
    private final ThresholdStateMonitor thresholdStateMonitor;

    public ThresholdingSessionImpl(ThresholdingServiceImpl service, ThresholdingSessionKey sessionKey, ResourceStorageDao resourceStorageDao, RrdRepository rrdRepository,
                                   ServiceParameters serviceParams, BlobStore blobStore, boolean isDistributed,
                                   ThresholdStateMonitor thresholdStateMonitor) {
        this.service = service;
        this.sessionKey = sessionKey;
        this.resourceStorageDao = resourceStorageDao;
        this.rrdRepository = rrdRepository;
        this.serviceParameters = serviceParams;
        this.blobStore = blobStore;
        this.isDistributed = isDistributed;
        this.thresholdStateMonitor = thresholdStateMonitor;
    }

    @Override
    public void accept(CollectionSet collectionSet) throws ThresholdInitializationException {
        acceptCollection(collectionSet);
    }

    @Override
    public void close() throws Exception {
        service.close(this);
    }

    @Override
    public ThresholdingSessionKey getKey() {
        return sessionKey;
    }

    @Override
    public BlobStore getBlobStore() {
        return blobStore;
    }

    public ResourceStorageDao getResourceDao() {
        return resourceStorageDao;
    }

    public RrdRepository getRrdRepository() {
        return rrdRepository;
    }

    public ServiceParameters getServiceParameters() {
        return serviceParameters;
    }

    private void acceptCollection(CollectionSet collectionSet) throws ThresholdInitializationException {
        Long sequenceNumber = collectionSet.getSequenceNumber().isPresent() ?
                collectionSet.getSequenceNumber().getAsLong() : null;
        ThresholdingVisitorImpl thresholdingVisitor = service.getThresholdingVistor(this, sequenceNumber);

        if (thresholdingVisitor == null) {
            LOG.error("No thresholdingVisitor for ThresholdingSession {}", sessionKey);
            return;
        }

        if (thresholdingVisitor.isNodeInOutage()) {
            LOG.info("run: the threshold processing will be skipped because the node {} is on a scheduled outage.", thresholdingVisitor.getNodeId());
        } else if (thresholdingVisitor.hasThresholds()) {
            thresholdingVisitor.setCounterReset(collectionSet.ignorePersist()); // Required to reinitialize the counters.

            collectionSet.visit(thresholdingVisitor);
        }
    }

    @Override
    public boolean isDistributed() {
        return isDistributed;
    }

    @Override
    public ThresholdStateMonitor getThresholdStateMonitor() {
        return thresholdStateMonitor;
    }
}
