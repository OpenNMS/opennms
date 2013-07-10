/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.accesspointmonitor.rest;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.model.OnmsAccessPoint;
import org.opennms.netmgt.model.OnmsAccessPointCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

/**
 * <p>
 * REST service to the OpenNMS {@link OnmsAccessPoint} data.
 * </p>
 * 
 * <pre>
 * curl -v -X GET -u admin:admin http://localhost:8980/opennms/rest/accesspoints
 * </pre>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
@Component
@PerRequest
@Scope("prototype")
@Path("accesspoints")
public class AccessPointRestService {

    @Autowired
    private AccessPointDao m_accessPointDao;

    @Context
    private UriInfo m_uriInfo;

    @Context
    private SecurityContext m_securityContext;

    @Context
    private ServletContext m_servletContext;

    /**
     * <p>
     * getAccessPoint
     * </p>
     * 
     * @param accessPointId
     *            a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAccessPoint} object.
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("{accessPointId}")
    @Transactional
    public OnmsAccessPoint getOutage(@PathParam("accessPointId")
    final String accessPointId) {
        readLock();
        try {
            return m_accessPointDao.get(accessPointId);
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>
     * getCount
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    public String getCount() {
        readLock();
        try {
            return Integer.toString(m_accessPointDao.countAll());
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>
     * getOutages
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.model.OnmsOutageCollection} object.
     */
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Transactional
    public OnmsAccessPointCollection getAccessPoints() {
        readLock();
        try {
            final CriteriaBuilder builder = new CriteriaBuilder(OnmsAccessPoint.class);
            // TODO: Fix query filters - these don't seem to work outside of
            // the opennms-webapp project
            // applyQueryFilters(m_uriInfo.getQueryParameters(), builder);

            final OnmsAccessPointCollection coll = new OnmsAccessPointCollection(m_accessPointDao.findAll());

            // For getting totalCount
            coll.setTotalCount(m_accessPointDao.countMatching(builder.count().toCriteria()));

            return coll;
        } finally {
            readUnlock();
        }
    }

    private final ReentrantReadWriteLock m_globalLock = new ReentrantReadWriteLock();

    private final Lock m_readLock = m_globalLock.readLock();

    private final Lock m_writeLock = m_globalLock.writeLock();

    protected void readLock() {
        m_readLock.lock();
    }

    protected void readUnlock() {
        if (m_globalLock.getReadHoldCount() > 0) {
            m_readLock.unlock();
        }
    }

    protected void writeLock() {
        if (m_globalLock.getWriteHoldCount() == 0) {
            while (m_globalLock.getReadHoldCount() > 0) {
                m_readLock.unlock();
            }
            m_writeLock.lock();
        }
    }

    protected void writeUnlock() {
        if (m_globalLock.getWriteHoldCount() > 0) {
            m_writeLock.unlock();
        }
    }
}
