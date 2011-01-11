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

import org.hibernate.FetchMode;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAlarmCollection;
import org.opennms.netmgt.model.OnmsCriteria;
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
public class AlarmRestService extends OnmsRestService {

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
    public OnmsAlarm getAlarm(@PathParam("alarmId") String alarmId) {
    	OnmsAlarm result= m_alarmDao.get(new Integer(alarmId));
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
    	return Integer.toString(m_alarmDao.countAll());
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
        OnmsAlarmCollection coll = new OnmsAlarmCollection(m_alarmDao.findMatching(getQueryFilters(m_uriInfo.getQueryParameters())));

        //For getting totalCount
        OnmsCriteria crit = new OnmsCriteria(OnmsAlarm.class);
        addFiltersToCriteria(m_uriInfo.getQueryParameters(), crit, OnmsAlarm.class);
        coll.setTotalCount(m_alarmDao.countMatching(crit));

        return coll;
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
	public void updateAlarm(@PathParam("alarmId")
	String alarmId, @FormParam("ack")
	Boolean ack) {
		OnmsAlarm alarm = m_alarmDao.get(new Integer(alarmId));
		if (ack == null) {
			throw new IllegalArgumentException(
					"Must supply the 'ack' parameter, set to either 'true' or 'false'");
		}
		processAlarmAck(alarm, ack);
	}

	/**
	 * <p>updateAlarms</p>
	 *
	 * @param formProperties a {@link org.opennms.web.rest.MultivaluedMapImpl} object.
	 */
	@PUT
	@Transactional
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void updateAlarms(MultivaluedMapImpl formProperties) {

		Boolean ack=false;
		if(formProperties.containsKey("ack")) {
			ack="true".equals(formProperties.getFirst("ack"));
			formProperties.remove("ack");
		}
		for (OnmsAlarm alarm : m_alarmDao.findMatching(getQueryFilters(formProperties))) {
			processAlarmAck(alarm, ack);
		}
	}

	private void processAlarmAck(OnmsAlarm alarm, Boolean ack) {
		if (ack) {
			alarm.setAlarmAckTime(new Date());
			alarm.setAlarmAckUser(m_securityContext.getUserPrincipal().getName());
		} else {
			alarm.setAlarmAckTime(null);
			alarm.setAlarmAckUser(null);
		}
		m_alarmDao.save(alarm);
	}

	private OnmsCriteria getQueryFilters(MultivaluedMap<String,String> params) {
	    OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class);

	    setLimitOffset(params, criteria, DEFAULT_LIMIT, false);
	    addOrdering(params, criteria, false);
        // Set default ordering
        addOrdering(
            new MultivaluedMapImpl(
                new String[][] { 
                    new String[] { "orderBy", "lastEventTime" }, 
                    new String[] { "order", "desc" } 
                }
            ), criteria, false
        );
	    addFiltersToCriteria(params, criteria, OnmsAlarm.class);

	    criteria.setFetchMode("firstEvent", FetchMode.JOIN);
	    criteria.setFetchMode("lastEvent", FetchMode.JOIN);
	    return getDistinctIdCriteria(OnmsAlarm.class, criteria);
	}
}
