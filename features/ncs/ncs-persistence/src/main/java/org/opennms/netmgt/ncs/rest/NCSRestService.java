/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ncs.rest;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.ncs.persistence.NCSComponentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Basic Web Service using REST for NCS Components
 *
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 */
@Component("ncsRestService")
@Path("NCS")
@Transactional
public class NCSRestService {
    private static final Logger LOG = LoggerFactory.getLogger(NCSRestService.class);

    @Autowired
    NCSComponentService m_componentService;

    public void afterPropertiesSet() throws RuntimeException {
        Assert.notNull(m_componentService);
    }

    /**
     * <p>getNodes</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNodeList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{type}/{foreignSource}:{foreignId}")
    public NCSComponent getComponent(@PathParam("type") final String type, @PathParam("foreignSource") final String foreignSource, @PathParam("foreignId") final String foreignId) {
        afterPropertiesSet();
        readLock();
        try {
            LOG.debug("getComponent: type = {}, foreignSource = {}, foreignId = {}", type, foreignSource, foreignId);

            if (m_componentService == null) {
                throw getException(Status.INTERNAL_SERVER_ERROR, "Component service is null");
            }
            if (foreignSource == null) {
                throw getException(Status.BAD_REQUEST, "Foreign Source cannot be null");
            }
            if (foreignId == null) {
                throw getException(Status.BAD_REQUEST, "Foreign ID cannot be null");
            }

            final NCSComponent component = m_componentService.getComponent(type, foreignSource, foreignId);
            if (component == null) throw getException(Status.NOT_FOUND, "Component of type {} not found for {}:{}", type, foreignSource, foreignId);
            return component;
        } finally {
            readUnlock();
        }
    }

    @GET
    @Path("attributes")
    public ComponentList getComponentsByAttributes() {
        afterPropertiesSet();
        readLock();
        try {
            if (m_componentService == null) {
                throw getException(Status.INTERNAL_SERVER_ERROR, "Component service is null");
            }

            return m_componentService.findComponentsWithAttribute("jnxVpnPwVpnName", "ge-3/1/4.2");
        } finally {
            readUnlock();
        }

    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response addComponents(@QueryParam("deleteOrphans") final boolean deleteOrphans, final NCSComponent component) {
        afterPropertiesSet();
        writeLock();
        try {
            LOG.debug("addComponents: Adding component {} (deleteOrphans={})", component, Boolean.valueOf(deleteOrphans));

            if (m_componentService == null) {
                throw getException(Status.INTERNAL_SERVER_ERROR, "Component service is null");
            }
            if (component == null) {
                throw getException(Status.BAD_REQUEST, "Component cannot be null");
            }

            m_componentService.addOrUpdateComponents(component, deleteOrphans);
            return Response.ok(component).build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>getNodes</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNodeList} object.
     */
    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{type}/{foreignSource}:{foreignId}")
    public NCSComponent addComponent(@QueryParam("deleteOrphans") final boolean deleteOrphans, @PathParam("type") String type, @PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId, NCSComponent subComponent) {
        afterPropertiesSet();
        writeLock();
        try {
            LOG.debug("addComponent: type = {}, foreignSource = {}, foreignId = {} (deleteOrphans={})", type, foreignSource, foreignId, Boolean.valueOf(deleteOrphans));

            if (m_componentService == null) {
                throw getException(Status.INTERNAL_SERVER_ERROR, "Component service is null");
            }
            if (type == null) {
                throw getException(Status.BAD_REQUEST, "Type cannot be null");
            }
            if (foreignSource == null) {
                throw getException(Status.BAD_REQUEST, "Foreign Source cannot be null");
            }
            if (foreignId == null) {
                throw getException(Status.BAD_REQUEST, "Foreign ID cannot be null");
            }
            if (subComponent == null) {
                throw getException(Status.BAD_REQUEST, "Sub-component cannot be null");
            }

            return m_componentService.addSubcomponent(type, foreignSource, foreignId, subComponent, deleteOrphans);
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{type}/{foreignSource}:{foreignId}")
    public Response deleteComponent(@QueryParam("deleteOrphans") final boolean deleteOrphans, @PathParam("type") String type, @PathParam("foreignSource") String foreignSource, @PathParam("foreignId") String foreignId) {
        afterPropertiesSet();
        writeLock();

        try {
            LOG.info("deleteComponent: Deleting component of type {} and foreignIdentity {}:{} (deleteOrphans={})", type, foreignSource, foreignId, Boolean.valueOf(deleteOrphans));

            if (m_componentService == null) {
                throw getException(Status.INTERNAL_SERVER_ERROR, "Component service is null");
            }
            if (type == null) {
                throw getException(Status.BAD_REQUEST, "Type cannot be null");
            }
            if (foreignSource == null) {
                throw getException(Status.BAD_REQUEST, "Foreign Source cannot be null");
            }
            if (foreignId == null) {
                throw getException(Status.BAD_REQUEST, "Foreign ID cannot be null");
            }

            m_componentService.deleteComponent(type, foreignSource, foreignId, deleteOrphans);
            return Response.noContent().build();
        } finally {
            writeUnlock();
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

    @XmlRootElement(name="components")
    @JsonRootName("components")
    public static class ComponentList extends JaxbListWrapper<NCSComponent> {
        private static final long serialVersionUID = 1L;

        public ComponentList() { super(); }
        public ComponentList(final Collection<? extends NCSComponent> components) {
            super(components);
        }
        @XmlElement(name="component")
        @JsonProperty("component")
        public List<NCSComponent> getObjects() {
            return super.getObjects();
        }
    }

    protected static WebApplicationException getException(final Status status, String msg, Object... params) throws WebApplicationException {
        if (params != null) msg = MessageFormatter.arrayFormat(msg, params).getMessage();
        LOG.error(msg);
        return new WebApplicationException(Response.status(status).type(MediaType.TEXT_PLAIN).entity(msg).build());
    }

}
