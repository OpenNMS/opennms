/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.rest.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.netmgt.graph.api.GraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.graph.rest.api.GraphRestService;
import org.opennms.netmgt.graph.rest.impl.renderer.GraphRenderer;
import org.opennms.netmgt.graph.rest.impl.renderer.json.JsonGraphRenderer;
import org.opennms.netmgt.graph.rest.impl.renderer.xml.XmlGraphRenderer;

import com.google.common.base.Strings;

public class GraphRestServiceImpl implements GraphRestService {

    private GraphService graphService;

    public GraphRestServiceImpl(GraphService graphService) {
        this.graphService = Objects.requireNonNull(graphService);
    }

    @Override
    public Response listContainerInfo(String acceptHeader) {
        // TODO MVR a Graph implements GraphInfo and some of them return the graph (e.g. GraphML) as they are static anyways
        // and contain the information. However if that object is returned here, the whole graph is returned instead of just label,namespace,description,etc.
        // We should probably find a way to NOT do that
        final List<GraphContainerInfo> graphContainerInfos = graphService.getGraphContainerInfos();
        if (graphContainerInfos.isEmpty()) {
            return Response.noContent().build();
        }
        // TODO MVR duplicate code
        final MediaType contentType = parseContentType(acceptHeader);
        if (contentType == null) {
            return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
        }
        final String rendered = render(contentType, graphContainerInfos);
        return Response.ok(rendered).type(contentType).build();
    }

    @Override
    public Response getContainer(String acceptHeader, String containerId) {
        GraphContainer container = graphService.getGraphContainer(containerId);
        if (container == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        // TODO MVR duplicate code
        final MediaType contentType = parseContentType(acceptHeader);
        if (contentType == null) {
            return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
        }
        final String rendered = render(contentType, container);
        return Response.ok(rendered).type(contentType).build();
    }

    // Tries to parse the provided type
    protected static MediaType parseContentType(String type) {
        if (Strings.isNullOrEmpty(type)) {
            return null;
        }
        // If multi valued, try for each value, until one matches
        if (type.contains(",")) {
            final List<String> multipleTypes = Arrays.stream(type.split(","))
                    .map(String::trim)
                    .filter(value -> !Strings.isNullOrEmpty(value))
                    .collect(Collectors.toList());
            for (String eachType : multipleTypes) {
                final MediaType parsedType = parseContentType(eachType);
                if (parsedType != null) {
                    return parsedType;
                }
            }
        }
        final MediaType mediaType = MediaType.valueOf(type);
        if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE) || mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
            return mediaType;
        }
        return null;
    }

    private static String render(MediaType mediaType, List<GraphContainerInfo> infos) {
        return getRenderer(mediaType).render(infos);
    }

    private static String render(MediaType mediaType, GraphContainer graphContainer) {
        return getRenderer(mediaType).render(graphContainer);
    }

    protected static GraphRenderer getRenderer(MediaType mediaType) {
        if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
            return new JsonGraphRenderer();
        }
        if (mediaType.equals(MediaType.APPLICATION_XML_TYPE)) {
            return new XmlGraphRenderer();
        }
        throw new IllegalArgumentException("Cannot render '" + mediaType + "'");
    }

}
