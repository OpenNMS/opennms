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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;

/**
 * Test helper class which starts up an Elasticsearch instance in the current JVM.
 */
public class EmbeddedElasticSearchServer {

    private static final List<Class<? extends Plugin>> ALWAYS_ON_PLUGINS = Collections.singletonList(Netty4Plugin.class);

    // The embedded ES instance
    private Node node;
    private final Settings settings;
    private final Set<Class<? extends Plugin>> plugins;

    public EmbeddedElasticSearchServer() {
        this(new ElasticSearchServerConfig().withDefaults().build(), null);
    }

    public EmbeddedElasticSearchServer(ElasticSearchServerConfig config) {
        this(config.build(), config.getPlugins());
    }

    public EmbeddedElasticSearchServer(Settings settings, List<Class<? extends Plugin>> plugins) {
        final String homeDirectory = settings.get(Environment.PATH_HOME_SETTING.getKey());
        if (homeDirectory == null || homeDirectory.isEmpty()) {
            throw new IllegalArgumentException("Value for " + Environment.PATH_HOME_SETTING.getKey() + " is null or empty.");
        }
        this.settings = settings;
        this.plugins = new LinkedHashSet<>(plugins);
        this.plugins.addAll(ALWAYS_ON_PLUGINS);
    }

    public void start() throws Exception {
        this.node = new PluginNode(settings, plugins);
        this.node.start();
    }

    public void shutdown() throws IOException {
        if (node != null) {
            node.close();
        } else {
            throw new IllegalStateException("The server has not been started. Please invoke start() before invoking shutdown()");
        }
    }

    public Client getClient() {
        if (node != null) {
            return node.client();
        } else {
            throw new IllegalStateException("The server has not been started. Please invoke start() before invoking getClient()");
        }
    }

    private static class PluginNode extends Node {
        public PluginNode(Settings settings, Collection<Class<? extends Plugin>> plugins) {
            super(InternalSettingsPreparer.prepareEnvironment(settings, Collections.emptyMap(),
                    null, null) , plugins, false);
        }
    }
}