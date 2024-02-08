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
