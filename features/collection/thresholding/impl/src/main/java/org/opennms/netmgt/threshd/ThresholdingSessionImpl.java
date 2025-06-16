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
package org.opennms.netmgt.threshd;

import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
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

    private ServiceParameters serviceParameters;
    
    private final BlobStore blobStore;
    
    private final boolean isDistributed;
    
    private final ThresholdStateMonitor thresholdStateMonitor;

    public ThresholdingSessionImpl(ThresholdingServiceImpl service, ThresholdingSessionKey sessionKey,
                                   ServiceParameters serviceParams, BlobStore blobStore, boolean isDistributed,
                                   ThresholdStateMonitor thresholdStateMonitor) {
        this.service = service;
        this.sessionKey = sessionKey;
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
