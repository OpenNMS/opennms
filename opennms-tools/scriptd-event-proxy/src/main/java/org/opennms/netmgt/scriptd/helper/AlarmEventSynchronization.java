package org.opennms.netmgt.scriptd.helper;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.Parameter;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parms;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public class AlarmEventSynchronization implements EventSynchronization {

	List<EventForwarder> m_forwarders = new ArrayList<EventForwarder>();
	ThreadCategory log;

	public AlarmEventSynchronization() {
		super();
        ThreadCategory.setPrefix("OpenNMS.InsProxy");
        log=ThreadCategory.getInstance(this.getClass());
        log.debug("InsAbstract Session Constructor: loaded");
    }

    public ThreadCategory getLog() {
        return log;
    }

	String m_criteriaRestriction = "EXISTS (select 1 from alarms where severity > 3 and alarms.alarmid = alarmid and eventtime = lasteventtime)";

	public void addEventForwarder(EventForwarder forwarder) {
		if (forwarder != null)
			m_forwarders.add(forwarder);
	}

	public void setCriteriaRestriction(String criteria) {
		m_criteriaRestriction=criteria;
	}

	public List<Event> getEvents() {
		return getEventsByCriteria();
	}

	public void sync() {
		for (EventForwarder forwarder: m_forwarders) {
			for (Event event: getEvents()) {
				forwarder.flushSyncEvent(event);
			}
		}
	}

	private Event getXMLEvent(OnmsEvent ev) {
        Event e = new Event();
        e.setDbid(ev.getId());

        //UEI
        if (ev.getEventUei() != null ) {
            e.setUei(ev.getEventUei());
        } else {
            return null;
        }

        // Source
        if (ev.getEventSource() != null ) {
            e.setSource(ev.getEventSource());
        } 

        //nodeid
        if (ev.getNode() != null) {
            e.setNodeid(ev.getNode().getId());
        }


        // timestamp
        if (ev.getEventTime() != null) {
            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            e.setTime(dateFormat.format(ev.getEventTime()));
        }
        
        // host
        if (ev.getEventHost() != null) {
            e.setHost(ev.getEventHost());
        }
        
        // interface
        if (ev.getIpAddr() != null) {
            e.setInterface(ev
                           .getIpAddr());
        }
        
        // Service Name
        if (ev.getServiceType() != null) {
            e.setService(ev.getServiceType().getName());
        }

        // Description
        if (ev.getEventDescr() != null ) {
            e.setDescr(ev.getEventDescr());
        }
        
        // Log message
        if (ev.getEventLogMsg() != null) {
            Logmsg msg = new Logmsg();
            msg.setContent(ev.getEventLogMsg());
            e.setLogmsg(msg);
        }

        // severity
        if (ev.getEventSeverity() != null) {
            e.setSeverity(OnmsSeverity.get(ev.getEventSeverity()).getLabel());
        }
        
        // operator Instruction
        if (ev.getEventOperInstruct() != null) {
            e.setOperinstruct(ev.getEventOperInstruct());
        }

        // parms
        if (ev.getEventParms() != null ) {
            Parms parms = Parameter.decode(ev.getEventParms());
            if (parms != null ) e.setParms(parms);
        }

        AlarmData ad = new AlarmData();
        OnmsAlarm onmsAlarm = ev
                .getAlarm();
        try {
            if (onmsAlarm != null) {
                ad
                        .setReductionKey(onmsAlarm
                                .getReductionKey());
                ad
                        .setAlarmType(onmsAlarm
                                .getAlarmType());
                ad
                        .setClearKey(onmsAlarm
                                .getClearKey());
                e
                        .setAlarmData(ad);
            }
        } catch (ObjectNotFoundException e1) {
        }
        return e;
    }
	
	@SuppressWarnings("unchecked")
    private List<Event> getEventsByCriteria() {
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final EventDao eventDao = BeanUtils.getBean(bf,"eventDao", EventDao.class);
        final List<Event> xmlevents = new ArrayList<Event>();
        TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate",TransactionTemplate.class);
        try {
                transTemplate.execute(new TransactionCallback() {
                public Object doInTransaction(final TransactionStatus status) {
                    final OnmsCriteria criteria = new OnmsCriteria(OnmsEvent.class);
                    criteria.add(Restrictions.sqlRestriction(m_criteriaRestriction));
                    List<OnmsEvent> events = eventDao.findMatching(criteria);
                    if (events != null && events.size()>0) {
                        Iterator<OnmsEvent> ite = events.iterator();
                        while (ite.hasNext()) {
                            Event xmlEvent = getXMLEvent(ite.next());
                            if (xmlEvent != null) xmlevents.add(xmlEvent);
                        }
                    }
                    return new Object();
                }

            });
        
        } catch (final RuntimeException e) {
        }
        return xmlevents;
    }
	

}
