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

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.activemq.broker.BrokerService;
import org.opennms.features.activemq.broker.api.ManagedBroker;
import org.opennms.features.activemq.broker.api.ManagedDestination;

public class ManagedBrokerImpl implements ManagedBroker {
    private final BrokerService brokerService;

    public ManagedBrokerImpl(BrokerService brokerService) {
        this.brokerService = Objects.requireNonNull(brokerService);
    }

    @Override
    public int getCurrentConnections() {
        try {
            return brokerService.getCurrentConnections();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getMemoryPercentUsage() {
        try {
            return brokerService.getProducerSystemUsage().getMemoryUsage().getPercentUsage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getMemoryUsage() {
        try {
            return brokerService.getProducerSystemUsage().getMemoryUsage().getUsage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getMemoryLimit() {
        try {
            return brokerService.getProducerSystemUsage().getMemoryUsage().getLimit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ManagedDestination> getDestinations() {
        try {
            return brokerService.getBroker().getDestinationMap().entrySet().stream()
                    .map(e -> new ManagedDestinationImpl(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
