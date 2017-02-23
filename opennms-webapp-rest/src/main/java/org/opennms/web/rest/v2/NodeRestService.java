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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNodeList;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsNode} entity
 *
 * @author Seth
 */
@Component
@Path("nodes")
@Transactional
public class NodeRestService extends AbstractDaoRestService<OnmsNode,Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(NodeRestService.class);

	@Autowired
	private MonitoringLocationDao m_locationDao;

	@Autowired
	private NodeDao m_dao;

	protected NodeDao getDao() {
		return m_dao;
	}

	protected Class<OnmsNode> getDaoClass() {
		return OnmsNode.class;
	}

	protected CriteriaBuilder getCriteriaBuilder() {
		final CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class);

		builder.alias("snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
		builder.alias("ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);
		builder.alias("categories", "category", JoinType.LEFT_JOIN);
		builder.alias("assetRecord", "assetRecord", JoinType.LEFT_JOIN);
		builder.alias("ipInterfaces.monitoredServices.serviceType", "serviceType", JoinType.LEFT_JOIN);

		// Order by label by default
		builder.orderBy("label").desc();

		return builder;
	}

	@Override
	protected JaxbListWrapper<OnmsNode> createListWrapper(Collection<OnmsNode> list) {
		return new OnmsNodeList(list);
	}

	@Override
	public Response doCreate(final UriInfo uriInfo, final OnmsNode object) {
		if (object.getLocation() == null) {
			OnmsMonitoringLocation location = m_locationDao.getDefaultLocation();
			LOG.debug("addNode: Assigning new node to default location: {}", location.getLocationName());
			object.setLocation(location);
		}
		return super.doCreate(uriInfo, object);
	}
}
