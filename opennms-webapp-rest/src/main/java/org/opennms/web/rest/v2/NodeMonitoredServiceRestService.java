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

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.support.CreateIfNecessaryTemplate;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoredServiceList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsIpInterface} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Transactional
public class NodeMonitoredServiceRestService extends AbstractNodeDependentRestService<OnmsMonitoredService,OnmsMonitoredService,Integer,String> {

    private static final Logger LOG = LoggerFactory.getLogger(NodeMonitoredServiceRestService.class);

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private MonitoredServicesComponent m_component;

    @Autowired
    private MonitoredServiceDao m_dao;

    @Override
    protected MonitoredServiceDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsMonitoredService> getDaoClass() {
        return OnmsMonitoredService.class;
    }

    @Override
    protected Class<OnmsMonitoredService> getQueryBeanClass() {
        return OnmsMonitoredService.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(final UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass());

        // 1st level JOINs
        builder.alias("ipInterface", Aliases.ipInterface.toString(), JoinType.LEFT_JOIN);
        builder.alias("serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias("ipInterface.node", Aliases.node.toString(), JoinType.LEFT_JOIN);
        builder.alias("ipInterface.snmpInterface", Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN);

        // 3rd level JOINs
        builder.alias("node.assetRecord", Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        // TODO: Only add this alias when filtering by category so that we can specify a join condition
        builder.alias("node.categories", Aliases.category.toString(), JoinType.LEFT_JOIN);
        builder.alias("node.location", Aliases.location.toString(), JoinType.LEFT_JOIN);

        builder.orderBy("id");

        updateCriteria(uriInfo, builder);

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsMonitoredService> createListWrapper(Collection<OnmsMonitoredService> list) {
        return new OnmsMonitoredServiceList(list);
    }

    @Override
    protected Response doCreate(SecurityContext securityContext, UriInfo uriInfo, OnmsMonitoredService service) {
        final OnmsIpInterface iface = getInterface(uriInfo);
        if (iface == null) {
            throw getException(Status.BAD_REQUEST, "IP interface was not found");
        } else if (service == null) {
            throw getException(Status.BAD_REQUEST, "Service object cannot be null");
        } else if (service.getServiceType() == null || service.getServiceType().getName() == null) {
            throw getException(Status.BAD_REQUEST, "Service type names cannot be null");
        }
        service.setServiceType(getServiceType(service.getServiceName()));
        service.setIpInterface(iface);
        iface.addMonitoredService(service);
        getDao().save(service);

        final Event e = EventUtils.createNodeGainedServiceEvent("ReST", iface.getNode().getId(), iface.getIpAddress(), service.getServiceName(), iface.getNode().getLabel(),
                                                                iface.getNode().getLabelSource(), iface.getNode().getSysName(), iface.getNode().getSysDescription());
        sendEvent(e);
        return Response.created(RedirectHelper.getRedirectUri(uriInfo, service.getServiceName())).build();
    }

    @Override
    protected void updateCriteria(final UriInfo uriInfo, final CriteriaBuilder builder) {
        super.updateCriteria(uriInfo, builder);
        List<PathSegment> segments = uriInfo.getPathSegments(true);
        final String ipAddress =  segments.get(3).getPath(); // /nodes/{criteria}/ipinterfaces/{ipAddress}
        builder.eq("ipInterface.ipAddress", ipAddress);
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, OnmsMonitoredService targetObject, MultivaluedMapImpl params) {
        final String previousStatus = targetObject.getStatus();
        RestUtils.setBeanProperties(targetObject, params);
        getDao().update(targetObject);
        boolean changed = m_component.hasStatusChanged(previousStatus, targetObject);
        return changed ? Response.noContent().build() : Response.notModified().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsMonitoredService svc) {
        svc.getIpInterface().getMonitoredServices().remove(svc);
        getDao().delete(svc);
        final Event e = EventUtils.createDeleteServiceEvent("ReST", svc.getNodeId(), svc.getIpAddress().getHostAddress(), svc.getServiceName(), -1L);
        sendEvent(e);
    }

    @Override
    protected OnmsMonitoredService doGet(UriInfo uriInfo, String serviceName) {
        final OnmsIpInterface iface = getInterface(uriInfo);
        return iface == null ? null : iface.getMonitoredServiceByServiceType(serviceName);
    }

    private OnmsServiceType getServiceType(final String serviceName) {
        final OnmsServiceType serviceType = new CreateIfNecessaryTemplate<OnmsServiceType, ServiceTypeDao>(m_transactionManager, m_serviceTypeDao) {
            @Override
            protected OnmsServiceType query() {
                return m_dao.findByName(serviceName);
            }
            @Override
            protected OnmsServiceType doInsert() {
                LOG.info("getServiceType: creating service type {}", serviceName);
                final OnmsServiceType s = new OnmsServiceType(serviceName);
                m_dao.saveOrUpdate(s);
                return s;
            }
        }.execute();
        return serviceType;
    }

    private OnmsIpInterface getInterface(final UriInfo uriInfo) {
        final OnmsNode node = getNode(uriInfo);
        final String ipAddress =  uriInfo.getPathSegments(true).get(3).getPath();
        return node == null ? null : node.getIpInterfaceByIpAddress(ipAddress);
    }

}
