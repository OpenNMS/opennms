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

package org.opennms.web.rest;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * Read-only API for retrieving resources.
 *
 * @author jwhite
 */
@Component
@Scope("prototype")
@Path("resources")
public class ResourceRestService extends OnmsRestService {

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ResourceDao m_resourceDao;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public ResourceDTOCollection getResources(@DefaultValue("1") @QueryParam("depth") final int depth) {
        List<ResourceDTO> resources = Lists.newLinkedList();
        for (OnmsResource resource : m_resourceDao.findTopLevelResources()) {
            resources.add(new ResourceDTO(resource, depth));
        }
        return new ResourceDTOCollection(resources);
    }

    @GET
    @Path("{resourceId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public ResourceDTO getResourceById(@PathParam("resourceId") final String resourceId,
            @DefaultValue("-1") @QueryParam("depth") final int depth) {
        OnmsResource resource = m_resourceDao.getResourceById(resourceId);
        if (resource == null) {
            throw getException(Status.NOT_FOUND, "No resource with id '{}' found.", resourceId);
        }

        return new ResourceDTO(resource, depth);
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

        return new ResourceDTO(resource, depth);
    }

    @XmlRootElement(name = "resources")
    public static final class ResourceDTOCollection extends JaxbListWrapper<ResourceDTO> {

        private static final long serialVersionUID = 1L;

        public ResourceDTOCollection() {
            super();
        }

        public ResourceDTOCollection(Collection<? extends ResourceDTO> resources) {
            super(resources);
        }

        @XmlElement(name = "resource")
        public List<ResourceDTO> getObjects() {
            return super.getObjects();
        }
    }

    @XmlRootElement(name = "resource")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class ResourceDTO {

        @XmlAttribute(name = "id")
        private final String m_id;

        @XmlAttribute(name = "label")
        private final String m_label;

        @XmlAttribute(name = "name")
        private final String m_name;

        @XmlAttribute(name = "link")
        private final String m_link;

        @XmlAttribute(name = "parentId")
        private final String m_parentId;

        @XmlElement(name="children")
        private final ResourceDTOCollection m_children;

        @XmlElementWrapper(name="stringPropertyAttributes")
        private final Map<String, String> m_stringPropertyAttributes;

        @XmlElementWrapper(name="externalValueAttributes")
        private final Map<String, String> m_externalValueAttributes;

        @XmlElementWrapper(name="rrdGraphAttributes")
        private final Map<String, RrdGraphAttribute> m_rrdGraphAttributes;

        @SuppressWarnings("unused")
        private ResourceDTO() {
            throw new UnsupportedOperationException("No-arg constructor for JAXB.");
        }

        public ResourceDTO(final OnmsResource resource) {
            this(resource, -1);
        }

        public ResourceDTO(final OnmsResource resource, final int depth) {
            m_id = resource.getId();
            m_label = resource.getLabel();
            m_name = resource.getName();
            m_link = resource.getLink();
            m_parentId = resource.getParent() == null ? null : resource.getParent().getId();
            m_stringPropertyAttributes = resource.getStringPropertyAttributes();
            m_externalValueAttributes = resource.getExternalValueAttributes();
            m_rrdGraphAttributes = resource.getRrdGraphAttributes();

            if (depth == 0) {
                m_children = null;
            } else {
                List<ResourceDTO> children = Lists.newLinkedList();
                for (final OnmsResource child : resource.getChildResources()) {
                    children.add(new ResourceDTO(child, depth-1));
                }
                m_children = new ResourceDTOCollection(children);
            }
        }

        public String getId() {
            return m_id;
        }

        public String getLabel() {
            return m_label;
        }

        public String getName() {
            return m_name;
        }

        public String getLink() {
            return m_link;
        }

        public String getParentId() {
            return m_parentId;
        }

        public ResourceDTOCollection getChildren() {
            return m_children;
        }

        public Map<String, String> getStringPropertyAttributes() {
            return m_stringPropertyAttributes;
        }

        public Map<String, String> getExternalValueAttributes() {
            return m_externalValueAttributes;
        }

        public Map<String, RrdGraphAttribute> getRrdGraphAttributes() {
            return m_rrdGraphAttributes;
        }
    }
}
