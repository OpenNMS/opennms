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
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.rest.v1.support.OnmsMonitoringLocationDefinitionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private static final Logger LOG = LoggerFactory.getLogger(MonitoringLocationRestService.class);

    @Autowired
    private MonitoringLocationDao m_dao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

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
    public Response doCreate(final SecurityContext securityContext, final UriInfo uriInfo, final OnmsMonitoringLocation location) {
        final boolean sendEvent = location.getPollingPackageNames() != null && !location.getPollingPackageNames().isEmpty();

        final String id = getDao().save(location);

        if (sendEvent) {
            final EventBuilder eventBuilder = new EventBuilder(EventConstants.POLLER_PACKAGE_LOCATION_ASSOCIATION_CHANGED_EVENT_UEI, "ReST");
            eventBuilder.addParam(EventConstants.PARM_LOCATION, location.getLocationName());
            try {
                m_eventProxy.send(eventBuilder.getEvent());
            } catch (final EventProxyException e) {
                LOG.warn("Failed to send Event on creation of location " + e.getMessage(), e);
            }
        }

        return Response.created(RedirectHelper.getRedirectUri(uriInfo, id)).build();
    }

    private boolean comparePollingPackageNames(final OnmsMonitoringLocation aLocation, final OnmsMonitoringLocation bLocation) {
        if (aLocation != null && bLocation != null) {
            final List<String> aPkgs = aLocation.getPollingPackageNames();
            final List<String> bPkgs = bLocation.getPollingPackageNames();
            if (aPkgs != null && bPkgs != null) {
                return aPkgs.containsAll(bPkgs) && bPkgs.containsAll(aPkgs);
            } else {
                return (aPkgs == null && bPkgs == null);
            }
        }
        return (aLocation == null && bLocation == null);
    }

    @Override
    protected Response doUpdate(final SecurityContext securityContext, final UriInfo uriInfo, final String key, final OnmsMonitoringLocation targetObject) {
        final boolean sendEvent = !comparePollingPackageNames(m_dao.get(key), targetObject);

        if (!key.equals(targetObject.getLocationName())) {
            throw getException(Status.BAD_REQUEST, "The ID of the object doesn't match the ID of the path: {} != {}", targetObject.getLocationName(), key);
        }

        m_dao.clear();

        getDao().saveOrUpdate(targetObject);

        if (sendEvent) {
            final EventBuilder eventBuilder = new EventBuilder(EventConstants.POLLER_PACKAGE_LOCATION_ASSOCIATION_CHANGED_EVENT_UEI, "ReST");
            eventBuilder.addParam(EventConstants.PARM_LOCATION, targetObject.getLocationName());
            try {
                m_eventProxy.send(eventBuilder.getEvent());
            } catch (final EventProxyException e) {
                LOG.warn("Failed to send Event on polling package modification " + e.getMessage(), e);
            }
        }

        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsMonitoringLocation location) {
        final boolean sendEvent = location.getPollingPackageNames() != null && !location.getPollingPackageNames().isEmpty();

        getDao().delete(location);

        if (sendEvent) {
            final EventBuilder eventBuilder = new EventBuilder(EventConstants.POLLER_PACKAGE_LOCATION_ASSOCIATION_CHANGED_EVENT_UEI, "ReST");
            eventBuilder.addParam(EventConstants.PARM_LOCATION, location.getLocationName());
            try {
                m_eventProxy.send(eventBuilder.getEvent());
            } catch (final EventProxyException e) {
                LOG.warn("Failed to send Event on deletion of location " + e.getMessage(), e);
            }
        }
    }
}
