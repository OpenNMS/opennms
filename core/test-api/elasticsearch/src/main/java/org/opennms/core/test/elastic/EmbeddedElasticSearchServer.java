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
            super(InternalSettingsPreparer.prepareEnvironment(settings, null) , plugins);
        }

    }
}