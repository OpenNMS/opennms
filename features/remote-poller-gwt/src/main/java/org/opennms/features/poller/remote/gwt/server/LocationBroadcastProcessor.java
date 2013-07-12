/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.poller.remote.gwt.server;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationUpdatedRemoteEvent;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.events.Parameter;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>LocationBroadcastProcessor class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@EventListener(name="LocationStatusService")
public class LocationBroadcastProcessor implements InitializingBean, DisposableBean {
    private static final Logger LOG = LoggerFactory.getLogger(LocationBroadcastProcessor.class);
    @Autowired
    private LocationDataService m_locationDataService;

    @Autowired
    private EventDao m_eventDao;

    @SuppressWarnings("unused")
    private static final long UPDATE_PERIOD = 1000 * 60;
    @SuppressWarnings("unused")
    private static volatile Timer m_timer;

    private String[] m_events = new String[] {
            EventConstants.LOCATION_MONITOR_STARTED_UEI,
            EventConstants.LOCATION_MONITOR_STOPPED_UEI,
            EventConstants.LOCATION_MONITOR_DISCONNECTED_UEI,
            EventConstants.LOCATION_MONITOR_RECONNECTED_UEI,
            EventConstants.LOCATION_MONITOR_REGISTERED_UEI,
            EventConstants.LOCATION_MONITOR_PAUSED_UEI,
            EventConstants.REMOTE_NODE_LOST_SERVICE_UEI,
            EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI
    };

    public LocationEventHandler m_eventHandler;

    private TimerTask m_task;

    /**
     * <p>Constructor for LocationBroadcastProcessor.</p>
     */
    public LocationBroadcastProcessor() {
        m_timer = new Timer();
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);

        m_task = new TimerTask() {
            private Date m_lastRun = new Date();

            @Override
            public void run() {
                final Date now = new Date();
                final OnmsCriteria criteria = new OnmsCriteria(OnmsEvent.class)
                    .add(Restrictions.between("eventTime", m_lastRun, now))
                    .add(Restrictions.in("eventUei", m_events));
                for (final OnmsEvent e : m_eventDao.findMatching(criteria)) {
                    handleLocationEvent(e);
                }
                m_lastRun = now;
            }
        };
        //m_timer.schedule(m_task, UPDATE_PERIOD, UPDATE_PERIOD);
    }
    
    /**
     * <p>destroy</p>
     */
    @Override
    public void destroy() {
        if (m_task != null) {
            m_task.cancel();
        }
        
    }

    /**
     * <p>locationMonitorStarted</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.LOCATION_MONITOR_STARTED_UEI)
    public void locationMonitorStarted(final Event event) {
        handleLocationEvent(event);
    }

    /**
     * <p>locationMonitorStopped</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.LOCATION_MONITOR_STOPPED_UEI)
    public void locationMonitorStopped(final Event event) {
        handleLocationEvent(event);
    }

    /**
     * <p>locationMonitorDisconnected</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.LOCATION_MONITOR_DISCONNECTED_UEI)
    public void locationMonitorDisconnected(final Event event) {
        handleLocationEvent(event);
    }

    /**
     * <p>locationMonitorReconnected</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.LOCATION_MONITOR_RECONNECTED_UEI)
    public void locationMonitorReconnected(final Event event) {
        handleLocationEvent(event);
    }

    /**
     * <p>locationMonitorRegistered</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.LOCATION_MONITOR_REGISTERED_UEI)
    public void locationMonitorRegistered(final Event event) {
        handleLocationEvent(event);
    }

    /**
     * <p>locationMonitorPaused</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.LOCATION_MONITOR_PAUSED_UEI)
    public void locationMonitorPaused(final Event event) {
        handleLocationEvent(event);
    }

    /**
     * <p>nodeLostService</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.REMOTE_NODE_LOST_SERVICE_UEI)
    public void nodeLostService(final Event event) {
        handleLocationEvent(event);
    }

    /**
     * <p>nodeRegainedService</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI)
    public void nodeRegainedService(final Event event) {
        handleLocationEvent(event);
    }

    /**
     * <p>setEventHandler</p>
     *
     * @param handler a {@link org.opennms.features.poller.remote.gwt.server.LocationEventHandler} object.
     */
    public void setEventHandler(final LocationEventHandler handler) {
        m_eventHandler = handler;
    }

    private void handleLocationEvent(final OnmsEvent event) {
        if (m_eventHandler == null) {
            LOG.warn("handleLocationEvent called, but no eventHandler is registered");
            return;
        }
        handleEventParms(Parameter.decode(event.getEventParms()));
    }
    private void handleLocationEvent(final Event event) {
        if (m_eventHandler == null) {
            LOG.warn("handleLocationEvent called, but no eventHandler is registered");
            return;
        }
        handleEventParms(event.getParmCollection());
    }

    private void handleEventParms(final List<Parm> parms) {
        for (final Parm p : parms) {
            if (p.getParmName().equals(EventConstants.PARM_LOCATION_MONITOR_ID)) {
                final LocationInfo info = m_locationDataService.getLocationInfoForMonitor(Integer.valueOf(p.getValue().getContent()));
                m_eventHandler.sendEvent(new LocationUpdatedRemoteEvent(info));
                for (final ApplicationInfo applicationInfo : m_locationDataService.getApplicationsForLocation(info)) {
                    m_eventHandler.sendEvent(new ApplicationUpdatedRemoteEvent(applicationInfo));
                }
            }
        }
    }

}
