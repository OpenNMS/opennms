/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

import java.text.ParseException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.joda.time.Duration;
import org.opennms.netmgt.config.monitoringLocations.LocationDef;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.provision.persist.StringIntervalPropertyEditor;
import org.opennms.web.rest.support.OnmsMonitoringLocationDefinitionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component("monitoringLocationsRestService")
@PerRequest
@Scope("prototype")
@Path("monitoringLocations")
public class MonitoringLocationsRestService extends OnmsRestService {

	private static final Logger LOG = LoggerFactory.getLogger(MonitoringLocationsRestService.class);

	@Autowired
	private LocationMonitorDao m_locationMonitorDao;

	@Context
	HttpHeaders m_headers;

	@Context
	SecurityContext m_securityContext;

	@GET
	@Path("default")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
	public LocationDef getDefaultMonitoringLocation() throws ParseException {
		readLock();
		try {
			return m_locationMonitorDao.findAllMonitoringLocationDefinitions().get(0);
		} finally {
			readUnlock();
		}
	}

	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
	public OnmsMonitoringLocationDefinitionList getForeignSources() throws ParseException {
		readLock();

		try {
			return new OnmsMonitoringLocationDefinitionList(m_locationMonitorDao.findAllMonitoringLocationDefinitions());
		} finally {
			readUnlock();
		}
	}

	@GET
	@Path("count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTotalCount() throws ParseException {
		readLock();
		try {
			return Integer.toString(m_locationMonitorDao.findAllMonitoringLocationDefinitions().size());
		} finally {
			readUnlock();
		}
	}

	@GET
	@Path("{monitoringLocation}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
	public LocationDef getMonitoringLocation(@PathParam("monitoringLocation") String monitoringLocation) {
		readLock();
		try {
			return m_locationMonitorDao.findMonitoringLocationDefinition(monitoringLocation);
		} finally {
			readUnlock();
		}
	}

	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
	@Transactional
	public Response addMonitoringLocation(@Context UriInfo uriInfo, LocationDef monitoringLocation) {
		writeLock();
		try {
			LOG.debug("addMonitoringLocation: Adding monitoringLocation {}", monitoringLocation.getLocationName());
			m_locationMonitorDao.saveMonitoringLocationDefinition(monitoringLocation);
			return Response.seeOther(getRedirectUri(uriInfo, monitoringLocation.getLocationName())).build();
		} finally {
			writeUnlock();
		}
	}

	@PUT
	@Path("{monitoringLocation}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response updateMonitoringLocation(@Context UriInfo uriInfo, @PathParam("monitoringLocation") String monitoringLocation, MultivaluedMapImpl params) {
		writeLock();
		try {
			LocationDef def = m_locationMonitorDao.findMonitoringLocationDefinition(monitoringLocation);
			LOG.debug("updateMonitoringLocation: updating monitoring location {}", monitoringLocation);

			if (params.isEmpty()) return Response.seeOther(getRedirectUri(uriInfo)).build();

			final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(def);
			wrapper.registerCustomEditor(Duration.class, new StringIntervalPropertyEditor());
			for(final String key : params.keySet()) {
				if (wrapper.isWritableProperty(key)) {
					Object value = null;
					String stringValue = params.getFirst(key);
					value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
					wrapper.setPropertyValue(key, value);
				}
			}
			LOG.debug("updateMonitoringLocation: monitoring location {} updated", monitoringLocation);
			m_locationMonitorDao.saveMonitoringLocationDefinition(def);
			return Response.seeOther(getRedirectUri(uriInfo)).build();
		} finally {
			writeUnlock();
		}
	}

	@DELETE
	@Path("{monitoringLocation}")
	@Transactional
	public Response deleteMonitoringLocation(@PathParam("monitoringLocation") String monitoringLocation) {
		writeLock();
		try {
			LOG.debug("deleteMonitoringLocation: deleting monitoring location {}", monitoringLocation);
			m_locationMonitorDao.deleteMonitoringLocationDefinition(monitoringLocation);
			return Response.ok().build();
		} finally {
			writeUnlock();
		}
	}
}
