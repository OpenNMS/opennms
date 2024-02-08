/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.rest.v1;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.joda.time.Duration;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.provision.persist.StringIntervalPropertyEditor;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.v1.support.OnmsMonitoringLocationDefinitionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("monitoringLocationsRestService")
@Path("monitoringLocations")
@Tag(name = "MonitoringLocations", description = "Monitoring Locations API")
public class MonitoringLocationsRestService extends OnmsRestService {

	private static final Logger LOG = LoggerFactory.getLogger(MonitoringLocationsRestService.class);
	private static final String POLLING_PACKAGE_NAMES = "pollingPackageNames";

	@Autowired
	@Qualifier("eventProxy")
	private EventProxy m_eventProxy;

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
		final List<OnmsMonitoringLocation> onmsMonitoringLocationList = m_monitoringLocationDao.findAll();
		Collections.sort(onmsMonitoringLocationList, Comparator.comparing(OnmsMonitoringLocation::getLocationName));
		return new OnmsMonitoringLocationDefinitionList(onmsMonitoringLocationList);
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
			boolean sendEvent = false;

			OnmsMonitoringLocation def = m_monitoringLocationDao.get(monitoringLocation);
			LOG.debug("updateMonitoringLocation: updating monitoring location {}", monitoringLocation);

			if (params.isEmpty()) return Response.notModified().build();

			boolean modified = false;
			final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(def);
			wrapper.registerCustomEditor(Duration.class, new StringIntervalPropertyEditor());
			for(final String key : params.keySet()) {
				if (wrapper.isWritableProperty(key)) {
					String stringValue = params.getFirst(key);
					Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
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
