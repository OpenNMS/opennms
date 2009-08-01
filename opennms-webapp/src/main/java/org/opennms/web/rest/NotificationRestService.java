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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.dao.NotificationDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsNotificationCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Scope("prototype")
@Path("notifications")
public class NotificationRestService extends OnmsRestService {
    @Autowired
    private NotificationDao m_notifDao;
    
    @Context 
    UriInfo m_uriInfo;

    @Context
    SecurityContext m_securityContext;
    
    @GET
    @Produces("text/xml")
    @Path("{notifId}")
    @Transactional
    public OnmsNotification getNotification(@PathParam("eventId") String notifId) {
    	OnmsNotification result= m_notifDao.get(new Integer(notifId));
    	return result;
    }
    
    @GET
    @Produces("text/plain")
    @Path("count")
    @Transactional
    public String getCount() {
    	return Integer.toString(m_notifDao.countAll());
    }

    @GET
    @Produces("text/xml")
    @Transactional
    public OnmsNotificationCollection getNotifications() {
    	MultivaluedMap<java.lang.String,java.lang.String> params=m_uriInfo.getQueryParameters();
		OnmsCriteria criteria=new OnmsCriteria(OnmsNotification.class);

    	setLimitOffset(params, criteria, 10);
    	addOrdering(params, criteria);
    	addFiltersToCriteria(params, criteria, OnmsNotification.class);

        return new OnmsNotificationCollection(m_notifDao.findMatching(getDistinctIdCriteria(OnmsNotification.class,criteria)));
    }
    
    @PUT
    @Path("{notifId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
    public void updateNotification(@PathParam("notifId") String notifId, @FormParam("ack") Boolean ack) {
    	OnmsNotification notif=m_notifDao.get(new Integer(notifId));
    	if(ack==null) {
    		throw new  IllegalArgumentException("Must supply the 'ack' parameter, set to either 'true' or 'false'");
    	}
       	processNotifAck(notif,ack);
    }
    
	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public void updateNotifications(MultivaluedMapImpl formProperties) {

		Boolean ack=false;
		if(formProperties.containsKey("ack")) {
			ack="true".equals(formProperties.getFirst("ack"));
			formProperties.remove("ack");
		}
		
		OnmsCriteria criteria = new OnmsCriteria(OnmsNotification.class);
		setLimitOffset(formProperties, criteria, 10);
		addFiltersToCriteria(formProperties, criteria, OnmsNotification.class);

		
		for (OnmsNotification notif : m_notifDao.findMatching(criteria)) {
			processNotifAck(notif, ack);
		}
	}


	private void processNotifAck( OnmsNotification notif, Boolean ack) {
		if(ack) {
       		notif.setRespondTime(new Date());
       		notif.setAnsweredBy(m_securityContext.getUserPrincipal().getName());
    	} else {
    		notif.setRespondTime(null);
    		notif.setAnsweredBy(null);
    	}
       	m_notifDao.save(notif);
	}
}

