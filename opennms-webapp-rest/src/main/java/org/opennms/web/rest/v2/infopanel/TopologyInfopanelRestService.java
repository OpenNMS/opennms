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

import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Serves operator-configured topology info-panel content for a node: the
 * rendered {@code $OPENNMS_HOME/etc/infopanel/} Jinjava templates as
 * {@code [{title, order, html}]}. The new Vue topology Inspector appends these
 * below its native fields, so the existing template investment carries over.
 *
 * <p>Read-only and open to any authenticated user via the default
 * {@code /api/v2/**} security rules. {@code @Transactional} keeps a Hibernate
 * session open so templates can traverse lazy node/resource associations.
 */
@Component
@Path("topology/infopanel")
@Tag(name = "Topology", description = "Topology map APIs")
@Produces(MediaType.APPLICATION_JSON)
public class TopologyInfopanelRestService {

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ResourceDao m_resourceDao;

    // Optional: templates that embed metrics use it; absent installs degrade
    // gracefully (those template sections are skipped, not fatal to the rest).
    @Autowired(required = false)
    private MeasurementsService m_measurementsService;

    private volatile InfoPanelRenderer m_renderer;

    @GET
    @Transactional(readOnly = true)
    public List<InfoPanelItem> getForNode(@QueryParam("nodeId") final Integer nodeId) {
        if (nodeId == null) {
            throw webException(Response.Status.BAD_REQUEST, "A nodeId query parameter is required");
        }
        final OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            throw webException(Response.Status.NOT_FOUND, "No node with id " + nodeId);
        }
        return renderer().renderForNode(node);
    }

    /**
     * Edge-scoped panels for a link between two nodes: templates render with
     * an {@code edge} context (see {@link EdgeInfo}). Port names and protocol
     * are optional -- they come from a link's discovery binding when present.
     */
    @GET
    @javax.ws.rs.Path("edge")
    @Transactional(readOnly = true)
    public List<InfoPanelItem> getForEdge(@QueryParam("sourceNodeId") final Integer sourceNodeId,
                                          @QueryParam("targetNodeId") final Integer targetNodeId,
                                          @QueryParam("sourcePort") final String sourcePort,
                                          @QueryParam("targetPort") final String targetPort,
                                          @QueryParam("protocol") final String protocol) {
        if (sourceNodeId == null || targetNodeId == null) {
            throw webException(Response.Status.BAD_REQUEST, "sourceNodeId and targetNodeId query parameters are required");
        }
        final OnmsNode source = m_nodeDao.get(sourceNodeId);
        final OnmsNode target = m_nodeDao.get(targetNodeId);
        if (source == null || target == null) {
            throw webException(Response.Status.NOT_FOUND,
                    "No node with id " + (source == null ? sourceNodeId : targetNodeId));
        }
        final EdgeInfo edge = new EdgeInfo(protocol,
                new EdgeInfo.Port(source, sourcePort, resolveSnmpInterface(source, sourcePort)),
                new EdgeInfo.Port(target, targetPort, resolveSnmpInterface(target, targetPort)));
        return renderer().renderForEdge(edge);
    }

    /**
     * Match a persisted port label against the node's SNMP interfaces by
     * ifName, then ifDescr (the two labels discovery writes). Null when the
     * port is unknown or nothing matches -- templates handle a null
     * {@code snmpInterface}/{@code ifIndex} themselves.
     */
    private static OnmsSnmpInterface resolveSnmpInterface(final OnmsNode node, final String port) {
        if (port == null || port.isBlank()) {
            return null;
        }
        return node.getSnmpInterfaces().stream()
                .filter(s -> port.equals(s.getIfName()) || port.equals(s.getIfDescr()))
                .findFirst()
                .orElse(null);
    }

    private InfoPanelRenderer renderer() {
        if (m_renderer == null) {
            synchronized (this) {
                if (m_renderer == null) {
                    final java.nio.file.Path dir = Paths.get(System.getProperty("opennms.home", "."), "etc", "infopanel");
                    m_renderer = new InfoPanelRenderer(m_nodeDao, m_resourceDao, m_measurementsService, dir);
                }
            }
        }
        return m_renderer;
    }

    private static javax.ws.rs.WebApplicationException webException(final Response.Status status, final String message) {
        return new javax.ws.rs.WebApplicationException(
                Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}
