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
    private static final String TOPOLOGY_URL = "/opennms/topology";

    private Layout layout;
    private List<String> vertexIds = new ArrayList<>();
    private int szl = 1;
    private TopologyProvider provider = TopologyProvider.ENLINKD;

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
