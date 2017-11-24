/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.minion.core.impl;

import org.opennms.minion.core.api.ControllerConfig;

public class ControllerConfigImpl implements ControllerConfig {
    private String brokerUrl;
    private int brokerMaxConnections;
    private int brokerConcurrentConsumers;
    private int brokerIdleTimeout;

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public void setBrokerMaxConnections(int brokerMaxConnections) {
        this.brokerMaxConnections = brokerMaxConnections;
    }

    public void setBrokerConcurrentConsumers(int brokerConcurrentConsumers) {
        this.brokerConcurrentConsumers = brokerConcurrentConsumers;
    }

    public void setBrokerIdleTimeout(int brokerIdleTimeout) {
        this.brokerIdleTimeout = brokerIdleTimeout;
    }

    @Override
    public String getBrokerUrl() {
        return brokerUrl;
    }

    @Override
    public int getBrokerMaxConnections() {
        return brokerMaxConnections;
    }

    @Override
    public int getBrokerConcurrentConsumers() {
        return brokerConcurrentConsumers;
    }

    @Override
    public int getBrokerIdleTimeout() {
        return brokerIdleTimeout;
    }

    @Override
    public String toString() {
        return "ControllerConfigImpl{" +
                "brokerUrl='" + brokerUrl + '\'' +
                ", brokerMaxConnections=" + brokerMaxConnections +
                ", brokerConcurrentConsumers=" + brokerConcurrentConsumers +
                ", brokerIdleTimeout=" + brokerIdleTimeout +
                '}';
    }
}
