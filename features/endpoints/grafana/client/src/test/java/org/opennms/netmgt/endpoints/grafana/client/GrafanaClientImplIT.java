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
package org.opennms.netmgt.endpoints.grafana.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.opennms.netmgt.endpoints.grafana.api.Dashboard;
import org.opennms.netmgt.endpoints.grafana.api.Panel;
import org.opennms.netmgt.endpoints.grafana.api.PanelContainer;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class GrafanaClientImplIT {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort());

    @Test
    public void grafana8DashboardResponse() throws Exception {
        stubFor(get(urlEqualTo("/api/dashboards/uid/T2Ra73RVk"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("dashboard8.json")));

        GrafanaServerConfiguration config = new GrafanaServerConfiguration(wireMockRule.baseUrl(), "xxxx", 5, 5);
        GrafanaClientImpl client = new GrafanaClientImpl(config);
        Dashboard dashboard = client.getDashboardByUid("T2Ra73RVk");

        assertThat(dashboard.getMeta().getSlug(), equalTo("test"));
        assertThat(dashboard.getPanels().get(0).getDatasource(), equalTo("ecy5MqR4z"));
    }

    @Test
    public void canGetDashboardAndRenderPng() throws IOException, ExecutionException, InterruptedException {
        stubFor(get(urlEqualTo("/api/dashboards/uid/eWsVEL6zz"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("dashboard.json")));

        GrafanaServerConfiguration config = new GrafanaServerConfiguration(wireMockRule.baseUrl(), "xxxx", 5, 5);
        GrafanaClientImpl client = new GrafanaClientImpl(config);
        Dashboard dashboard = client.getDashboardByUid("eWsVEL6zz");

        assertThat(dashboard.getMeta().getSlug(), equalTo("flows"));
        assertThat(dashboard.getMeta().isStarred(), equalTo(true));
        assertThat(dashboard.getMeta().getVersion(), equalTo(6));
        assertThat(dashboard.getTags(), contains("cool", "dude"));
        assertThat(dashboard.getTimezone(), equalTo("browser"));

        assertThat(panelTitles(dashboard), contains("Traffic (Flows)", "Traffic by Application",
                "Traffic by Application",
                "Traffic (SNMP via MIB-2)",
                "MIB-2 Traffic",
                "MIB-2 Errors and Discards",
                "Conversation (Flows)"));

        Panel row = dashboard.getPanels().get(6);
        assertThat(panelTitles(row), contains("Traffic by Conversation (Top N)", "Traffic by Conversation (Top N)"));

        Panel panel = dashboard.getPanels().get(1);
        assertThat(panel.getId(), equalTo(3));
        assertThat(panel.getDatasource(), equalTo("minion-dev (Flow)"));
        assertThat(panel.getDescription(), equalTo("igb0"));

        stubFor(get(urlEqualTo("/render/d-solo/eWsVEL6zz/flows?panelId=3&from=0&to=1&width=128&height=128&timeout=5&theme=light"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "image/png")
                        .withBodyFile("panel.png")));

        byte[] pngBytes = client.renderPngForPanel(dashboard, panel, 128, 128, 0L, 1L, null, Collections.emptyMap()).get();
        assertThat(pngBytes.length, equalTo(6401));
    }

    private static List<String> panelTitles(PanelContainer container) {
        return container.getPanels().stream().map(Panel::getTitle).collect(Collectors.toList());
    }
}
