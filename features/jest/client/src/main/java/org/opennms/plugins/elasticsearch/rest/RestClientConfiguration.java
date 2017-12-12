/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.plugins.elasticsearch.rest;

public class RestClientConfiguration {

    private String globalElasticUsername;
    private String globalElasticPassword;
    private String elasticUrl;
    private Integer defaultMaxTotalConnectionsPerRoute;
    private Integer maxTotalConnection;
    private Boolean nodeDiscovery;
    private Integer nodeDiscoveryFrequency;
    private String proxy;
    private Integer connectionTimeout;
    private Integer maxConnectionIdleTimeout;
    private Boolean multithreaded;
    private Integer readTimeout;
    private Integer retries;
    private boolean multiThreaded;

    public String getGlobalElasticUsername() {
        return globalElasticUsername;
    }

    public void setGlobalElasticUsername(String globalElasticUsername) {
        this.globalElasticUsername = globalElasticUsername;
    }

    public String getGlobalElasticPassword() {
        return globalElasticPassword;
    }

    public void setGlobalElasticPassword(String globalElasticPassword) {
        this.globalElasticPassword = globalElasticPassword;
    }

    public String getElasticUrl() {
        return elasticUrl;
    }

    public void setElasticUrl(String elasticUrl) {
        this.elasticUrl = elasticUrl;
    }

    public Integer getDefaultMaxTotalConnectionsPerRoute() {
        return defaultMaxTotalConnectionsPerRoute;
    }

    public void setDefaultMaxTotalConnectionsPerRoute(int defaultMaxTotalConnectionsPerRoute) {
        this.defaultMaxTotalConnectionsPerRoute = defaultMaxTotalConnectionsPerRoute;
    }

    public Integer getMaxTotalConnection() {
        return maxTotalConnection;
    }

    public void setMaxTotalConnection(int maxTotalConnection) {
        this.maxTotalConnection = maxTotalConnection;
    }

    public Boolean isNodeDiscovery() {
        return nodeDiscovery;
    }

    public Boolean getNodeDiscovery() {
        return this.nodeDiscovery;
    }

    public void setNodeDiscovery(boolean nodeDiscovery) {
        this.nodeDiscovery = nodeDiscovery;
    }

    public Integer getNodeDiscoveryFrequency() {
        return nodeDiscoveryFrequency;
    }

    public void setNodeDiscoveryFrequency(int nodeDiscoveryFrequency) {
        this.nodeDiscoveryFrequency = nodeDiscoveryFrequency;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public Integer getMaxConnectionIdleTimeout() {
        return maxConnectionIdleTimeout;
    }

    public Boolean isMultiThreaded() {
        return multithreaded;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setMaxConnectionIdleTimeout(int maxConnectionIdleTimeout) {
        this.maxConnectionIdleTimeout = maxConnectionIdleTimeout;
    }

    public Boolean getMultiThreaded() {
        return multithreaded;
    }

    public void setMultithreaded(boolean multithreaded) {
        this.multithreaded = multithreaded;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setMultiThreaded(boolean multiThreaded) {
        this.multiThreaded = multiThreaded;
    }
}
