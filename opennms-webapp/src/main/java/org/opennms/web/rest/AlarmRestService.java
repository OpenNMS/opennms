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

package org.opennms.web.rest;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAlarmCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
/**
 * <p>AlarmRestService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@PerRequest
@Scope("prototype")
@Path("alarms")
public class AlarmRestService extends AlarmRestServiceBase {

    @Autowired
    private AlarmDao m_alarmDao;
    
    @Context 
    UriInfo m_uriInfo;

    @Context
    SecurityContext m_securityContext;
    
    /**
     * <p>getAlarm</p>
     *
     * @param alarmId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAlarm} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{alarmId}")
    @Transactional
    public OnmsAlarm getAlarm(@PathParam("alarmId") final String alarmId) {
        getReadLock().lock();
        try {
            return m_alarmDao.get(new Integer(alarmId));
        } finally {
            getReadLock().unlock();
        }
    }
    
    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    public String getCount() {
        getReadLock().lock();
        try {
            return Integer.toString(m_alarmDao.countAll());
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>getAlarms</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsAlarmCollection} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    public OnmsAlarmCollection getAlarms() {
        getReadLock().lock();
        
        try {
            final CriteriaBuilder builder = getCriteriaBuilder(m_uriInfo.getQueryParameters(), false);
            builder.distinct();
            final OnmsAlarmCollection coll = new OnmsAlarmCollection(m_alarmDao.findMatching(builder.toCriteria()));
    
            //For getting totalCount
            coll.setTotalCount(m_alarmDao.countMatching(builder.clearOrder().limit(0).offset(0).toCriteria()));
    
            return coll;
        } finally {
            getReadLock().unlock();
        }
    }
    
    /**
     * <p>updateAlarm</p>
     *
     * @param alarmId a {@link java.lang.String} object.
     * @param ack a {@link java.lang.Boolean} object.
     */
    @PUT
	@Path("{alarmId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public void updateAlarm(@PathParam("alarmId") final String alarmId, @FormParam("ack") final Boolean ack) {
        getWriteLock().lock();
        
        try {
        	final OnmsAlarm alarm = m_alarmDao.get(new Integer(alarmId));
    		if (ack == null) {
    			throw new IllegalArgumentException("Must supply the 'ack' parameter, set to either 'true' or 'false'");
    		}
    		processAlarmAck(alarm, ack);
        } finally {
            getWriteLock().unlock();
        }
	}

	/**
	 * <p>updateAlarms</p>
	 *
	 * @param formProperties a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
	 */
	@PUT
	@Transactional
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void updateAlarms(final MultivaluedMapImpl formProperties) {
	    getWriteLock().lock();
	    
	    try {
    		Boolean ack=false;
    		if(formProperties.containsKey("ack")) {
    			ack="true".equals(formProperties.getFirst("ack"));
    			formProperties.remove("ack");
    		}
    		
    		final CriteriaBuilder builder = getCriteriaBuilder(formProperties, false);
    		builder.distinct();
    		builder.limit(0);
    		builder.offset(0);
    		for (final OnmsAlarm alarm : m_alarmDao.findMatching(builder.toCriteria())) {
    			processAlarmAck(alarm, ack);
    		}
	    } finally {
	        getWriteLock().unlock();
	    }
	}

	private void processAlarmAck(final OnmsAlarm alarm, final Boolean ack) {
		if (ack) {
			alarm.setAlarmAckTime(new Date());
			alarm.setAlarmAckUser(m_securityContext.getUserPrincipal().getName());
		} else {
			alarm.setAlarmAckTime(null);
			alarm.setAlarmAckUser(null);
		}
		m_alarmDao.save(alarm);
	}

}
