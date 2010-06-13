package org.opennms.features.poller.remote.gwt.server;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.LogUtils;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.remoteevents.ApplicationUpdatedRemoteEvent;
import org.opennms.features.poller.remote.gwt.client.remoteevents.LocationUpdatedRemoteEvent;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.events.Parameter;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

@EventListener(name="LocationStatusService")
public class LocationBroadcastProcessor implements InitializingBean {
    @Autowired
    private LocationDataService m_locationDataService;

    @Autowired
    private EventDao m_eventDao;

    private static final long UPDATE_PERIOD = 1000 * 60;
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

    public LocationBroadcastProcessor() {
        m_timer = new Timer();
    }

    public void afterPropertiesSet() throws Exception {
        m_timer.schedule(new TimerTask() {
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
        }, UPDATE_PERIOD, UPDATE_PERIOD);
    }

    @EventHandler(uei = EventConstants.LOCATION_MONITOR_STARTED_UEI)
    public void locationMonitorStarted(final Event event) {
        handleLocationEvent(event);
    }

    @EventHandler(uei = EventConstants.LOCATION_MONITOR_STOPPED_UEI)
    public void locationMonitorStopped(final Event event) {
        handleLocationEvent(event);
    }

    @EventHandler(uei = EventConstants.LOCATION_MONITOR_DISCONNECTED_UEI)
    public void locationMonitorDisconnected(final Event event) {
        handleLocationEvent(event);
    }

    @EventHandler(uei = EventConstants.LOCATION_MONITOR_RECONNECTED_UEI)
    public void locationMonitorReconnected(final Event event) {
        handleLocationEvent(event);
    }

    @EventHandler(uei = EventConstants.LOCATION_MONITOR_REGISTERED_UEI)
    public void locationMonitorRegistered(final Event event) {
        handleLocationEvent(event);
    }

    @EventHandler(uei = EventConstants.LOCATION_MONITOR_PAUSED_UEI)
    public void locationMonitorPaused(final Event event) {
        handleLocationEvent(event);
    }

    @EventHandler(uei = EventConstants.REMOTE_NODE_LOST_SERVICE_UEI)
    public void nodeLostService(final Event event) {
        handleLocationEvent(event);
    }

    @EventHandler(uei = EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI)
    public void nodeRegainedService(final Event event) {
        handleLocationEvent(event);
    }

    public void setEventHandler(final LocationEventHandler handler) {
        m_eventHandler = handler;
    }

    private void handleLocationEvent(final OnmsEvent event) {
        if (m_eventHandler == null) {
            LogUtils.warnf(this, "handleLocationEvent called, but no eventHandler is registered");
            return;
        }
        handleEventParms(Parameter.decode(event.getEventParms()));
    }
    private void handleLocationEvent(final Event event) {
        if (m_eventHandler == null) {
            LogUtils.warnf(this, "handleLocationEvent called, but no eventHandler is registered");
            return;
        }
        handleEventParms(event.getParms());
    }

    private void handleEventParms(final Parms parms) {
        for (final Parm p : parms.getParmCollection()) {
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