/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.scriptd.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.spring.BeanFactoryReference;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class AlarmEventSynchronization implements EventSynchronization {

	List<EventForwarder> m_forwarders = new ArrayList<>();

	public AlarmEventSynchronization() {
		super();
    }

        @Override
	public void addEventForwarder(EventForwarder forwarder) {
		if (forwarder != null)
			m_forwarders.add(forwarder);
	}

        @Override
	public void sync() {
		
		for (EventForwarder forwarder: m_forwarders) {
			forwarder.sendStartSync();
			for (Event event: getEvents()) {
				forwarder.flushSyncEvent(event);
			}
			forwarder.sendEndSync();
		}
	}

	private Event getXMLEvent(OnmsAlarm alarm) {
        Event event = new Event();
        
        event.setDbid(alarm.getLastEvent().getId());

        //UEI
        if (alarm.getUei() != null ) {
            event.setUei(alarm.getUei());
        } else {
            return null;
        }

        // Source
        if (alarm.getLastEvent().getEventSource() != null ) {
            event.setSource(alarm.getLastEvent().getEventSource());
        } 

        //nodeid
        if (alarm.getNode() != null) {
            event.setNodeid(alarm.getNode().getId().longValue());
        }

        // alarm creation time
        if (alarm.getFirstEventTime() != null) {
        	// TODO: This is incorrect. The creation time represents
        	// the time that the event was stored in the database, not
        	// the original timestamp of the event.
        	event.setCreationTime(alarm.getFirstEventTime());
        }

        // last event timestamp
        if (alarm.getLastEventTime() != null) {
            event.setTime(alarm.getLastEventTime());
        }
        
        // host
        if (alarm.getLastEvent().getEventHost() != null) {
            event.setHost(alarm.getLastEvent().getEventHost());
        }
        
        // interface
        if (alarm.getIpAddr() != null) {
            event.setInterfaceAddress(alarm.getIpAddr());
        }
        
        // Service Name
        if (alarm.getServiceType() != null) {
            event.setService(alarm.getServiceType().getName());
        }

        // Description
        if (alarm.getDescription() != null ) {
            event.setDescr(alarm.getDescription());
        }
        
        // Log message
        if (alarm.getLogMsg() != null) {
            Logmsg msg = new Logmsg();
            msg.setContent(alarm.getLogMsg());
            event.setLogmsg(msg);
        }

        // severity
        if (alarm.getSeverity() != null) {
            event.setSeverity((alarm.getSeverity()).getLabel());
        }
        
        // operator Instruction
        if (alarm.getOperInstruct() != null) {
            event.setOperinstruct(alarm.getOperInstruct());
        }

        AlarmData ad = new AlarmData();
        ad.setReductionKey(alarm.getReductionKey());
        ad.setAlarmType(alarm.getAlarmType());
        if (alarm.getClearKey() != null)
        	ad.setClearKey(alarm.getClearKey());
        event.setAlarmData(ad);
        return event;
    }
	
        @Override
	public List<Event> getEvents() {
        BeanFactoryReference bf = BeanUtils.getBeanFactory("daoContext");
        final AlarmDao alarmDao = BeanUtils.getBean(bf,"alarmDao", AlarmDao.class);
        final List<Event> xmlevents = new ArrayList<>();
        TransactionTemplate transTemplate = BeanUtils.getBean(bf, "transactionTemplate",TransactionTemplate.class);
        try {
                transTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    Map<String, OnmsAlarm> forwardAlarms = new HashMap<String, OnmsAlarm>();
                	for (OnmsAlarm alarm : alarmDao.findAll()) {
                		// Got Clear alarm
                		if (alarm.getAlarmType() == 2) {               
                			if (forwardAlarms.containsKey(alarm.getClearKey())) {
                				OnmsAlarm raise = forwardAlarms.get(alarm.getClearKey());
                				if (raise.getLastEventTime().before(alarm.getLastEventTime())) {
                					forwardAlarms.remove(alarm.getClearKey());
                				}
                			} else {
                    			forwardAlarms.put(alarm.getClearKey(), alarm);                			                				
                			}
                		} else if (alarm.getAlarmType() == 1){
                			if (forwardAlarms.containsKey(alarm.getReductionKey())) {
                  				OnmsAlarm clear = forwardAlarms.get(alarm.getReductionKey());
                  			    if (clear.getLastEventTime().before(alarm.getLastEventTime())) {
                  			    	forwardAlarms.put(alarm.getReductionKey(),alarm);
                  			    }
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
                }

            });
        
        } catch (final RuntimeException e) {
        }
        return xmlevents;
    }
	

}
