/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
