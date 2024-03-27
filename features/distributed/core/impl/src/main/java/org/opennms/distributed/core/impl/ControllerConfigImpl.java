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
package org.opennms.distributed.core.impl;

import org.opennms.distributed.core.api.ControllerConfig;

public class ControllerConfigImpl implements ControllerConfig {
    private String brokerUrl;
    private int brokerMaxConnections;
    private int brokerConcurrentConsumers;
    private int brokerIdleTimeout;
    private String httpUrl;

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

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
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
    public String getHttpUrl() {
        return httpUrl;
    }

    @Override
    public String toString() {
        return "ControllerConfigImpl{" +
                "brokerUrl='" + brokerUrl + '\'' +
                ", brokerMaxConnections=" + brokerMaxConnections +
                ", brokerConcurrentConsumers=" + brokerConcurrentConsumers +
                ", brokerIdleTimeout=" + brokerIdleTimeout +
                ", httpUrl=" + httpUrl +
                '}';
    }
}
