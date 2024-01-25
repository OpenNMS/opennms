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
package org.opennms.smoketest.graphml;

import java.io.InputStream;
import java.util.Objects;

import org.opennms.smoketest.utils.RestClient;

public class GraphmlDocument {

    private final String name;
    private final String resource;

    public GraphmlDocument(final String graphName, final String graphResource) {
        this.name = Objects.requireNonNull(graphName);
        this.resource = Objects.requireNonNull(graphResource);
        Objects.requireNonNull(getResourceAsStream());
    }

    public void create(final RestClient client) {
        if (client.getGraphML(name).getStatus() == 404) {
            client.sendGraphML(name, getResourceAsStream());
        }
    }

    public void delete(final RestClient client) {
        if (client.getGraphML(name).getStatus() != 404) {
            client.deleteGraphML(name);
        }
    }

    public boolean exists(RestClient client) {
        return client.getGraphML(name).getStatus() == 200;
    }

    private InputStream getResourceAsStream() {
        return getClass().getResourceAsStream(resource);
    }
}
