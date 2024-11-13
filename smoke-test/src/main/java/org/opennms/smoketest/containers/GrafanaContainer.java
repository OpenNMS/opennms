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
package org.opennms.smoketest.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.TestLifecycleAware;


/**
 * This class encapsulates all the logic required to start an
 * Grafana container and interface with the services
 * it provides.
 *
 * @author musaidali
 */
public class GrafanaContainer extends GenericContainer<GrafanaContainer> implements TestLifecycleAware  {

    public static final int WEB_PORT = 3000;
    public static final String GRAFANA_ALIAS = "grafana";
    public GrafanaContainer() {
        super("grafana/grafana:11.3.0");
            withEnv("GF_SECURITY_ADMIN_PASSWORD", "admin")
                .withNetwork(Network.SHARED)
                    .withExposedPorts(WEB_PORT);


    }
}
