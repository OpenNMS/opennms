/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.io.IOException;
import java.net.URLDecoder;
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

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
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
    public AvailabilityData getNodeAvailability() {
        try {
            return new AvailabilityData(m_categoryList.getCategoryData());
        } catch (final MarshalException | ValidationException | IOException e) {
            LOG.warn("Failed to get availability data: {}", e.getMessage(), e);
            throw getException(Status.INTERNAL_SERVER_ERROR, "Failed to get availability data: {}", e.getMessage());
        }
    }

    @GET
    @Path("/categories/{category}")
    public Category getCategory(@PathParam("category") final String categoryName) {
        try {
            final String category = URLDecoder.decode(categoryName, "UTF-8");
            final Category cat = CategoryModel.getInstance().getCategory(category);
            if (cat == null) {
                throw getException(Status.NOT_FOUND, "Category {} was not found.", categoryName);
            }
            return cat;
        } catch (final MarshalException | ValidationException | IOException e) {
            LOG.warn("Failed to get availability data for category {}: {}", categoryName, e.getMessage(), e);
            throw getException(Status.INTERNAL_SERVER_ERROR, "Failed to get availability data for category {} : {}", categoryName, e.getMessage());
        }
    }

    @GET
    @Path("/categories/{category}/nodes")
    public NodeList getCategoryNodes(@PathParam("category") final String categoryName) {
        try {
            final String category = URLDecoder.decode(categoryName, "UTF-8");
            final Category cat = CategoryModel.getInstance().getCategory(category);
            if (cat == null) {
                throw getException(Status.NOT_FOUND, "Category {} was not found.", categoryName);
            }
            return cat.getNodes();
        } catch (final MarshalException | ValidationException | IOException e) {
            LOG.warn("Failed to get availability data for category {}: {}", categoryName, e.getMessage(), e);
            throw getException(Status.INTERNAL_SERVER_ERROR, "Failed to get availability data for category {} : {}", categoryName, e.getMessage());
        }
    }

    @GET
    @Path("/categories/{category}/nodes/{nodeId}")
    public AvailabilityNode getCategoryNode(@PathParam("category") final String categoryName, @PathParam("nodeId") final Long nodeId) {
        try {
            final String category = URLDecoder.decode(categoryName, "UTF-8");
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
    public AvailabilityNode getNode(@PathParam("nodeId") final Integer nodeId) {
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
                final AvailabilityMonitoredService ams = new AvailabilityMonitoredService(svc, serviceAvail);
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
