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

package org.opennms.netmgt.flows.config.internal;

import org.opennms.netmgt.flows.config.CacheSettings;
import org.opennms.netmgt.flows.config.FlowConfiguration;
import org.opennms.netmgt.flows.config.IndexSettings;

public class FlowConfigurationImpl implements FlowConfiguration {

    private String globalElasticUsername;
    private String globalElasticPassword;
    private String elasticIndexStrategy;
    private int defaultMaxTotalConnectionPerRoute;
    private int maxTotalConnection;
    private IndexSettings indexSettings;
    private boolean nodeDiscovery;
    private int nodeDiscoveryFrequency;
    private String proxy;
    private long retryCooldown;
    private String elasticUrl;
    private CacheSettings nodeInfoCacheSettings;

    @Override
    public String getGlobalElasticUsername() {
        return globalElasticUsername;
    }

    public void setGlobalElasticUsername(String globalElasticUsername) {
        this.globalElasticUsername = globalElasticUsername;
    }

    @Override
    public String getGlobalElasticPassword() {
        return globalElasticPassword;
    }

    public void setGlobalElasticPassword(String globalElasticPassword) {
        this.globalElasticPassword = globalElasticPassword;
    }

    @Override
    public String getElasticIndexStrategy() {
        return elasticIndexStrategy;
    }

    public void setElasticIndexStrategy(String elasticIndexStrategy) {
        this.elasticIndexStrategy = elasticIndexStrategy;
    }

    @Override
    public int getDefaultMaxTotalConnectionPerRoute() {
        return defaultMaxTotalConnectionPerRoute;
    }

    public void setDefaultMaxTotalConnectionPerRoute(int defaultMaxTotalConnectionPerRoute) {
        this.defaultMaxTotalConnectionPerRoute = defaultMaxTotalConnectionPerRoute;
    }

    @Override
    public int getMaxTotalConnection() {
        return maxTotalConnection;
    }

    public void setMaxTotalConnection(int maxTotalConnection) {
        this.maxTotalConnection = maxTotalConnection;
    }

    @Override
    public IndexSettings getIndexSettings() {
        return indexSettings;
    }

    public void setIndexSettings(IndexSettings indexSettings) {
        this.indexSettings = indexSettings;
    }

    public boolean isNodeDiscovery() {
        return nodeDiscovery;
    }

    public void setNodeDiscovery(boolean nodeDiscovery) {
        this.nodeDiscovery = nodeDiscovery;
    }

    @Override
    public int getNodeDiscoveryFrequency() {
        return nodeDiscoveryFrequency;
    }

    public void setNodeDiscoveryFrequency(int nodeDiscoveryFrequency) {
        this.nodeDiscoveryFrequency = nodeDiscoveryFrequency;
    }

    @Override
    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    @Override
    public long getRetryCooldown() {
        return retryCooldown;
    }

    public void setRetryCooldown(long retryCooldown) {
        this.retryCooldown = retryCooldown;
    }

    @Override
    public String getElasticUrl() {
        return elasticUrl;
    }

    public void setElasticUrl(String elasticUrl) {
        this.elasticUrl = elasticUrl;
    }

    @Override
    public CacheSettings getNodeInfoCacheSettings() {
        return nodeInfoCacheSettings;
    }

    public void setNodeInfoCacheSettings(CacheSettings nodeInfoCacheSettings) {
        this.nodeInfoCacheSettings = nodeInfoCacheSettings;
    }
}
