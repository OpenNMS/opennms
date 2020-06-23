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
