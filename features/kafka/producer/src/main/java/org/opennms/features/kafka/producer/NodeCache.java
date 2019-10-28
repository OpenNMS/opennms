/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
