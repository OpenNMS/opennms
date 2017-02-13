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

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsMinionCollection;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsMinion} entity
 *
 * @author Seth
 */
@Component
@Path("minions")
@Transactional
public class MinionRestService extends AbstractDaoRestService<OnmsMinion,String> {

    private static final Logger LOG = LoggerFactory.getLogger(MinionRestService.class);

    @Autowired
    private MinionDao m_dao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    protected MinionDao getDao() {
        return m_dao;
    }

	protected Class<OnmsMinion> getDaoClass() {
		return OnmsMinion.class;
	}

	protected CriteriaBuilder getCriteriaBuilder() {
		final CriteriaBuilder builder = new CriteriaBuilder(OnmsMinion.class);

		// Order by label by default
		builder.orderBy("label").desc();

		return builder;
	}

	@Override
	protected JaxbListWrapper<OnmsMinion> createListWrapper(Collection<OnmsMinion> list) {
		return new OnmsMinionCollection(list);
	}

    @DELETE
    @Path("{id}")
    @Override
    public Response delete(@PathParam("id") final String id) {

        final OnmsMinion minion = getDao().get(id);
        final String location = minion.getLocation();
        Response response = super.delete(id);

        final EventBuilder eventBuilder = new EventBuilder(EventConstants.MONITORING_SYSTEM_DELETED_UEI,
                "OpenNMS.WebUI");
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_TYPE, OnmsMonitoringSystem.TYPE_MINION);
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_ID, id);
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_LOCATION, location);
        try {
            m_eventProxy.send(eventBuilder.getEvent());
        } catch (final EventProxyException e) {
            LOG.warn(" Failed to send Event on Minion deletion " + e.getMessage(), e);
        }

        return response;
    }
}
