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

package org.opennms.web.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.web.category.Category;
import org.opennms.web.category.CategoryList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.api.core.ResourceContext;
import com.sun.jersey.spi.resource.PerRequest;

/**
 * Basic Web Service using REST for Availability/RTC information.
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @since 15.0.2
 */
@Component
@PerRequest
@Scope("prototype")
@Path("availability")
@Transactional
public class AvailabilityRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(AvailabilityRestService.class);

    private static CategoryList m_categoryList;
    static {
        try {
            assertCategoryListExists();
        } catch (final ServletException e) {
            LOG.debug("Failed to create category list.", e);
        }
    }

    @Context 
    UriInfo m_uriInfo;

    @Context
    ResourceContext m_context;

    @Context
    SecurityContext m_securityContext;

    private static void assertCategoryListExists() throws ServletException {
        if (m_categoryList == null) {
            m_categoryList = new CategoryList();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public AvailabilityData getNodeAvailability() {
        readLock();
        try {
            final Map<String, List<Category>> categoryData = m_categoryList.getCategoryData();
            LOG.debug("categoryData: {}", categoryData);
            final AvailabilityData availabilityData = new AvailabilityData(categoryData);
            try {
                final String s = JaxbUtils.marshal(availabilityData);
                System.err.println(s);
            } catch (final RuntimeException e) {
                e.printStackTrace();
            }
            return availabilityData;
        } catch (final MarshalException | ValidationException | IOException e) {
            LOG.debug("Failed to get availability data.", e);
            throw getException(Status.BAD_REQUEST, "Failed to get availability data.");
        } finally {
            readUnlock();
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
    private static final class CategoryRestList extends JaxbListWrapper<Category> {
        private static final long serialVersionUID = 1L;

        public CategoryRestList() { super(); }
        public CategoryRestList(final Collection<? extends Category> categories) {
            super(categories);
        }

        @XmlElement(name="category")
        public List<Category> getObjects() {
            return super.getObjects();
        }
    }
}
