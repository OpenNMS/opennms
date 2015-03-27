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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAcknowledgmentCollection;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Path("acks")

/**
 * ReST service for Acknowledgments of alarms/notifications.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class AcknowledgmentRestService extends OnmsRestService {
    @Autowired
    private AcknowledgmentDao m_ackDao;
    
    @Autowired
    private AlarmDao m_alarmDao;
    
    @Autowired
    private NotificationDao m_notificationDao;
    
    @Context 
    UriInfo m_uriInfo;

    @Context
    SecurityContext m_securityContext;
    
    /**
     * <p>getAcknowledgment</p>
     *
     * @param alarmId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAcknowledgment} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{id}")
    @Transactional
    public OnmsAcknowledgment getAcknowledgment(@PathParam("id") String alarmId) {
        readLock();
        try {
            return m_ackDao.get(new Integer(alarmId));
        } finally {
            readUnlock();
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
        readLock();
        try {
            return Integer.toString(m_ackDao.countAll());
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>getAcks</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsAcknowledgmentCollection} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    public OnmsAcknowledgmentCollection getAcks() {
        readLock();
        
        try {
            final CriteriaBuilder builder = getQueryFilters(m_uriInfo.getQueryParameters());
            OnmsAcknowledgmentCollection coll = new OnmsAcknowledgmentCollection(m_ackDao.findMatching(builder.toCriteria()));
    
            //For getting totalCount
            builder.clearOrder();
            builder.limit(null);
            builder.offset(null);
            coll.setTotalCount(m_ackDao.countMatching(builder.toCriteria()));
    
            return coll;
        } finally {
            readUnlock();
        }
    }

    /**
     * <p>acknowledgeAlarm</p>
     *
     * @param alarmId a {@link java.lang.String} object.
     * @param action a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAcknowledgment} object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public OnmsAcknowledgment acknowledge(MultivaluedMap<String, String> formParams) {
    	writeLock();
    	
    	try {
	        String alarmId = formParams.getFirst("alarmId");
	        String notifId = formParams.getFirst("notifId");
	        String action = formParams.getFirst("action");
	        if (action == null) {
	            action = "ack";
	        }
	    	OnmsAcknowledgment ack = null;
	    	if (alarmId == null && notifId == null) {
	    		throw new IllegalArgumentException("You must supply either an alarmId or notifId!");
	    	} else if (alarmId != null && notifId != null) {
	    		throw new IllegalArgumentException("You cannot supply both an alarmId and a notifId!");
	    	} else if (alarmId != null) {
	    		final OnmsAlarm alarm = m_alarmDao.get(Integer.valueOf(alarmId));
	            ack = new OnmsAcknowledgment(alarm);
	    	} else if (notifId != null) {
	    		final OnmsNotification notification = m_notificationDao.get(Integer.valueOf(notifId));
	    		ack = new OnmsAcknowledgment(notification);
	    	}
	        
	        if ("ack".equals(action)) {
	            ack.setAckAction(AckAction.ACKNOWLEDGE);
	        } else if ("unack".equals(action)) {
	            ack.setAckAction(AckAction.UNACKNOWLEDGE);
	        } else if ("clear".equals(action)) {
	            ack.setAckAction(AckAction.CLEAR);
	        } else if ("esc".equals(action)) {
	            ack.setAckAction(AckAction.ESCALATE);
	        } else {
	            throw new IllegalArgumentException(
	            "Must supply the 'action' parameter, set to either 'ack, 'unack', 'clear', or 'esc'");
	        }

	        m_ackDao.processAck(ack);
	        return ack;
    	} finally {
    		writeUnlock();
    	}
    }

    private CriteriaBuilder getQueryFilters(MultivaluedMap<String,String> params) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsAcknowledgment.class);
        applyQueryFilters(params, builder);
        builder.orderBy("ackTime").desc();
        return builder;
    }
}
