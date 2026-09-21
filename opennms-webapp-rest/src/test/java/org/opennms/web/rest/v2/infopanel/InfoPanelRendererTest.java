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
package org.opennms.web.rest.v2.infopanel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

/**
 * Unit-tests the Jinjava info-panel rendering against a temporary template
 * directory and mocked DAOs -- no Spring context or database.
 */
public class InfoPanelRendererTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private NodeDao nodeDao;
    private ResourceDao resourceDao;
    private OnmsNode node;

    @Before
    public void setUp() {
        nodeDao = mock(NodeDao.class);
        resourceDao = mock(ResourceDao.class);
        when(resourceDao.getResourceForNode(org.mockito.ArgumentMatchers.any())).thenReturn(null);
        node = new OnmsNode();
        node.setId(42);
        node.setLabel("test-node");
    }

    private void writeTemplate(final String name, final String body) throws Exception {
        final Path p = tmp.getRoot().toPath().resolve(name);
        Files.write(p, body.getBytes(StandardCharsets.UTF_8));
    }

    private InfoPanelRenderer renderer() {
        return new InfoPanelRenderer(nodeDao, resourceDao, null, tmp.getRoot().toPath());
    }

    @Test
    public void rendersVisibleTemplatesSortedByOrderWithNodeContext() throws Exception {
        writeTemplate("a-second.html",
                "{% set visible = true %}{% set title = \"Second\" %}{% set order = 10 %}"
                        + "<b>{{ node.label }}</b> (id {{ node.id }})");
        writeTemplate("b-first.html",
                "{% set visible = true %}{% set title = \"First\" %}{% set order = 5 %}hello");

        final List<InfoPanelItem> items = renderer().renderForNode(node);

        assertThat(items, hasSize(2));
        // sorted by order ascending regardless of filename
        assertThat(items.stream().map(InfoPanelItem::getTitle).collect(Collectors.toList()),
                contains("First", "Second"));
        assertThat(items.get(0).getOrder(), is(5));
        assertThat(items.get(1).getOrder(), is(10));
        // node context resolved
        assertThat(items.get(1).getHtml(), containsString("<b>test-node</b> (id 42)"));
    }

    @Test
    public void skipsTemplatesThatAreNotVisible() throws Exception {
        writeTemplate("hidden.html", "{% set visible = false %}{% set title = \"Nope\" %}should not show");

        assertThat(renderer().renderForNode(node), is(empty()));
    }

    @Test
    public void missingTemplateDirYieldsNoItems() {
        final Path absent = tmp.getRoot().toPath().resolve("does-not-exist");
        final InfoPanelRenderer r = new InfoPanelRenderer(nodeDao, resourceDao, null, absent);
        assertThat(r.renderForNode(node), is(empty()));
    }

    @Test
    public void rendersEdgeScopedTemplatesWithEdgeContext() throws Exception {
        writeTemplate("edge-panel.html",
                "{% if edge %}{% set visible = true %}{% set title = \"Link\" %}"
                        + "{{ edge.discoveredBy }}: {{ edge.sourcePort.node.label }}/{{ edge.sourcePort.ifName }}"
                        + " -> {{ edge.targetPort.node.label }}/{{ edge.targetPort.ifIndex }}{% endif %}");
        writeTemplate("node-panel.html",
                "{% if node %}{% set visible = true %}{% set title = \"Node\" %}{{ node.label }}{% endif %}");

        final OnmsNode target = new OnmsNode();
        target.setId(43);
        target.setLabel("far-node");
        final OnmsSnmpInterface snmp = new OnmsSnmpInterface();
        snmp.setIfIndex(7);
        final EdgeInfo edge = new EdgeInfo("lldp",
                new EdgeInfo.Port(node, "eth0", null),
                new EdgeInfo.Port(target, "eth7", snmp));

        final List<InfoPanelItem> items = renderer().renderForEdge(edge);
        // the node-scoped template guards on `node` and must not render
        assertThat(items, hasSize(1));
        assertThat(items.get(0).getTitle(), is("Link"));
        assertThat(items.get(0).getHtml(), is("lldp: test-node/eth0 -> far-node/7"));
    }

    @Test
    public void nodeScopedRenderDoesNotExposeEdge() throws Exception {
        writeTemplate("edge-panel.html",
                "{% if edge %}{% set visible = true %}{% set title = \"Link\" %}x{% endif %}");
        assertThat(renderer().renderForNode(node), is(empty()));
    }
}
