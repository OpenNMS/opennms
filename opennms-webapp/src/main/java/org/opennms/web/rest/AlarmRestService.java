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
    
    @GET
    @Produces("text/xml")
    @Path("{alarmId}")
    @Transactional
    public OnmsAlarm getNotification(@PathParam("alarmId") String alarmId) {
    	OnmsAlarm result= m_alarmDao.get(new Integer(alarmId));
    	return result;
    }
    
    @GET
    @Produces("text/plain")
    @Path("count")
    @Transactional
    public String getCount() {
    	return Integer.toString(m_alarmDao.countAll());
    }

    @GET
    @Produces("text/xml")
    @Transactional
    public OnmsAlarmCollection getNotifications() {
    	MultivaluedMap<java.lang.String,java.lang.String> params=m_uriInfo.getQueryParameters();
		OnmsCriteria criteria=new OnmsCriteria(OnmsAlarm.class);

    	setLimitOffset(params, criteria);
        addOrdering(params, criteria, false);
    	addFiltersToCriteria(params, criteria, OnmsAlarm.class);
    	criteria.setFetchMode("firstEvent", FetchMode.JOIN);
        criteria.setFetchMode("lastEvent", FetchMode.JOIN);
        return new OnmsAlarmCollection(m_alarmDao.findMatching(getDistinctIdCriteria(OnmsAlarm.class, criteria)));
    }
    
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

	@PUT
	@Transactional
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void updateAlarms(MultivaluedMapImpl formProperties) {

		Boolean ack=false;
		if(formProperties.containsKey("ack")) {
			ack="true".equals(formProperties.getFirst("ack"));
			formProperties.remove("ack");
		}
		OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class);
		setLimitOffset(formProperties, criteria, 10);
		addFiltersToCriteria(formProperties, criteria, OnmsAlarm.class);
		for (OnmsAlarm alarm : m_alarmDao.findMatching(criteria)) {
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
}

