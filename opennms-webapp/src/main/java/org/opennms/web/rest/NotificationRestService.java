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

import org.hibernate.criterion.Order;
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
/**
 * <p>NotificationRestService class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
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
    
    /**
     * <p>getNotification</p>
     *
     * @param notifId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsNotification} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{notifId}")
    @Transactional
    public OnmsNotification getNotification(@PathParam("eventId") String notifId) {
    	OnmsNotification result= m_notifDao.get(new Integer(notifId));
    	return result;
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
    	return Integer.toString(m_notifDao.countAll());
    }

    /**
     * <p>getNotifications</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNotificationCollection} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Transactional
    public OnmsNotificationCollection getNotifications() {
        OnmsNotificationCollection coll = new OnmsNotificationCollection(m_notifDao.findMatching(getQueryFilters(m_uriInfo.getQueryParameters())));

        //For getting totalCount
        OnmsCriteria crit = new OnmsCriteria(OnmsNotification.class);
        addFiltersToCriteria(m_uriInfo.getQueryParameters(), crit, OnmsNotification.class);
        coll.setTotalCount(m_notifDao.countMatching(crit));

        return coll;
    }
    
    /**
     * <p>updateNotification</p>
     *
     * @param notifId a {@link java.lang.String} object.
     * @param ack a {@link java.lang.Boolean} object.
     */
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
    
	/**
	 * <p>updateNotifications</p>
	 *
	 * @param formProperties a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
	 */
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
		setLimitOffset(formProperties, criteria, DEFAULT_LIMIT, true);
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

    private OnmsCriteria getQueryFilters(MultivaluedMap<String,String> params) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsNotification.class);

        setLimitOffset(params, criteria, DEFAULT_LIMIT, false);
        addOrdering(params, criteria, false);
        // Set default ordering
        addOrdering(
            new MultivaluedMapImpl(
                new String[][] { 
                    new String[] { "orderBy", "notifyId" }, 
                    new String[] { "order", "desc" } 
                }
            ), criteria, false
        );
        addFiltersToCriteria(params, criteria, OnmsNotification.class);

        return getDistinctIdCriteria(OnmsNotification.class, criteria);
    }
}
