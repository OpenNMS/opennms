/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.rest.v1.support.OnmsMonitoringLocationDefinitionList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsMonitoringLocation} entity
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 */
@Component
@Path("monitoringLocations")
@Transactional
public class MonitoringLocationRestService extends AbstractDaoRestService<OnmsMonitoringLocation,OnmsMonitoringLocation,String,String> {

    @Autowired
    private MonitoringLocationDao m_dao;

    @Override
    protected MonitoringLocationDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsMonitoringLocation> getDaoClass() {
        return OnmsMonitoringLocation.class;
    }

    @Override
    protected Class<OnmsMonitoringLocation> getQueryBeanClass() {
        return OnmsMonitoringLocation.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMonitoringLocation.class);

        // Order by location name by default
        builder.orderBy("locationName").asc();

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsMonitoringLocation> createListWrapper(Collection<OnmsMonitoringLocation> list) {
        return new OnmsMonitoringLocationDefinitionList(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.LOCATION_SERVICE_PROPERTIES;
    }

    @Override
    protected OnmsMonitoringLocation doGet(UriInfo uriInfo, String id) {
        return getDao().get(id);
    }

    @Override
    public Response doCreate(final SecurityContext securityContext, final UriInfo uriInfo, final OnmsMonitoringLocation object) {
        final String id = getDao().save(object);
        return Response.created(RedirectHelper.getRedirectUri(uriInfo, id)).build();
    }

    @Override
    protected Response doUpdate(final SecurityContext securityContext, final UriInfo uriInfo, final String key, final OnmsMonitoringLocation targetObject) {
        if (!key.equals(targetObject.getLocationName())) {
            throw getException(Status.BAD_REQUEST, "The ID of the object doesn't match the ID of the path: {} != {}", targetObject.getLocationName(), key);
        }
        getDao().saveOrUpdate(targetObject);
        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsMonitoringLocation object) {
        getDao().delete(object);
    }
}
