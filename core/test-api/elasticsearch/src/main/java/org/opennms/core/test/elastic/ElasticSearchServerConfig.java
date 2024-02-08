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
package org.opennms.core.test.elastic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;

public class ElasticSearchServerConfig {

    public static final String ES_HTTP_PORT = "9205";

    private static final String DEFAULT_HOME_DIRECTORY = "target/elasticsearch-home";

    private final Settings.Builder builder = Settings.builder();
    private long startDelay; // in ms
    private boolean manualStartup;
    private boolean keepElasticHomeAfterShutdown;
    private List<Class<? extends Plugin>> plugins = new ArrayList<>();

    public ElasticSearchServerConfig() {
        withDefaults();
    }

    public ElasticSearchServerConfig withDefaults() {
        withNodeName("testNode");
        withTransportType("local");
        withClusterName("testCluster");
        withHomeDirectory(DEFAULT_HOME_DIRECTORY);
        withSetting("http.port", ES_HTTP_PORT);
        withSetting(NetworkModule.HTTP_TYPE_KEY, "netty4");
        withSetting(NetworkModule.TRANSPORT_TYPE_KEY, "netty4");
        // make startup faster
        withSetting("discovery.zen.ping_timeout", "1ms");
        return this;
    }

    public ElasticSearchServerConfig withNodeName(String name) {
        Objects.requireNonNull(name);
        builder.put(Node.NODE_NAME_SETTING.getKey(), name);
        return this;
    }

    public ElasticSearchServerConfig withTransportType(String transportType) {
        Objects.requireNonNull(transportType);
        builder.put(NetworkModule.TRANSPORT_TYPE_KEY, transportType);
        return this;
    }

    public ElasticSearchServerConfig withClusterName(String clusterName) {
        Objects.requireNonNull(clusterName);
        builder.put(ClusterName.CLUSTER_NAME_SETTING.getKey(), clusterName);
        return this;
    }

    public ElasticSearchServerConfig withHomeDirectory(String homeDirectory) {
        Objects.requireNonNull(homeDirectory);
        builder.put(Environment.PATH_HOME_SETTING.getKey(), homeDirectory);
        return this;
    }

    public ElasticSearchServerConfig withSetting(String key, String value) {
        Objects.requireNonNull(key);
        builder.put(key, value);
        return this;
    }

    public ElasticSearchServerConfig withSetting(String key, boolean value) {
        Objects.requireNonNull(key);
        builder.put(key, value);
        return this;
    }

    public ElasticSearchServerConfig withSetting(String key, int value) {
        Objects.requireNonNull(key);
        builder.put(key, value);
        return this;
    }

    public ElasticSearchServerConfig withPlugins(Class<? extends Plugin>... plugins) {
        this.plugins.addAll(Arrays.asList(plugins));
        return this;
    }

    public ElasticSearchServerConfig withKeepElasticHomeAfterShutdown(boolean keepElasticHomeAfterShutdown) {
        this.keepElasticHomeAfterShutdown = keepElasticHomeAfterShutdown;
        return this;
    }

    public ElasticSearchServerConfig enableCors() {
        this.withSetting("http.cors.allow-headers", "X-Requested-With,X-Auth-Token,Content-Type, Content-Length, Authorization")
                .withSetting("http.cors.allow-methods", "OPTIONS, HEAD, GET, POST, PUT, DELETE")
                .withSetting("http.cors.allow-origin", "/.*/")
                .withSetting("http.cors.enabled", "true");
        return this;
    }

    public ElasticSearchServerConfig withStartDelay(long elasticStartDelay) {
        this.startDelay = elasticStartDelay;
        return this;
    }

    public ElasticSearchServerConfig withManualStartup() {
        this.manualStartup = true;
        return this;
    }

    public long getStartDelay() {
        return startDelay;
    }

    public boolean isManualStartup() {
        return manualStartup;
    }

    public boolean isKeepElasticHomeAfterShutdown() {
        return keepElasticHomeAfterShutdown;
    }

    public String getHomeDirectory() {
        return builder.get(Environment.PATH_HOME_SETTING.getKey());
    }

    public List<Class<? extends Plugin>> getPlugins() {
        return plugins;
    }

    public Settings build() {
        return builder.build();
    }

}
