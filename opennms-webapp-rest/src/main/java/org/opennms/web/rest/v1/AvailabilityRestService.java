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
package org.opennms.web.rest.v1;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.category.AvailabilityIpInterface;
import org.opennms.web.category.AvailabilityMonitoredService;
import org.opennms.web.category.AvailabilityNode;
import org.opennms.web.category.Category;
import org.opennms.web.category.CategoryList;
import org.opennms.web.category.CategoryModel;
import org.opennms.web.category.NodeList;
import org.opennms.web.rest.v1.model.AvailabilityDataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for Availability/RTC information.
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @since 15.0.2
 */
@Component("availabilityRestService")
@Path("availability")
@Tag(name = "Availability", description = """
        Availability figures come from the RTC (real-time console) rolling window rather than from a fresh
        query, and are recomputed on the RTC's own schedule. All percentages are 0 to 100.

        The category groupings and their thresholds come from `categories.xml`. `last-updated` on a category
        is serialised as epoch milliseconds, not as the date-time string the derived schema shows.

        Categories are addressed by name in the path. Names commonly contain spaces, so they have to be
        percent-encoded (`Web%20Servers`); the handler URL-decodes the path segment before looking the
        category up.""")
@Transactional
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
public class AvailabilityRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(AvailabilityRestService.class);

    private static CategoryList m_categoryList;
    static {
        try {
            assertCategoryListExists();
        } catch (final ServletException e) {
            LOG.warn("Failed to create category list.", e);
        }
    }

    @Autowired
    private NodeDao m_nodeDao;

    private static void assertCategoryListExists() throws ServletException {
        if (m_categoryList == null) {
            m_categoryList = new CategoryList();
        }
    }

    @GET
    @Operation(
            summary = "Get availability for every category group",
            description = """
        Return every category group from `categories.xml` with the categories it contains and their current
        availability figures. The `nodes` array on a category lists every node id in that category.

        `last-updated` on each category is epoch milliseconds.""",
            operationId = "getAvailabilityForAllCategories"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category groups with their categories.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = AvailabilityDataResponse.class),
                                    examples = @ExampleObject(value = """
                    {
                      "section": [
                        {
                          "name": "Categories",
                          "categories": {
                            "totalCount": 8,
                            "count": 8,
                            "offset": 0,
                            "category": [
                              {
                                "name": "Web Servers",
                                "comment": "This category includes all managed interfaces which are running an HTTP (Web) server on port 80 or other common ports.",
                                "service-percentage": 100.0,
                                "service-down-count": 0,
                                "outage-class": "Normal",
                                "availability-class": "Warning",
                                "outage-text": "0 of 254",
                                "availability-text": "99.927%",
                                "nodes": [
                                  1,
                                  2,
                                  3
                                ],
                                "last-updated": 1787727304745,
                                "normal-threshold": 99.99,
                                "warning-threshold": 97.0,
                                "availability": 99.92666456601779
                              }
                            ]
                          }
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = AvailabilityDataResponse.class))
                    }),
            @ApiResponse(responseCode = "500", description = "The category data could not be read.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to get availability data: <cause>")))
    })
    public AvailabilityData getNodeAvailability() {
        try {
            return new AvailabilityData(m_categoryList.getCategoryData());
        } catch (final IOException e) {
            LOG.warn("Failed to get availability data: {}", e.getMessage(), e);
            throw getException(Status.INTERNAL_SERVER_ERROR, "Failed to get availability data: {}", e.getMessage());
        }
    }

    @GET
    @Path("/categories/{category}")
    @Operation(
            summary = "Get availability for one category",
            description = """
        Return the current availability figures for a single category from `categories.xml`, including the
        list of node ids it covers. `outage-class` and `availability-class` are the RTC's own severity
        labels, derived from the category's `normal-threshold` and `warning-threshold`. `last-updated` is
        epoch milliseconds.""",
            operationId = "getAvailabilityCategory"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The category and its availability figures.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = Category.class),
                                    examples = @ExampleObject(value = """
                    {
                      "name": "Web Servers",
                      "comment": "This category includes all managed interfaces which are running an HTTP (Web) server on port 80 or other common ports.",
                      "service-percentage": 100.0,
                      "service-down-count": 0,
                      "outage-class": "Normal",
                      "availability-class": "Warning",
                      "outage-text": "0 of 254",
                      "availability-text": "99.927%",
                      "nodes": [
                        1,
                        2,
                        3
                      ],
                      "last-updated": 1787727424662,
                      "normal-threshold": 99.99,
                      "warning-threshold": 97.0,
                      "availability": 99.92666456601779
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = Category.class))
                    }),
            @ApiResponse(responseCode = "404", description = "No category with that name is defined.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Category Web Serverz was not found."))),
            @ApiResponse(responseCode = "500", description = "The category data could not be read.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to get availability data for category Web Servers : <cause>")))
    })
    public Category getCategory(
            @Parameter(description = "Category name from categories.xml, percent-encoded. Case sensitive.",
                    required = true, example = "Web Servers")
            @PathParam("category") final String categoryName) {
        try {
            final String category = URLDecoder.decode(categoryName, StandardCharsets.UTF_8.name());
            final Category cat = CategoryModel.getInstance().getCategory(category);
            if (cat == null) {
                throw getException(Status.NOT_FOUND, "Category {} was not found.", categoryName);
            }
            return cat;
        } catch (final IOException e) {
            LOG.warn("Failed to get availability data for category {}: {}", categoryName, e.getMessage(), e);
            throw getException(Status.INTERNAL_SERVER_ERROR, "Failed to get availability data for category {} : {}", categoryName, e.getMessage());
        }
    }

    @GET
    @Path("/categories/{category}/nodes")
    @Operation(
            summary = "List the nodes in a category with their availability",
            description = """
        Return one entry per node in the category, each with the node's availability and service counts.
        The `ipinterfaces` array is empty on this operation.""",
            operationId = "getAvailabilityCategoryNodes"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nodes in the category.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = NodeList.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 254,
                      "count": 254,
                      "offset": 0,
                      "node": [
                        {
                          "id": 1,
                          "availability": 99.92797222222222,
                          "service-count": 1,
                          "service-down-count": 0,
                          "ipinterfaces": []
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = NodeList.class))
                    }),
            @ApiResponse(responseCode = "404", description = "No category with that name is defined.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Category Web Serverz was not found."))),
            @ApiResponse(responseCode = "500", description = "The category data could not be read.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to get availability data for category Web Servers : <cause>")))
    })
    public NodeList getCategoryNodes(
            @Parameter(description = "Category name from categories.xml, percent-encoded. Case sensitive.",
                    required = true, example = "Web Servers")
            @PathParam("category") final String categoryName) {
        try {
            final String category = URLDecoder.decode(categoryName, StandardCharsets.UTF_8.name());
            final Category cat = CategoryModel.getInstance().getCategory(category);
            if (cat == null) {
                throw getException(Status.NOT_FOUND, "Category {} was not found.", categoryName);
            }
            return cat.getNodes();
        } catch (final IOException e) {
            LOG.warn("Failed to get availability data for category {}: {}", categoryName, e.getMessage(), e);
            throw getException(Status.INTERNAL_SERVER_ERROR, "Failed to get availability data for category {} : {}", categoryName, e.getMessage());
        }
    }

    @GET
    @Path("/categories/{category}/nodes/{nodeId}")
    @Operation(
            summary = "Get availability for one node within a category",
            description = """
        Return the node's availability broken down by IP interface and monitored service. The category has
        to contain the node; membership is checked before the node is loaded.

        The result is the same payload as `GET /availability/nodes/{nodeId}`: the category only acts as a
        membership check and does not restrict which of the node's services are reported.

        A node id that is not in the category, or that does not exist, is reported as 500 rather than 404.""",
            operationId = "getAvailabilityCategoryNode"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node with its interfaces and services.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = AvailabilityNode.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": 1,
                      "availability": 99.92797222222222,
                      "service-count": 1,
                      "service-down-count": 0,
                      "ipinterfaces": [
                        {
                          "id": 1,
                          "address": "127.0.0.4",
                          "availability": 99.92797222222222,
                          "services": [
                            {
                              "up": true,
                              "id": 1022,
                              "name": "HTTP-8080",
                              "availability": 99.92797222222222
                            }
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = AvailabilityNode.class))
                    }),
            @ApiResponse(responseCode = "500", description = "The category is not defined, the node is not in it or does not exist, or the "
                    + "category data could not be read. The handler catches every failure, including the internal "
                    + "unknown-category 404, and rewraps it as 500, so this operation never answers 404.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to get availability data for category Web Servers : HTTP 404 Not Found")))
    })
    public AvailabilityNode getCategoryNode(
            @Parameter(description = "Category name from categories.xml, percent-encoded. Case sensitive.",
                    required = true, example = "Web Servers")
            @PathParam("category") final String categoryName,
            @Parameter(description = "Database id of the node.", required = true, example = "1")
            @PathParam("nodeId") final Long nodeId) {
        try {
            final String category = URLDecoder.decode(categoryName, StandardCharsets.UTF_8.name());
            final Category cat = CategoryModel.getInstance().getCategory(category);
            if (cat == null) {
                throw getException(Status.NOT_FOUND, "Category {} was not found.", categoryName);
            }
            final AvailabilityNode node = cat.getNode(nodeId);
            if (node == null) {
                throw getException(Status.NOT_FOUND, "Node {} was not found for category {}.", Long.toString(nodeId), categoryName);
            }
            return getAvailabilityNode(node.getId().intValue());
        } catch (final Exception e) {
            LOG.warn("Failed to get availability data for category {}: {}", categoryName, e.getMessage(), e);
            throw getException(Status.INTERNAL_SERVER_ERROR, "Failed to get availability data for category {} : {}", categoryName, e.getMessage());
        }
    }

    @GET
    @Path("/nodes/{nodeId}")
    @Operation(
            summary = "Get availability for one node",
            description = """
        Return the node's availability broken down by IP interface and monitored service. `up` on a service
        reflects the current service status, not the availability figure next to it.

        An unknown node id is reported as 500, not 404.""",
            operationId = "getAvailabilityNode"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The node with its interfaces and services.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = AvailabilityNode.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": 1,
                      "availability": 99.92797222222222,
                      "service-count": 1,
                      "service-down-count": 0,
                      "ipinterfaces": [
                        {
                          "id": 1,
                          "address": "127.0.0.4",
                          "availability": 99.92797222222222,
                          "services": [
                            {
                              "up": true,
                              "id": 1022,
                              "name": "HTTP-8080",
                              "availability": 99.92797222222222
                            }
                          ]
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = AvailabilityNode.class))
                    }),
            @ApiResponse(responseCode = "500", description = "The node does not exist, or its availability data could not be read.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Failed to get availability data for node 999999 : Cannot invoke \"org.opennms.netmgt.model.OnmsNode.getIpInterfaces()\" because \"dbNode\" is null")))
    })
    public AvailabilityNode getNode(
            @Parameter(description = "Database id of the node.", required = true, example = "1")
            @PathParam("nodeId") final Integer nodeId) {
        try {
            final AvailabilityNode avail = getAvailabilityNode(nodeId);
            if (avail == null) {
                throw getException(Status.NOT_FOUND, "Node {} was not found.", Integer.toString(nodeId));
            }
            return avail;
        } catch (final Exception e) {
            LOG.warn("Failed to get availability data for node {}: {}", nodeId, e.getMessage(), e);
            throw getException(Status.INTERNAL_SERVER_ERROR, "Failed to get availability data for node {} : {}", nodeId.toString(), e.getMessage());
        }
    }

    AvailabilityNode getAvailabilityNode(final int id) throws Exception {

        final OnmsNode dbNode = m_nodeDao.get(id);
        initialize(dbNode);

        if (dbNode == null) {
            throw getException(Status.NOT_FOUND, "Node {} was not found.", Integer.toString(id));
        }
        final double nodeAvail = CategoryModel.getNodeAvailability(id);

        final AvailabilityNode node = new AvailabilityNode(dbNode, nodeAvail);
        for (final OnmsIpInterface iface : dbNode.getIpInterfaces()) {
            final double ifaceAvail = CategoryModel.getInterfaceAvailability(id, str(iface.getIpAddress()));
            final AvailabilityIpInterface ai = new AvailabilityIpInterface(iface, ifaceAvail);
            for (final OnmsMonitoredService svc : iface.getMonitoredServices()) {
                final double serviceAvail = CategoryModel.getServiceAvailability(id, str(iface.getIpAddress()), svc.getServiceId());
                final AvailabilityMonitoredService ams = new AvailabilityMonitoredService(svc, serviceAvail, !svc.isDown());
                ai.addService(ams);
            }
            node.addIpInterface(ai);
        }
        return node;
    }

    private void initialize(final OnmsNode dbNode) {
        m_nodeDao.initialize(dbNode);
        m_nodeDao.initialize(dbNode.getIpInterfaces());
        for (final OnmsIpInterface iface : dbNode.getIpInterfaces()) {
            m_nodeDao.initialize(iface.getMonitoredServices());
        }
    }

    @XmlRootElement(name="availability")
    @XmlAccessorType(XmlAccessType.NONE)
    private static final class AvailabilityData {

        @XmlElement(name="section")
        private final List<CategoryRestInfo> m_categoryList = new ArrayList<>();

        @SuppressWarnings("unused")
        protected AvailabilityData() {}
        public AvailabilityData(final Map<String,List<Category>> categoryData) {
            for (final Map.Entry<String,List<Category>> entry : categoryData.entrySet()) {
                m_categoryList.add(new CategoryRestInfo(entry.getKey(), entry.getValue()));
            }
        }
    }

    @XmlRootElement(name="section")
    @XmlAccessorType(XmlAccessType.NONE)
    private static final class CategoryRestInfo {
        @XmlAttribute(name="name")
        private final String m_categoryName;

        @XmlElement(name="categories")
        private final CategoryRestList m_categories;

        @SuppressWarnings("unused")
        public CategoryRestInfo() {
            m_categoryName = null;
            m_categories = new CategoryRestList();
        }

        public CategoryRestInfo(final String categoryName, final List<Category> categories) {
            m_categoryName = categoryName;
            m_categories = new CategoryRestList(categories);
        }
    }

    @XmlRootElement(name="categories")
    @JsonRootName("categories")
    private static final class CategoryRestList extends JaxbListWrapper<Category> {
        private static final long serialVersionUID = 1L;

        public CategoryRestList() { super(); }
        public CategoryRestList(final Collection<? extends Category> categories) {
            super(categories);
        }

        @XmlElement(name="category")
        @JsonProperty("category")
        public List<Category> getObjects() {
            return super.getObjects();
        }
    }

    /**
     * Used for testing only.
     * @param dao
     */
    void setNodeDao(final NodeDao dao) {
        m_nodeDao = dao;
    }

}
