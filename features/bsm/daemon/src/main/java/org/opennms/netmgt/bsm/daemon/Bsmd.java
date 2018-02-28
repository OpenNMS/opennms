/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.daemon;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateChangeHandler;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.internal.AlarmWrapperImpl;
import org.opennms.netmgt.bsm.service.internal.SeverityMapper;
import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.daemon.DaemonTools;
import org.opennms.netmgt.daemon.SpringServiceDaemon;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * This daemon is responsible for driving the Business Service state machine by:
 *  1) Updating the state machine with Alarms when they are created, deleted or updated
 *  2) Sending events on the event bus when the operational status of a Business Service changes
 *  3) Reloading the Business Service configuration in the state machine when requested
 *
 * We currently listen for alarm life-cycle events in order to know when alarms change,
 * but not all changes send the required events (i.e. escalating an alarm in the WebUI).
 * In order to work around this, we periodically poll the database. 
 *
 * @author jwhite
 */
@EventListener(name=Bsmd.NAME, logPrefix="bsmd")
public class Bsmd implements SpringServiceDaemon, BusinessServiceStateChangeHandler  {
    private static final Logger LOG = LoggerFactory.getLogger(Bsmd.class);

    protected static final long DEFAULT_POLL_INTERVAL = 30; // seconds

    protected static final String POLL_INTERVAL_KEY = "org.opennms.features.bsm.pollInterval";

    public static final String NAME = "Bsmd";

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    @Qualifier("eventIpcManager")
    private EventIpcManager m_eventIpcManager;

    @Autowired
    private EventConfDao m_eventConfDao;

    @Autowired
    private TransactionTemplate m_template;

    @Autowired
    private BusinessServiceStateMachine m_stateMachine;

    @Autowired
    private BusinessServiceManager m_manager;

    private boolean m_verifyReductionKeys = true;

    private boolean m_hasBusinessServicesDefined = false;

    private ScheduledExecutorService m_alarmPoller;

    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(m_stateMachine, "stateMachine cannot be null");

        LOG.info("Initializing bsmd...");
        m_stateMachine.addHandler(this, null);
    }

    @Override
    public void start() throws Exception {
        Objects.requireNonNull(m_manager, "businessServiceDao cannot be null");
        Objects.requireNonNull(m_alarmDao, "alarmDao cannot be null");
        Objects.requireNonNull(m_eventIpcManager, "eventIpcManager cannot be null");
        Objects.requireNonNull(m_eventConfDao, "eventConfDao cannot be null");

        handleConfigurationChanged();
    }

    private void startAlarmPolling() {
        final long pollInterval = getPollInterval();
        m_alarmPoller = Executors.newScheduledThreadPool(1);
        m_alarmPoller.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    LOG.debug("Polling alarm table.");
                    m_template.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                            final List<AlarmWrapper> alarms = m_alarmDao.findAll().stream()
                                .map(AlarmWrapperImpl::new)
                                .collect(Collectors.toList());
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Handling {} alarms.", alarms.size());
                                LOG.trace("Handling alarms: {}", alarms);
                            }
                            m_stateMachine.handleAllAlarms(alarms);
                        }
                    });
                } catch (Exception ex) {
                    LOG.error("Error while polling alarm table.", ex);
                } finally {
                    LOG.debug("Polling alarm table complete.");
                }
            }
        }, pollInterval, pollInterval, TimeUnit.SECONDS);
    }

    private void stopAlarmPolling() {
        if (m_alarmPoller != null) {
            m_alarmPoller.shutdown();
            m_alarmPoller = null;
        }
    }

    protected boolean isAlarmPolling() {
        return m_alarmPoller != null;
    }

    protected long getPollInterval() {
        final String pollIntervalProperty = System.getProperty(POLL_INTERVAL_KEY, Long.toString(DEFAULT_POLL_INTERVAL));
        try {
            long pollInterval = Long.valueOf(pollIntervalProperty);
            if (pollInterval <= 0) {
                LOG.warn("Defined pollInterval must be greater than 0, but was {}. Falling back to default: {}", pollInterval, DEFAULT_POLL_INTERVAL);
                return DEFAULT_POLL_INTERVAL;
            }
            LOG.debug("Using poll interval {}", pollInterval);
            return pollInterval;
        } catch (Exception ex) {
            LOG.warn("The defined pollInterval {} could not be interpreted as long value. Falling back to default: {}", pollIntervalProperty, DEFAULT_POLL_INTERVAL);
            return DEFAULT_POLL_INTERVAL;
        }
    }

    /**
     * Called when the configuration of one or more business services was changed.
     */
    private void handleConfigurationChanged() {
        if (m_verifyReductionKeys) {
            // The state machine makes certain assumptions about the reduction keys
            // associated with particular events. Since these are configurable, we may
            // want to verify that the actual values match our assumptions and bail if they don't
            verifyReductionKey(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, "%uei%:%dpname%:%nodeid%:%interface%:%service%");
            verifyReductionKey(EventConstants.NODE_DOWN_EVENT_UEI, "%uei%:%dpname%:%nodeid%");
            verifyReductionKey(EventConstants.INTERFACE_DOWN_EVENT_UEI, "%uei%:%dpname%:%nodeid%:%interface%");
        }

        // Update the state machine with the latest list of business services
        m_template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                final List<BusinessService> businessServices = m_manager.getAllBusinessServices();
                m_hasBusinessServicesDefined = businessServices.size() > 0;
                LOG.debug("Adding {} business services to the state machine.", businessServices.size());
                m_stateMachine.setBusinessServices(businessServices);
            }
        });

        // Enable/disable polling for alarms based on whether or not
        // we have any business services
        if (m_hasBusinessServicesDefined && !isAlarmPolling()) {
            startAlarmPolling();
        } else if (!m_hasBusinessServicesDefined && isAlarmPolling()) {
            stopAlarmPolling();
        }
    }

    private void verifyReductionKey(String uei, String expectedReductionKey) {
        List<org.opennms.netmgt.xml.eventconf.Event> eventsForUei = m_eventConfDao.getEvents(uei);
        if (eventsForUei == null) {
            LOG.warn("Could not find an event with uei '{}'.");
            return;
        }
        if (eventsForUei.size() != 1) {
            LOG.warn("Could not find a unique event definition for uei '{}'.", uei);
            return;
        }
        if (eventsForUei.get(0).getAlarmData() == null) {
            LOG.warn("Could not find alarm data for event with uei '{}'.", uei);
            return;
        }
        AlarmData alarmData = eventsForUei.get(0).getAlarmData();
        if (!expectedReductionKey.equals(alarmData.getReductionKey())) {
            LOG.warn("Expected reduction key '{}' for uei '{}' but found '{}'.", expectedReductionKey, uei, alarmData.getReductionKey());
        }
    }

    @EventHandler(ueis = {
        EventConstants.ALARM_CREATED_UEI,
        EventConstants.ALARM_ESCALATED_UEI,
        EventConstants.ALARM_CLEARED_UEI,
        EventConstants.ALARM_UNCLEARED_UEI,
        EventConstants.ALARM_UPDATED_WITH_REDUCED_EVENT_UEI,
        EventConstants.ALARM_DELETED_EVENT_UEI
    })
    public void handleAlarmLifecycleEvents(Event e) {
        if (e == null || !m_hasBusinessServicesDefined) {
            // Return quick if we weren't given an event, or if there are no business rules defined
            // in which case we don't need to perform any further handling
            return;
        }

        final Parm alarmIdParm = e.getParm(EventConstants.PARM_ALARM_ID);
        if (alarmIdParm == null || alarmIdParm.getValue() == null) {
            LOG.warn("The alarmId parameter has no value on event with uei: {}. Ignoring.", e.getUei());
            return;
        }

        int alarmId;
        try {
            alarmId = Integer.parseInt(alarmIdParm.getValue().getContent());
        } catch (NumberFormatException ee) {
            LOG.warn("Failed to retrieve the alarmId for event with uei: {}. Ignoring.", e.getUei(), ee);
            return;
        }

        if (EventConstants.ALARM_DELETED_EVENT_UEI.equals(e.getUei())) {
            handleAlarmDeleted(e, alarmId);
        } else {
            handleAlarmCreatedOrUpdated(e, alarmId);
        }
    }

    private void handleAlarmCreatedOrUpdated(Event e, int alarmId) {
        m_template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                final OnmsAlarm alarm = m_alarmDao.get(alarmId);
                if (alarm == null) {
                    LOG.error("Could not find alarm with id: {} for event with uei: {}. Ignoring.", alarmId, e.getUei());
                    return;
                }
                final AlarmWrapperImpl alarmWrapper = new AlarmWrapperImpl(alarm);
                LOG.debug("Handling alarm with id: {}, reduction key: {} and severity: {} and status: {}", alarm.getId(), alarm.getReductionKey(), alarm.getSeverity(), alarmWrapper.getStatus());
                m_stateMachine.handleNewOrUpdatedAlarm(alarmWrapper);
            }
        });
    }

    private void handleAlarmDeleted(Event e, int alarmId) {
        final Parm alarmReductionKeyParm = e.getParm(EventConstants.PARM_ALARM_REDUCTION_KEY);
        if (alarmReductionKeyParm == null || alarmReductionKeyParm.getValue() == null) {
            LOG.warn("The alarmReductionKey parameter has no value on event with uei: {}. Ignoring.", e.getUei());
            return;
        }

        final String reductionKey = alarmReductionKeyParm.getValue().toString();
        LOG.debug("Handling delete for alarm with id: {} and reduction key: {}", alarmId, reductionKey);
        m_stateMachine.handleNewOrUpdatedAlarm(new AlarmWrapper() {
            @Override
            public String getReductionKey() {
                return reductionKey;
            }

            @Override
            public Status getStatus() {
                return Status.INDETERMINATE;
            }
        });
    }

    /**
     * Called when the operational status of a business service was changed.
     */
    @Override
    public void handleBusinessServiceStateChanged(BusinessService businessService, Status newStatus, Status prevStatus) {
        final OnmsSeverity newSeverity = SeverityMapper.toSeverity(newStatus);
        final OnmsSeverity prevSeverity = SeverityMapper.toSeverity(prevStatus);

        // Always send an serviceOperationalStatusChanged event
        EventBuilder ebldr = new EventBuilder(EventConstants.BUSINESS_SERVICE_OPERATIONAL_STATUS_CHANGED_UEI, NAME);
        addBusinessServicesAttributesAsEventParms(businessService, ebldr);
        ebldr.addParam(EventConstants.PARM_BUSINESS_SERVICE_ID, businessService.getId());
        ebldr.addParam(EventConstants.PARM_BUSINESS_SERVICE_NAME, businessService.getName());
        ebldr.addParam(EventConstants.PARM_PREV_SEVERITY_ID, prevSeverity.getId());
        ebldr.addParam(EventConstants.PARM_PREV_SEVERITY_LABEL, prevSeverity.getLabel());
        ebldr.addParam(EventConstants.PARM_NEW_SEVERITY_ID, newSeverity.getId());
        ebldr.addParam(EventConstants.PARM_NEW_SEVERITY_LABEL, newSeverity.getLabel());
        m_eventIpcManager.sendNow(ebldr.getEvent());

        // Generate a serviceProblem or a serviceProblemResolved event based on the current status
        if (newSeverity.isGreaterThan(OnmsSeverity.NORMAL)) {
            ebldr = new EventBuilder(EventConstants.BUSINESS_SERVICE_PROBLEM_UEI, NAME);
            addBusinessServicesAttributesAsEventParms(businessService, ebldr);
            ebldr.addParam(EventConstants.PARM_BUSINESS_SERVICE_ID, businessService.getId());
            ebldr.addParam(EventConstants.PARM_BUSINESS_SERVICE_NAME, businessService.getName());
            ebldr.setSeverity(newSeverity.toString());
        } else {
            ebldr = new EventBuilder(EventConstants.BUSINESS_SERVICE_PROBLEM_RESOLVED_UEI, NAME);
            addBusinessServicesAttributesAsEventParms(businessService, ebldr);
            ebldr.addParam(EventConstants.PARM_BUSINESS_SERVICE_ID, businessService.getId());
            ebldr.addParam(EventConstants.PARM_BUSINESS_SERVICE_NAME, businessService.getName());
        }
        m_eventIpcManager.sendNow(ebldr.getEvent());
    }

    /**
     * Adds all of the business services attributes as parameters to the given event builder.
     */
    private static void addBusinessServicesAttributesAsEventParms(BusinessService businessService, EventBuilder ebldr) {
        businessService.getAttributes().entrySet().stream()
            .forEach(attr -> {
                ebldr.addParam(attr.getKey(), attr.getValue());
            });
    }

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadEvent(Event e) {
        LOG.info("Received a reload configuration event: {}", e);
        DaemonTools.handleReloadEvent(e, Bsmd.NAME, (event) -> handleConfigurationChanged());
    }

    @Override
    public void destroy() throws Exception {
        LOG.info("Stopping bsmd...");
        stopAlarmPolling();
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }

    public EventIpcManager getEventIpcManager() {
        return m_eventIpcManager;
    }

    public void setEventConfDao(EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }

    public EventConfDao getEventConfDao() {
        return m_eventConfDao;
    }

    public void setTransactionTemplate(TransactionTemplate template) {
        m_template = template;
    }

    public TransactionTemplate getTransactionTemplate() {
        return m_template;
    }

    public void setVerifyReductionKeys(boolean verify) {
        m_verifyReductionKeys = verify;
    }

    public boolean getVerifyReductionKeys() {
        return m_verifyReductionKeys;
    }

    public void setBusinessServiceStateMachine(BusinessServiceStateMachine stateMachine) {
        m_stateMachine = stateMachine;
    }

    public BusinessServiceStateMachine getBusinessServiceStateMachine() {
        return m_stateMachine;
    }
}
