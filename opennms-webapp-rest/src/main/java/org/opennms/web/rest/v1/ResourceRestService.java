/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import static org.opennms.web.svclayer.support.DefaultGraphResultsService.RESOURCE_IDS_CONTEXT;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.opennms.netmgt.model.resource.ResourceDTOCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

/**
 * Read-only API for retrieving resources.
 *
 * @author jwhite
 */
@Component("resourceRestService")
@Path("resources")
public class ResourceRestService extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceRestService.class);

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ResourceDao m_resourceDao;

    @Autowired
    private FilterDao m_filterDao;

    @Autowired
    private JsonStore m_jsonStore;

    private final Gson m_gson = new Gson();

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public ResourceDTOCollection getResources(@DefaultValue("1") @QueryParam("depth") final int depth) {
        List<ResourceDTO> resources = Lists.newLinkedList();
        for (OnmsResource resource : m_resourceDao.findTopLevelResources()) {
            resources.add(ResourceDTO.fromResource(resource, depth));
        }
        return new ResourceDTOCollection(resources);
    }

    @GET
    @Path("{resourceId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public ResourceDTO getResourceById(@PathParam("resourceId") final String resourceId,
            @DefaultValue("-1") @QueryParam("depth") final int depth) {
        OnmsResource resource = m_resourceDao.getResourceById(ResourceId.fromString(resourceId));
        if (resource == null) {
            throw getException(Status.NOT_FOUND, "No resource with id '{}' found.", resourceId);
        }

        return ResourceDTO.fromResource(resource, depth);
    }

    @DELETE
    @Path("{resourceId}")
    @Transactional(readOnly=false)
    public void deleteResourceById(@PathParam("resourceId") final String resourceId) {
        final boolean found = m_resourceDao.deleteResourceById(ResourceId.fromString(resourceId));

        if (!found) {
            throw getException(Status.NOT_FOUND, "No resource with id '{}' found.", resourceId);
        }
    }


    @GET
    @Path("fornode/{nodeCriteria}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public ResourceDTO getResourceForNode(@PathParam("nodeCriteria") final String nodeCriteria,
            @DefaultValue("-1") @QueryParam("depth") final int depth) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.NOT_FOUND, "No node found with criteria '{}'.", nodeCriteria);
        }

        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        if (resource == null) {
            throw getException(Status.NOT_FOUND, "No resource found for node with id {}.", "" + node.getId());
        }

        return ResourceDTO.fromResource(resource, depth);
    }

    /**
     * Selects nodes and parts of there resource information.
     * <ul>
     *   <li>If no {@code nodeSubresources} criteria is given then all subresources are returned but their nested content is not included.</li>
     *   <li>If no {@code stringProperties} criteria is given then all string properties are included.</li>
     * </ul>
     *
     * @param nodes Comma separated list of node ids; both, database ids and foreign ids are supported
     * @param filterRules Comma separated list of rule names for selecting nodes
     * @param nodeSubresources Comma separated list of subresource names (the part after the dot)
     * @param stringProperties Comma separated list of property names
     * @return
     */
    @GET
    @Path("select")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public List<ResourceDTO> select(
            @DefaultValue("") @QueryParam("nodes") String nodes,
            @DefaultValue("") @QueryParam("filterRules") String filterRules,
            @DefaultValue("") @QueryParam("nodeSubresources") String nodeSubresources,
            @DefaultValue("") @QueryParam("stringProperties") String stringProperties
    ) {
        var allNodeIds = Stream.of(nodes.split(","))
                .map(String::trim)
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.toSet());

        var ruleNodeIds = Stream.of(filterRules.split(","))
                .map(String::trim)
                .filter(StringUtils::isNoneBlank)
                .flatMap(s -> m_filterDao.getNodeMap(s).keySet().stream().map(String::valueOf))
                .collect(Collectors.toSet());

        allNodeIds.addAll(ruleNodeIds);

        var subresourceNames = Stream.of(nodeSubresources.split(","))
                .map(String::trim)
                .filter(StringUtils::isNoneBlank)
                .distinct()
                .collect(Collectors.toList());

        var stringPropertyNames = Stream.of(stringProperties.split(","))
                .map(String::trim)
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.toSet());

        var resources = allNodeIds.stream().sorted()
                .map(nodeId -> {
                    if (nodeId.contains(":")) {
                        var node = m_nodeDao.get(nodeId);
                        return node != null ? m_resourceDao.getResourceForNode(node) : null;
                    } else {
                        return m_resourceDao.getResourceById(ResourceId.get("node", nodeId));
                    }
                })
                .filter(r -> r != null)
                .map(r -> ResourceDTO.fromResource(r, -1))
                .map(nodeResource -> {
                    nodeResource.setRrdGraphAttributes(null);
                    nodeResource.setStringPropertyAttributes(null);
                    nodeResource.setExternalValueAttributes(null);
                    if (nodeResource.getChildren() == null) {
                        return nodeResource;
                    }
                    // prune the node resource
                    // -> include only selected parts if selections are available
                    if (subresourceNames.isEmpty()) {
                        // no subresources are selected
                        // -> include all subresources but strip their content
                        // -> information about children but not about their content is required for browsing
                        for (var c : nodeResource.getChildren()) {
                            c.setChildren(null);
                            c.setRrdGraphAttributes(null);
                            c.setStringPropertyAttributes(null);
                            c.setExternalValueAttributes(null);
                        }
                    } else {
                        // include only those children that match any of the given subresource names
                        for (var subresourceIterator = nodeResource.getChildren().iterator(); subresourceIterator.hasNext(); ) {
                            var subresource = subresourceIterator.next();
                            if (subresourceNames.stream().anyMatch(name -> subresource.getId().endsWith("." + name))) {
                                // this subresource is selected
                                // -> we are not interested in its children, rrd graph attributes, and external value attributes
                                subresource.setChildren(null);
                                subresource.setRrdGraphAttributes(null);
                                subresource.setExternalValueAttributes(null);
                                // check if specific string properties or external values are selected
                                // -> in that case only include these ones
                                // -> otherwise include all
                                if (!stringPropertyNames.isEmpty()) {
                                    // -> include only selected stringProperties and externalValues
                                    for (var iter = subresource.getStringPropertyAttributes().keySet().iterator(); iter.hasNext(); ) {
                                        var s = iter.next();
                                        if (!stringPropertyNames.contains(s)) {
                                            iter.remove();
                                        }
                                    }
                                }
                            } else {
                                // subresource is not selected
                                // -> remove it from result
                                subresourceIterator.remove();
                            }
                        }
                    }
                    return nodeResource;
                });

        return resources.collect(Collectors.toList());
    }

    @POST
    @Path("generateId")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response saveResourcesWithId(String[] resources) {

        String resourcesInJson = m_gson.toJson(resources);
        // The generated key will be same for a given input.
        String key = UUID.nameUUIDFromBytes(resourcesInJson.getBytes()).toString();
        m_jsonStore.put(key, resourcesInJson, RESOURCE_IDS_CONTEXT);
        String jsonKey = m_gson.toJson(key);
        return Response.ok(jsonKey).build();

    }

}
