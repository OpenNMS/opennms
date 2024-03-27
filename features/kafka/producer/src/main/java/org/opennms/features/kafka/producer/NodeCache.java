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
package org.opennms.features.kafka.producer;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;

import com.google.common.collect.Maps;

public class NodeCache {

    private final NodeDao nodeDao;

    private final SessionUtils sessionUtils;

    private final Map<Long, Long> lastUpdatedByNodeId = Maps.newConcurrentMap();

    private long timeoutInMs = TimeUnit.MINUTES.toMillis(5);

    public NodeCache(NodeDao nodeDao, SessionUtils sessionUtils) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    /**
     * Calls the given consumer with a OnmsNode object corresponding to the given nodeId
     * if no call has been made for this node within the configured timeout.
     *
     * The given node object may be null if no node exists with the given node id.
     * The callback will be done within the context of a read-only transaction.
     * If multiple threads use this function with the same node id, it is possible
     * that multiple callbacks occur before the timeout.
     *
     * @param nodeId db id of the node to query
     * @param consumer callback to issue with the node, if the timeout has not expired since the last callback
     */
    public void triggerIfNeeded(long nodeId, Consumer<OnmsNode> consumer) {
        final long now = System.currentTimeMillis();
        final Long lastUpdated = lastUpdatedByNodeId.get(nodeId);
        if (lastUpdated != null && now - lastUpdated <= timeoutInMs) {
            // No update required
            return;
        }

        sessionUtils.withReadOnlyTransaction(() -> {
            // Lookup the node
            final OnmsNode node = nodeDao.get((int)nodeId);

            // Use the timestamp we gather at the beginning of the function, instead
            // of making another call to System.currentTimeMillis(), even though
            // some time may have passed since
            lastUpdatedByNodeId.put(nodeId, now);

            // We got a node, trigger the consumer while holding the transaction
            // in order to allow relationships to be loaded
            consumer.accept(node);
            return null;
        });
    }

    public void setTimeoutInMs(long timeoutInMs) {
        this.timeoutInMs = timeoutInMs;
    }
}
