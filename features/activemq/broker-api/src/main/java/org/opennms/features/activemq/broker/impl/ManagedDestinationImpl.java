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

package org.opennms.features.activemq.broker.impl;

import java.util.Objects;

import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.DestinationFilter;
import org.apache.activemq.broker.region.Queue;
import org.apache.activemq.command.ActiveMQDestination;
import org.opennms.features.activemq.broker.api.ManagedDestination;

public class ManagedDestinationImpl implements ManagedDestination {
    private final ActiveMQDestination def;
    private final Destination dest;

    public ManagedDestinationImpl(ActiveMQDestination def, Destination dest) {
        this.def = Objects.requireNonNull(def);
        this.dest = Objects.requireNonNull(dest);
    }

    @Override
    public String getName() {
        return dest.getName();
    }

    @Override
    public boolean isQueue() {
        return def.isQueue();
    }

    @Override
    public boolean isTopic() {
        return def.isTopic();
    }

    @Override
    public boolean isTemporary() {
        return def.isTemporary();
    }

    @Override
    public long getMessageCount() {
        return dest.getDestinationStatistics().getMessages().getCount();
    }

    @Override
    public long getEnqueueCount() {
        return dest.getDestinationStatistics().getEnqueues().getCount();
    }

    @Override
    public long getDequeueCount() {
        return dest.getDestinationStatistics().getDequeues().getCount();
    }

    @Override
    public boolean isCursorFull() {
        final Queue queue = toQueue(dest);
        if (queue == null) {
            return false;
        }
        if (queue.getMessages() != null){
            return queue.getMessages().isFull();
        }
        return false;
    }

    @Override
    public void purge() throws Exception {
        final Queue queue = toQueue(dest);
        if (queue == null) {
            throw new UnsupportedOperationException("purge() can only be performed on queues.");
        }
        queue.purge();
    }

    private Queue toQueue(Destination dest) {
        if (dest instanceof Queue) {
            return (Queue)dest;
        } else if (dest instanceof DestinationFilter) {
            final DestinationFilter destFilter = (DestinationFilter)dest;
            return toQueue(destFilter.getNext());
        } else {
            return null;
        }
    }
}
