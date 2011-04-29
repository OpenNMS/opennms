package org.opennms.netmgt.scriptd.helper;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.hibernate.criterion.Restrictions;

import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.dao.AlarmDao;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;

import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;

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

	String m_criteriaRestriction = "";

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
			forwarder.sendStartSync();
			for (Event event: getEvents()) {
				forwarder.flushSyncEvent(event);
			}
			forwarder.sendEndSync();
		}
	}

	private Event getXMLEvent(OnmsAlarm ev) {
        Event e = new Event();
        
        e.setDbid(ev.getLastEvent().getId());

        //UEI
        if (ev.getUei() != null ) {
            e.setUei(ev.getUei());
        } else {
            return null;
        }

        // Source
        if (ev.getLastEvent().getEventSource() != null ) {
            e.setSource(ev.getLastEvent().getEventSource());
        } 

        //nodeid
        if (ev.getNode() != null) {
            e.setNodeid(ev.getNode().getId());
        }


        // timestamp
        if (ev.getLastEventTime() != null) {
            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            e.setTime(dateFormat.format(ev.getLastEventTime()));
        }
        
        // host
        if (ev.getLastEvent().getEventHost() != null) {
            e.setHost(ev.getLastEvent().getEventHost());
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
        if (ev.getDescription() != null ) {
            e.setDescr(ev.getDescription());
        }
        
        // Log message
        if (ev.getLogMsg() != null) {
            Logmsg msg = new Logmsg();
            msg.setContent(ev.getLogMsg());
            e.setLogmsg(msg);
        }

        // severity
        if (ev.getSeverity() != null) {
            e.setSeverity((ev.getSeverity()).getLabel());
        }
        
        // operator Instruction
        if (ev.getOperInstruct() != null) {
            e.setOperinstruct(ev.getOperInstruct());
        }

        AlarmData ad = new AlarmData();
        ad.setReductionKey(ev.getReductionKey());
        ad.setAlarmType(ev.getAlarmType());
        if (ev.getClearKey() != null)
        	ad.setClearKey(ev.getClearKey());
        e.setAlarmData(ad);
        return e;
    }
	
    @SuppressWarnings("rawtypes")
	private List<Event> getEventsByCriteria() {
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final AlarmDao alarmDao = BeanUtils.getBean(bf,"alarmDao", AlarmDao.class);
        final List<Event> xmlevents = new ArrayList<Event>();
        TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate",TransactionTemplate.class);
        try {
                transTemplate.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(final TransactionStatus status) {
                    final OnmsCriteria criteria = new OnmsCriteria(OnmsEvent.class);
                    criteria.add(Restrictions.sqlRestriction(m_criteriaRestriction));
                    List<OnmsAlarm> alarms = alarmDao.findMatching(criteria);
                    Map<String, OnmsAlarm> forwardAlarms = new HashMap<String, OnmsAlarm>();
                	for (OnmsAlarm alarm : alarms) {
                		if (alarm.getAlarmType() == 2) {               
                			if (forwardAlarms.containsKey(alarm.getClearKey())) {
                				OnmsAlarm raisingAlarm = forwardAlarms.get(alarm.getClearKey());
                				if (raisingAlarm.getLastEventTime().before(alarm.getLastEventTime()))
                					forwardAlarms.remove(alarm.getClearKey());
                			} else {
                    			forwardAlarms.put(alarm.getClearKey(), alarm);                			                				
                			}
                		} else if (alarm.getAlarmType() == 1){
                			if (forwardAlarms.containsKey(alarm.getReductionKey())) {
                  				OnmsAlarm clearingAlarm = forwardAlarms.get(alarm.getReductionKey());
                  			    if (clearingAlarm.getLastEventTime().after(alarm.getLastEventTime()))
                  			    	forwardAlarms.remove(alarm.getReductionKey());
                			} else {
                				forwardAlarms.put(alarm.getReductionKey(), alarm);                			                			
                			}
                		}  else {
            				forwardAlarms.put(alarm.getReductionKey(), alarm);                			                			                			
                		}
                    }
                	for (OnmsAlarm alarm : forwardAlarms.values()) {
                		if (alarm.getAlarmType() != 2) {               
                			Event xmlEvent = getXMLEvent(alarm);
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
