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

package org.opennms.netmgt.alarmd;

import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.UpdateField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.util.Assert;

/**
 * Singleton to persist OnmsAlarms.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class AlarmPersisterImpl implements AlarmPersister {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmPersisterImpl.class);

    private AlarmDao m_alarmDao;
    private EventDao m_eventDao;
    private EventForwarder m_eventForwarder;
    private TransactionOperations m_transactionOperations;

    private static class OnmsAlarmAndLifecycleEvent {
        private final OnmsAlarm m_alarm;
        private final Event m_event;

        public OnmsAlarmAndLifecycleEvent(OnmsAlarm alarm, Event event) {
            m_alarm = alarm;
            m_event = event;
        }

        public OnmsAlarm getAlarm() {
            return m_alarm;
        }

        public Event getEvent() {
            return m_event;
        }
    }

    /** {@inheritDoc} 
     * @return */
    @Override
    public OnmsAlarm persist(Event event) {
        if (!checkEventSanityAndDoWeProcess(event)) {
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("process: {}; nodeid: {}; ipaddr: {}; serviceid: {}", event.getUei(), event.getNodeid(), event.getInterface(), event.getService());
        }

        // Process the alarm inside a transaction
        OnmsAlarmAndLifecycleEvent alarmAndEvent = m_transactionOperations.execute(new TransactionCallback<OnmsAlarmAndLifecycleEvent>() {
            @Override
            public OnmsAlarmAndLifecycleEvent doInTransaction(TransactionStatus arg0) {
                return addOrReduceEventAsAlarm(event);
            }
        });

        // Send the event outside of the database transaction
        m_eventForwarder.sendNow(alarmAndEvent.getEvent());

        return alarmAndEvent.getAlarm();
    }

    private OnmsAlarmAndLifecycleEvent addOrReduceEventAsAlarm(Event event) {
        // 2012-03-11 pbrane: for some reason when we get here the event from the DB doesn't have the LogMsg (in my tests anyway)
        OnmsEvent e = m_eventDao.get(event.getDbid());
        Assert.notNull(e, "Event was deleted before we could retrieve it and create an alarm.");

        String reductionKey = event.getAlarmData().getReductionKey();
        LOG.debug("addOrReduceEventAsAlarm: looking for existing reduction key: {}", reductionKey);
        OnmsAlarm alarm = m_alarmDao.findByReductionKey(reductionKey);

        EventBuilder ebldr = null;
        if (alarm == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addOrReduceEventAsAlarm: reductionKey:{} not found, instantiating new alarm", reductionKey);
            }
            alarm = createNewAlarm(e, event);
            
            //FIXME: this should be a cascaded save
            m_alarmDao.save(alarm);
            m_eventDao.saveOrUpdate(e);

            ebldr = new EventBuilder(EventConstants.ALARM_CREATED_UEI, Alarmd.NAME);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addOrReduceEventAsAlarm: reductionKey:{} found, reducing event to existing alarm: {}", reductionKey, alarm.getIpAddr());
            }
            reduceEvent(e, alarm, event);
            m_alarmDao.update(alarm);
            m_eventDao.update(e);

            if (event.getAlarmData().isAutoClean()) {
                m_eventDao.deletePreviousEventsForAlarm(alarm.getId(), e);
            }

            ebldr = new EventBuilder(EventConstants.ALARM_UPDATED_WITH_REDUCED_EVENT_UEI, Alarmd.NAME);
        }

        if (alarm.getNodeId() != null) {
            alarm.getNode().getForeignSource(); // This should trigger the lazy loading of the node object, to properly populate the NorthboundAlarm class.
        }

        ebldr.addParam(EventConstants.PARM_ALARM_UEI, alarm.getUei());
        ebldr.addParam(EventConstants.PARM_ALARM_ID, alarm.getId());

        return new OnmsAlarmAndLifecycleEvent(alarm, ebldr.getEvent());
    }

    private static void reduceEvent(OnmsEvent e, OnmsAlarm alarm, Event event) {
        
        //Always set these
        alarm.setLastEvent(e);
        alarm.setLastEventTime(e.getEventTime());
        alarm.setCounter(alarm.getCounter() + 1);
        
        if (!event.getAlarmData().hasUpdateFields()) {
            
            //We always set these even if there are not update fields specified
            alarm.setLogMsg(e.getEventLogMsg());
            alarm.setEventParms(e.getEventParms());
        } else {
            for (UpdateField field : event.getAlarmData().getUpdateFieldList()) {
                String fieldName = field.getFieldName();

                //Always set these, unless specified not to, in order to maintain current behavior
                if (fieldName.equalsIgnoreCase("LogMsg") && field.isUpdateOnReduction() == false) {
                    continue;
                } else {
                    alarm.setLogMsg(e.getEventLogMsg());
                }

                if (fieldName.equalsIgnoreCase("Parms") && field.isUpdateOnReduction() == false) {
                    continue;
                } else {
                    alarm.setEventParms(e.getEventParms());
                }


                //Set these others
                if (field.isUpdateOnReduction()) {
                    
                    if (fieldName.toLowerCase().startsWith("distpoller")) {
                        alarm.setDistPoller(e.getDistPoller());
                    } else if (fieldName.toLowerCase().startsWith("ipaddr")) {
                        alarm.setIpAddr(e.getIpAddr());
                    } else if (fieldName.toLowerCase().startsWith("mouseover")) {
                        alarm.setMouseOverText(e.getEventMouseOverText());
                    } else if (fieldName.toLowerCase().startsWith("operinstruct")) {
                        alarm.setOperInstruct(e.getEventOperInstruct());
                    } else if (fieldName.equalsIgnoreCase("severity")) {
                        alarm.setSeverity(OnmsSeverity.valueOf(e.getSeverityLabel()));
                    } else if (fieldName.toLowerCase().contains("descr")) {
                        alarm.setDescription(e.getEventDescr());
                        alarm.setSeverity(OnmsSeverity.valueOf(e.getSeverityLabel()));
                    } else {
                        LOG.warn("reduceEvent: The specified field: {}, is not supported.", fieldName);
                    }

                    /* This doesn't work because the properties are not consistent from OnmsEvent to OnmsAlarm
                    try {
                        final BeanWrapper ew = PropertyAccessorFactory.forBeanPropertyAccess(e);
                        final BeanWrapper aw = PropertyAccessorFactory.forBeanPropertyAccess(alarm);
                        aw.setPropertyValue(fieldName, ew.getPropertyValue(fieldName));
                    } catch (BeansException be) {                        
                        LOG.error("reduceEvent", be);
                        continue;
                    }
                    */
                    
                }
            }
            
        }

        e.setAlarm(alarm);
    }

    private static OnmsAlarm createNewAlarm(OnmsEvent e, Event event) {
        if (e.getServiceType() != null) {
            e.getServiceType().getName(); // To avoid potential LazyInitializationException when dealing with NorthboundAlarm
        }
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setAlarmType(event.getAlarmData().getAlarmType());
        alarm.setClearKey(event.getAlarmData().getClearKey());
        alarm.setCounter(1);
        alarm.setDescription(e.getEventDescr());
        alarm.setDistPoller(e.getDistPoller());
        alarm.setEventParms(e.getEventParms());
        alarm.setFirstEventTime(e.getEventTime());
        alarm.setIfIndex(e.getIfIndex());
        alarm.setIpAddr(e.getIpAddr());
        alarm.setLastEventTime(e.getEventTime());
        alarm.setLastEvent(e);
        alarm.setLogMsg(e.getEventLogMsg());
        alarm.setMouseOverText(e.getEventMouseOverText());
        alarm.setNode(e.getNode());
        alarm.setOperInstruct(e.getEventOperInstruct());
        alarm.setReductionKey(event.getAlarmData().getReductionKey());
        alarm.setServiceType(e.getServiceType());
        alarm.setSeverity(OnmsSeverity.get(e.getEventSeverity())); //TODO: what to do?
        alarm.setSuppressedUntil(e.getEventTime()); //TODO: fix UI to not require this be set
        alarm.setSuppressedTime(e.getEventTime()); //TODO: Fix UI to not require this be set
        //alarm.setTTicketId(e.getEventTTicket());
        //alarm.setTTicketState(TroubleTicketState.CANCEL_FAILED);  //FIXME
        alarm.setUei(e.getEventUei());
        e.setAlarm(alarm);
        return alarm;
    }
    
    private static boolean checkEventSanityAndDoWeProcess(final Event event) {
        // 2009-01-07 pbrane: TODO: Understand why we use Assert
        Assert.notNull(event, "Incoming event was null, aborting"); 

        if (event.getLogmsg() != null && "donotpersist".equals(event.getLogmsg().getDest())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("checkEventSanity: uei '{}' marked as 'donotpersist'; not processing event.", event.getUei());
            }
            return false;
        }
        
        if (event.getAlarmData() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("checkEventSanity: uei '{}' has no alarm data; not processing event.", event.getUei());
            }
            return false;
        }

        // 2009-01-07 pbrane: TODO: Understand why we use Assert
        Assert.isTrue(event.getDbid() > 0, "Incoming event has an illegal dbid (" + event.getDbid() + "), aborting");

        return true;
    }

    public TransactionOperations getTransactionOperations() {
        return m_transactionOperations;
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        m_transactionOperations = transactionOperations;
    }

    /**
     * <p>setAlarmDao</p>
     *
     * @param alarmDao a {@link org.opennms.netmgt.dao.api.AlarmDao} object.
     */
    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    /**
     * <p>getAlarmDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.AlarmDao} object.
     */
    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    /**
     * <p>setEventDao</p>
     *
     * @param eventDao a {@link org.opennms.netmgt.dao.api.EventDao} object.
     */
    public void setEventDao(EventDao eventDao) {
        m_eventDao = eventDao;
    }

    /**
     * <p>getEventDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.EventDao} object.
     */
    public EventDao getEventDao() {
        return m_eventDao;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }
}
