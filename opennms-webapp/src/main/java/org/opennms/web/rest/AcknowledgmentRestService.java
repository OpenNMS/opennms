/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 16, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.dao.AcknowledgmentDao;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAcknowledgmentCollection;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.acknowledgments.AckService;
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
 */
public class AcknowledgmentRestService extends OnmsRestService {
    @Autowired
    private AcknowledgmentDao m_ackDao;
    
    @Autowired
    private AlarmDao m_alarmDao;
    
    @Autowired
    private AckService m_ackSvc;
    
    @Context 
    UriInfo m_uriInfo;

    @Context
    SecurityContext m_securityContext;
    
    @GET
    @Produces("text/xml")
    @Path("{id}")
    @Transactional
    public OnmsAcknowledgment getAcknowledgment(@PathParam("id") String alarmId) {
        OnmsAcknowledgment result = m_ackDao.get(new Integer(alarmId));
    	return result;
    }
    
    @GET
    @Produces("text/plain")
    @Path("count")
    @Transactional
    public String getCount() {
        return Integer.toString(m_ackDao.countAll());
    }

    @GET
    @Produces("text/xml")
    @Transactional
    public OnmsAcknowledgmentCollection getAcks() {
    	MultivaluedMap<java.lang.String,java.lang.String> params=m_uriInfo.getQueryParameters();
		OnmsCriteria criteria=new OnmsCriteria(OnmsAcknowledgment.class);

    	setLimitOffset(params, criteria);
    	addOrdering(params, criteria);
    	addFiltersToCriteria(params, criteria, OnmsAcknowledgment.class);

        return new OnmsAcknowledgmentCollection(m_ackDao.findMatching(getDistinctIdCriteria(OnmsAcknowledgment.class, criteria)));
    }

//    @PUT
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public OnmsAcknowledgment acknowledgeAlarm(@FormParam("alarmId") String alarmId, @FormParam("action") String action) {
        OnmsAlarm alarm = m_alarmDao.get(Integer.valueOf(alarmId));
        OnmsAcknowledgment ack = new OnmsAcknowledgment(alarm);
        
        if (action == null) {
            action = "ack";
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
        
        m_ackSvc.processAck(ack);
        return ack;
        
    }
    
}

