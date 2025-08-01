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
package org.opennms.features.topology.link;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Helper class to correctly build a link to the Topology UI.
 *
 * @author mvrueden.
 */
public class TopologyLinkBuilder {
    public static final String PARAMETER_LAYOUT = "layout";
    public static final String PARAMETER_FOCUS_VERTICES = "focus-vertices";
    public static final String PARAMETER_SEMANTIC_ZOOM_LEVEL = "szl";
    public static final String PARAMETER_GRAPH_PROVIDER = "provider";
    public static final String PARAMETER_LAYER_NAMESPACE = "layer-namespace";
    private static final String TOPOLOGY_URL = "/opennms/topology";

    private Layout layout;
    private List<String> vertexIds = new ArrayList<>();
    private int szl = 1;
    private TopologyProvider provider = TopologyProvider.ENLINKD;
    private String layer;

    public TopologyLinkBuilder() {

    }

    public TopologyLinkBuilder layout(String layoutString) {
        return layout(Layout.createFromLabel(layoutString));
    }

    public TopologyLinkBuilder layout(Layout layout) {
        if (layout != null) {
            this.layout = layout;
        }
        return this;
    }

    public TopologyLinkBuilder focus(String... vertexId) {
        if (vertexId != null) {
            return focus(Arrays.asList(vertexId));
        }
        return this;
    }

    public TopologyLinkBuilder focus(List<String> vertexIds) {
        if (vertexIds != null) {
            vertexIds.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .collect(Collectors.toList());
            this.vertexIds = Collections.unmodifiableList(vertexIds);
        }
        return this;
    }

    public TopologyLinkBuilder szl(int szl) {
        if (szl >= 0) {
            this.szl = szl;
        } else {
            this.szl = 1;
        }
        return this;
    }

    public TopologyLinkBuilder provider(TopologyProvider provider) {
        if (provider != null && provider.getLabel() != null && !provider.getLabel().isEmpty()) {
            this.provider = provider;
        }
        return this;
    }

    // By default the default layer is always selected.
    // Use this to override the layer you want to select
    public TopologyLinkBuilder layer(String namespace) {
        if (namespace != null && !namespace.isEmpty()) {
            this.layer = namespace;
        }
        return this;
    }

    public String getLink() {
        final List<String> parameters = new ArrayList<>();
        try {
            parameters.add(parameter(PARAMETER_GRAPH_PROVIDER, provider.getLabel()));
            parameters.add(parameter(PARAMETER_SEMANTIC_ZOOM_LEVEL, Integer.toString(szl)));

            // only add parameter, if we have a focus defined
            if (!vertexIds.isEmpty()) {
                parameters.add(parameter(PARAMETER_FOCUS_VERTICES, vertexIds.stream().collect(Collectors.joining(","))));
            }

            // only add parameter, if we have a layout defined
            if (layout != null) {
                parameters.add(parameter(PARAMETER_LAYOUT, layout.getLabel()));
            }

            // only add parameter, if a layer was defined
            if (layer != null) {
                parameters.add(parameter(PARAMETER_LAYER_NAMESPACE, layer));
            }

            // If we have parameters, build the link
            if (!parameters.isEmpty()) {
                return TOPOLOGY_URL + "?" + parameters.stream().collect(Collectors.joining("&"));
            }
            return TOPOLOGY_URL;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String parameter(String parameterName, String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(parameterName, StandardCharsets.UTF_8.name()) + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    }
}
