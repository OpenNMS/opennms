/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ncs.rest;

import java.util.Collection;
import java.util.LinkedList;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.ncs.persistence.NCSComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.sun.jersey.spi.resource.PerRequest;

/**
 * Basic Web Service using REST for NCS Components
 *
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 */
@Component
@PerRequest
@Scope("prototype")
@Path("NCS")
@Transactional
public class NCSRestService {
	
	@Autowired
	NCSComponentService m_componentService;

	@Autowired
	EventDao m_eventDao;
	
	@Autowired
	AlarmDao m_alarmDao;
	
    @Context 
    UriInfo m_uriInfo;
    
    public void afterPropertiesSet() throws RuntimeException {
    	Assert.notNull(m_componentService);
    	Assert.notNull(m_eventDao);
    	Assert.notNull(m_alarmDao);
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
	    	LogUtils.debugf(this, "getComponent: type = %s, foreignSource = %s, foreignId = %s", type, foreignSource, foreignId);
	
	    	if (m_componentService == null) {
	    		throw new IllegalStateException("component service is null");
	    	}

	    	final NCSComponent component = m_componentService.getComponent(type, foreignSource, foreignId);
	    	if (component == null) throw new WebApplicationException(Status.BAD_REQUEST);
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
	    		throw new IllegalStateException("component service is null");
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
			LogUtils.debugf(this, "addComponents: Adding component %s (deleteOrphans=%s)", component, Boolean.valueOf(deleteOrphans));

	    	if (m_componentService == null) {
	    		throw new IllegalStateException("component service is null");
	    	}

	    	try {
	        	m_componentService.addOrUpdateComponents(component, deleteOrphans);
	    	} catch (final DataAccessException e) {
	    		throw new WebApplicationException(e, Status.BAD_REQUEST);
	    	}
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
	    	LogUtils.debugf(this, "addComponent: type = %s, foreignSource = %s, foreignId = %s (deleteOrphans=%s)", type, foreignSource, foreignId, Boolean.valueOf(deleteOrphans));
	
	    	if (m_componentService == null) {
	    		throw new IllegalStateException("component service is null");
	    	}
	    	
	    	if (subComponent == null) {
	    		throw new WebApplicationException(Status.BAD_REQUEST);
	    	}
	
	    	try {
	    		return m_componentService.addSubcomponent(type, foreignSource, foreignId, subComponent, deleteOrphans);
	    	} catch (final DataAccessException e) {
	    		throw new WebApplicationException(e, Status.BAD_REQUEST);
	    	}
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
	        LogUtils.infof(this, "deleteComponent: Deleting component of type %s and foreignIdentity %s:%s (deleteOrphans=%s)", type, foreignSource, foreignId, Boolean.valueOf(deleteOrphans));
	
	    	if (m_componentService == null) {
	    		throw new IllegalStateException("component service is null");
	    	}

	    	m_componentService.deleteComponent(type, foreignSource, foreignId, deleteOrphans);
	        return Response.ok().build();
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

	@XmlRootElement(name = "components")
	public static class ComponentList extends LinkedList<NCSComponent> {

	    private static final long serialVersionUID = 8031737923157780179L;
	    private int m_totalCount;

	    /**
	     * <p>Constructor for OnmsNodeList.</p>
	     */
	    public ComponentList() {
	        super();
	    }

	    /**
	     * <p>Constructor for OnmsNodeList.</p>
	     *
	     * @param c a {@link java.util.Collection} object.
	     */
	    public ComponentList(Collection<? extends NCSComponent> c) {
	        super(c);
	    }

	    /**
	     * <p>getNodes</p>
	     *
	     * @return a {@link java.util.List} object.
	     */
	    @XmlElement(name = "component")
	    public List<NCSComponent> getComponents() {
	        return this;
	    }

	    /**
	     * <p>setNodes</p>
	     *
	     * @param components a {@link java.util.List} object.
	     */
	    public void setComponents(List<NCSComponent> components) {
	        if (components == this) return;
	        clear();
	        addAll(components);
	    }

	    /**
	     * <p>getCount</p>
	     *
	     * @return a {@link java.lang.Integer} object.
	     */
	    @XmlAttribute(name="count")
	    public int getCount() {
	        return this.size();
	    }

	    // The property has a getter "" but no setter. For unmarshalling, please define setters.
	    public void setCount(final int count) {
	    }

	    /**
	     * <p>getTotalCount</p>
	     *
	     * @return a int.
	     */
	    @XmlAttribute(name="totalCount")
	    public int getTotalCount() {
	        return m_totalCount;
	    }
	    
	    /**
	     * <p>setTotalCount</p>
	     *
	     * @param count a int.
	     */
	    public void setTotalCount(int count) {
	        m_totalCount = count;
	    }
	}
	
}
