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
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.collect.Maps;

public class NodeCache {

    private final NodeDao nodeDao;

    private final TransactionOperations transactionOperations;

    private final Map<Long, Long> lastUpdatedByNodeId = Maps.newHashMap();

    private long timeoutInMs = TimeUnit.MINUTES.toMillis(5);

    public NodeCache(NodeDao nodeDao, TransactionOperations transactionOperations) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.transactionOperations = Objects.requireNonNull(transactionOperations);
    }

    public synchronized void triggerIfNeeded(long nodeId, Consumer<OnmsNode> consumer) {
        final long now = System.currentTimeMillis();
        final Long lastUpdated = lastUpdatedByNodeId.get(nodeId);
        if (lastUpdated != null && now - lastUpdated <= timeoutInMs) {
            // No update required
            return;
        }

        transactionOperations.execute((TransactionCallback<Void>) status -> {
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
