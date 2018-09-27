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

package org.opennms.core.ipc.sink.offheap;

import org.opennms.core.ipc.sink.api.AggregationPolicy;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.api.SinkModule;

public class MockModule implements SinkModule<MockMessage, MockMessage> {

    private static final int QUEUE_SIZE = 100;
    private static final int NUM_THREADS = 16;
    private boolean blocked = false;

    @Override
    public String getId() {
        return "Mock";
    }

    @Override
    public int getNumConsumerThreads() {
        return NUM_THREADS;
    }

    @Override
    public byte[] marshal(MockMessage message) {
        return message.getId().getBytes();
    }

    @Override
    public MockMessage unmarshal(byte[] message) {
        return new MockMessage(new String(message));
    }

    @Override
    public byte[] marshalSingleMessage(MockMessage message) {
        return message.getId().getBytes();
    }

    @Override
    public MockMessage unmarshalSingleMessage(byte[] message) {
        return new MockMessage(new String(message));
    }

    @Override
    public AggregationPolicy<MockMessage, MockMessage, ?> getAggregationPolicy() {
        return null;
    }

    @Override
    public AsyncPolicy getAsyncPolicy() {
        return new AsyncPolicy() {
            @Override
            public int getQueueSize() {
                return QUEUE_SIZE;
            }

            @Override
            public int getNumThreads() {
                return NUM_THREADS;
            }

            @Override
            public boolean isBlockWhenFull() {
                return blocked;
            }
        };
    }

    protected void setBlocked(boolean blocked) {
         this.blocked = blocked;
    }
}
