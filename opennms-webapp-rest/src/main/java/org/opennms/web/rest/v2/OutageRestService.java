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

import javax.ws.rs.Path;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsOutageCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsOutage} entity.
 *
 * @author Seth
 */
@Component
@Path("outages")
@Transactional
public class OutageRestService extends AbstractDaoRestService<OnmsOutage,Integer> {

	@Autowired
	private OutageDao m_dao;

	@Override
	protected OutageDao getDao() {
		return m_dao;
	}

	@Override
	protected Class<OnmsOutage> getDaoClass() {
		return OnmsOutage.class;
	}

	@Override
	protected CriteriaBuilder getCriteriaBuilder() {
		final CriteriaBuilder builder = new CriteriaBuilder(OnmsOutage.class);
		builder.alias("monitoredService", "monitoredService", JoinType.LEFT_JOIN);
		builder.alias("monitoredService.ipInterface", "ipInterface", JoinType.LEFT_JOIN);
		builder.alias("monitoredService.serviceType", "serviceType", JoinType.LEFT_JOIN);
		builder.alias("ipInterface.node", "node", JoinType.LEFT_JOIN);
		builder.alias("serviceLostEvent", "serviceLostEvent", JoinType.LEFT_JOIN);
		builder.alias("serviceRegainedEvent", "serviceRegainedEvent", JoinType.LEFT_JOIN); 

		// NOTE: Left joins on a toMany relationship need a join condition so that only one row is returned

		// Order by ID by default
		builder.orderBy("id").desc();

		return builder;
	}

	@Override
	protected JaxbListWrapper<OnmsOutage> createListWrapper(Collection<OnmsOutage> list) {
		return new OnmsOutageCollection(list);
	}
}
