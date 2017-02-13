/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.test.elasticsearch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.rules.ExternalResource;

/**
 * This class starts up an embedded Elasticsearch node for use in integration
 * tests.
 * 
 * @author Seth
 */
public class JUnitElasticsearchServer extends ExternalResource {

    private Node m_node;
    private Path m_temporaryDirectory;

    @Override
    public void before() throws Exception {
        m_temporaryDirectory = Files.createTempDirectory("elasticsearch-data");

        ImmutableSettings.Builder elasticsearchSettings = ImmutableSettings.settingsBuilder()
            // By default, the service will listen on a free port from 9200 to 9300
            //.put("http.enabled", "false")
            //network.publish_host: 192.168.0.1
            .put("cluster.name", "opennms")
            .put("path.data", m_temporaryDirectory);

        m_node = NodeBuilder.nodeBuilder()
                //.local(true)
                .settings(elasticsearchSettings.build())
                .node();
    }

    public Client getClient() {
        return m_node.client();
    }

    @Override
    public void after() {
        m_node.close();

        try {
            FileUtils.deleteDirectory(new File(m_temporaryDirectory.toUri()));
        } catch (IOException e) {
            throw new RuntimeException("Could not delete data directory of embedded elasticsearch server", e);
        }
    }
}
