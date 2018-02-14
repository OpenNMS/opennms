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

package org.opennms.web.rest.v1;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.joda.time.Duration;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.provision.persist.StringIntervalPropertyEditor;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v1.support.OnmsMonitoringLocationDefinitionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("monitoringLocationsRestService")
@Path("monitoringLocations")
public class MonitoringLocationsRestService extends OnmsRestService {

	private static final Logger LOG = LoggerFactory.getLogger(MonitoringLocationsRestService.class);

	@Autowired
	private MonitoringLocationDao m_monitoringLocationDao;

	@GET
	@Path("default")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
	public OnmsMonitoringLocation getDefaultMonitoringLocation() throws ParseException {
		return m_monitoringLocationDao.findAll().get(0);
	}

	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
	public OnmsMonitoringLocationDefinitionList getForeignSources() throws ParseException {
		return new OnmsMonitoringLocationDefinitionList(m_monitoringLocationDao.findAll());
	}

	@GET
	@Path("count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTotalCount() throws ParseException {
		return Integer.toString(m_monitoringLocationDao.findAll().size());
	}

	@GET
	@Path("{monitoringLocation}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
	public OnmsMonitoringLocation getMonitoringLocation(@PathParam("monitoringLocation") String monitoringLocation) {
	    final OnmsMonitoringLocation loc = m_monitoringLocationDao.get(monitoringLocation);
            if (loc == null) {
                throw getException(Status.NOT_FOUND, "Monitoring location {} was not found.", monitoringLocation);
            }
            return loc;
	}

	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
	@Transactional
	public Response addMonitoringLocation(@Context final UriInfo uriInfo, OnmsMonitoringLocation monitoringLocation) {
		writeLock();
		try {
			LOG.debug("addMonitoringLocation: Adding monitoringLocation {}", monitoringLocation.getLocationName());
			m_monitoringLocationDao.save(monitoringLocation);
			return Response.created(getRedirectUri(uriInfo, monitoringLocation.getLocationName())).build();
		} finally {
			writeUnlock();
		}
	}

	@PUT
	@Path("{monitoringLocation}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response updateMonitoringLocation(@PathParam("monitoringLocation") String monitoringLocation, MultivaluedMapImpl params) {
		writeLock();
		try {
			OnmsMonitoringLocation def = m_monitoringLocationDao.get(monitoringLocation);
			LOG.debug("updateMonitoringLocation: updating monitoring location {}", monitoringLocation);

			if (params.isEmpty()) return Response.notModified().build();

			boolean modified = false;
			final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(def);
			wrapper.registerCustomEditor(Duration.class, new StringIntervalPropertyEditor());
			for(final String key : params.keySet()) {
				if (wrapper.isWritableProperty(key)) {
					Object value = null;
					String stringValue = params.getFirst(key);
					value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
					wrapper.setPropertyValue(key, value);
					modified = true;
				}
			}
			if (modified) {
			    LOG.debug("updateMonitoringLocation: monitoring location {} updated", monitoringLocation);
			    m_monitoringLocationDao.save(def);
			    return Response.noContent().build();
			}
			return Response.notModified().build();
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
			m_monitoringLocationDao.delete(monitoringLocation);
			return Response.noContent().build();
		} finally {
			writeUnlock();
		}
	}
}
