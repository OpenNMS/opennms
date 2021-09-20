/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.enlinkd.model.UserDefinedLink;
import org.opennms.netmgt.enlinkd.persistence.api.UserDefinedLinkDao;
import org.opennms.netmgt.enlinkd.service.api.UserDefinedLinkTopologyService;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Path("userdefinedlinks")
@Transactional
public class UserDefinedLinkRestService extends AbstractDaoRestService<UserDefinedLink,UserDefinedLink,Integer,Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(NodeCategoriesRestService.class);

    @Autowired(required=false)
    private UserDefinedLinkDao m_dao;

    @Autowired(required=false)
    private UserDefinedLinkTopologyService m_service;

    @Override
    protected OnmsDao<UserDefinedLink, Integer> getDao() {
        return m_dao;
    }

    @Override
    protected Class<UserDefinedLink> getDaoClass() {
        return UserDefinedLink.class;
    }

    @Override
    protected Class<UserDefinedLink> getQueryBeanClass() {
        return UserDefinedLink.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        return new CriteriaBuilder(getDaoClass()).distinct();
    }

    @Override
    protected JaxbListWrapper<UserDefinedLink> createListWrapper(Collection<UserDefinedLink> list) {
        return new UserDefinedLinkCollection(list);
    }

    @Override
    protected UserDefinedLink doGet(UriInfo uriInfo, Integer id) {
        return m_dao.get(id);
    }

    @Override
    public Response doCreate(final SecurityContext securityContext, final UriInfo uriInfo, final UserDefinedLink udl) {
        if (udl == null) {
            throw getException(Response.Status.BAD_REQUEST, "Link cannot be null");
        }
        m_service.saveOrUpdate(udl);
        return Response.created(RedirectHelper.getRedirectUri(uriInfo, udl.getDbId())).build();
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, UserDefinedLink udl, MultivaluedMapImpl params) {
        RestUtils.setBeanProperties(udl, params);
        m_service.saveOrUpdate(udl);
        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, UserDefinedLink udl) {
        m_service.delete(udl);
    }

    @XmlRootElement(name = "user_defined_links")
    @JsonRootName("user_defined_links")
    public static class UserDefinedLinkCollection extends JaxbListWrapper<UserDefinedLink> {
        private static final long serialVersionUID = 1L;

        public UserDefinedLinkCollection() { super(); }

        public UserDefinedLinkCollection(final Collection<? extends UserDefinedLink> udls) {
            super(udls);
        }

        @XmlElement(name="user_defined_link")
        @JsonProperty("user_defined_link")
        public List<UserDefinedLink> getObjects() {
            return super.getObjects();
        }

    }

}
