package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.model.LocationMonitorIpInterface;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.springframework.util.Assert;

public class MockLocationMonitorDao extends AbstractMockDao<OnmsLocationMonitor, Integer> implements LocationMonitorDao {
    private AtomicInteger m_id = new AtomicInteger(0);
    private Map<String,OnmsMonitoringLocationDefinition> m_locationDefinitions = new HashMap<String,OnmsMonitoringLocationDefinition>();
    private LinkedHashSet<OnmsLocationSpecificStatus> m_statuses = new LinkedHashSet<OnmsLocationSpecificStatus>();

    @Override
    protected void generateId(final OnmsLocationMonitor mon) {
        mon.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsLocationMonitor loc) {
        return loc.getId();
    }

    @Override
    public Collection<OnmsLocationMonitor> findByLocationDefinition(final OnmsMonitoringLocationDefinition locationDefinition) {
        final Set<OnmsLocationMonitor> monitors = new HashSet<OnmsLocationMonitor>();
        for (final OnmsLocationMonitor mon : findAll()) {
            if (mon.getDefinitionName().equals(locationDefinition.getName())) {
                monitors.add(mon);
            }
        }
        return monitors;
    }

    @Override
    public Collection<OnmsLocationMonitor> findByApplication(final OnmsApplication application) {
        /*
         *         return findObjects(OnmsLocationMonitor.class, "select distinct l from OnmsLocationSpecificStatus as status " +
                        "join status.monitoredService as m " +
                        "join m.applications a " +
                        "join status.locationMonitor as l " +
                        "where a = ? and status.id in ( " +
                    "select max(s.id) from OnmsLocationSpecificStatus as s " +
                    "group by s.locationMonitor, s.monitoredService " +
                ")", application);

         */
        final Set<OnmsLocationMonitor> monitors = new HashSet<OnmsLocationMonitor>();
        for (final OnmsLocationSpecificStatus stat : getAllMostRecentStatusChanges()) {
            if (stat.getMonitoredService().getApplications().contains(application)) {
                monitors.add(stat.getLocationMonitor());
            }
        }
        return monitors;
    }

    @Override
    public List<OnmsMonitoringLocationDefinition> findAllMonitoringLocationDefinitions() {
        return new ArrayList<OnmsMonitoringLocationDefinition>(m_locationDefinitions.values());
    }

    @Override
    public OnmsMonitoringLocationDefinition findMonitoringLocationDefinition(final String monitoringLocationDefinitionName) {
        if (m_locationDefinitions.containsKey(monitoringLocationDefinitionName)) {
            return m_locationDefinitions.get(monitoringLocationDefinitionName);
        }
        return null;
    }

    @Override
    public void saveMonitoringLocationDefinition(final OnmsMonitoringLocationDefinition def) {
        m_locationDefinitions.put(def.getName(), def);
    }

    @Override
    public void saveMonitoringLocationDefinitions(final Collection<OnmsMonitoringLocationDefinition> defs) {
        for (final OnmsMonitoringLocationDefinition def : defs) {
           saveMonitoringLocationDefinition(def);
        }
    }

    @Override
    public void saveStatusChange(final OnmsLocationSpecificStatus status) {
        m_statuses .add(status);
    }

    @Override
    public OnmsLocationSpecificStatus getMostRecentStatusChange(final OnmsLocationMonitor locationMonitor, final OnmsMonitoredService monSvc) {
        final Iterator<OnmsLocationSpecificStatus> it = new LinkedList<OnmsLocationSpecificStatus>(m_statuses).descendingIterator();
        while (it.hasNext()) {
            OnmsLocationSpecificStatus stat = it.next();
            if (locationMonitor.getId() == stat.getLocationMonitor().getId() && monSvc.getId() == stat.getMonitoredService().getId()) {
                return stat;
            }
        }
        return null;
    }

    private static class StatusState {
        private final OnmsLocationSpecificStatus m_status;

        public StatusState(final OnmsLocationSpecificStatus status) {
            Assert.notNull(status);
            Assert.notNull(status.getMonitoredService());
            Assert.notNull(status.getMonitoredService().getId());
            Assert.notNull(status.getLocationMonitor());
            Assert.notNull(status.getLocationMonitor().getId());
            m_status = status;
        }

        public OnmsLocationSpecificStatus getStatus() {
            return m_status;
        }

        @Override
        public int hashCode() {
            final int prime = 17;
            int result = 1;
            result = prime * result + getMonitoredServiceId().hashCode();
            result = prime * result + getLocationMonitorId().hashCode();
            return result;
        }

        private Integer getLocationMonitorId() {
            return m_status.getLocationMonitor().getId();
        }

        private Integer getMonitoredServiceId() {
            return m_status.getMonitoredService().getId();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof StatusState))
                return false;
            StatusState other = (StatusState) obj;
            if (getLocationMonitorId() == null) {
                if (other.getLocationMonitorId() != null)
                    return false;
            } else if (!getLocationMonitorId().equals(other.getLocationMonitorId()))
                return false;
            if (getMonitoredServiceId() == null) {
                if (other.getMonitoredServiceId() != null)
                    return false;
            } else if (!getMonitoredServiceId().equals(other.getMonitoredServiceId()))
                return false;
            return true;
        }
    }
    @Override
    public Collection<OnmsLocationSpecificStatus> getAllMostRecentStatusChanges() {
        return getMostRecentStatusChangesInCollection(m_statuses);
    }

    private Collection<OnmsLocationSpecificStatus> getMostRecentStatusChangesInCollection(final Collection<OnmsLocationSpecificStatus> sourceStatuses) {
        final Set<StatusState> states = new LinkedHashSet<StatusState>();
        for (final OnmsLocationSpecificStatus status : sourceStatuses) {
            final StatusState state = new StatusState(status);
            states.add(state);
        }
        final List<OnmsLocationSpecificStatus> statuses = new ArrayList<OnmsLocationSpecificStatus>();
        for (final StatusState state : states) {
            statuses.add(state.getStatus());
        }
        return statuses;
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getAllStatusChangesAt(final Date timestamp) {
        return getMostRecentStatusChangesInCollection(getStatusChangesBetween(new Date(0), timestamp));
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesBetween(final Date startDate, final Date endDate) {
        final List<OnmsLocationSpecificStatus> statuses = new ArrayList<OnmsLocationSpecificStatus>();
        for (final OnmsLocationSpecificStatus status : m_statuses) {
            final Date timestamp = status.getPollResult().getTimestamp();
            if (timestamp.getTime() == startDate.getTime() || timestamp.after(startDate)) {
                if (timestamp.before(endDate)) statuses.add(status);
            }
        }
        return statuses;
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesForLocationBetween(final Date startDate, final Date endDate, final String locationDefinitionName) {
        final List<OnmsLocationSpecificStatus> statuses = new ArrayList<OnmsLocationSpecificStatus>();
        for (final OnmsLocationSpecificStatus status : getStatusChangesBetween(startDate, endDate)) {
            if (locationDefinitionName.equals(status.getLocationMonitor().getName())) {
                statuses.add(status);
            }
        }
        return statuses;
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesForApplicationBetween(final Date startDate, final Date endDate, final String applicationName) {
        final List<OnmsLocationSpecificStatus> statuses = new ArrayList<OnmsLocationSpecificStatus>();
        for (final OnmsLocationSpecificStatus status : getStatusChangesBetween(startDate, endDate)) {
            for (final OnmsApplication app : status.getMonitoredService().getApplications()) {
                if (applicationName.equals(app.getName())) {
                    statuses.add(status);
                    break;
                }
            }
        }
        return statuses;
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getStatusChangesBetweenForApplications(final Date startDate, final Date endDate, final Collection<String> applicationNames) {
        final List<OnmsLocationSpecificStatus> statuses = new ArrayList<OnmsLocationSpecificStatus>();
        for (final OnmsLocationSpecificStatus status : getStatusChangesBetween(startDate, endDate)) {
            boolean added = false;
            for (final OnmsApplication app : status.getMonitoredService().getApplications()) {
                for (final String applicationName : applicationNames) {
                    if (applicationName.equals(app.getName())) {
                        statuses.add(status);
                        added = true;
                        break;
                    }
                }
                if (added) break;
            }
        }
        return statuses;
    }

    @Override
    public Collection<OnmsLocationSpecificStatus> getMostRecentStatusChangesForLocation(final String locationName) {
        final List<OnmsLocationSpecificStatus> statuses = new ArrayList<OnmsLocationSpecificStatus>();
        for (final OnmsLocationSpecificStatus status : getAllMostRecentStatusChanges()) {
            if (locationName.equals(status.getLocationMonitor().getName())) {
                statuses.add(status);
            }
        }
        return statuses;
    }

    @Override
    public Collection<LocationMonitorIpInterface> findStatusChangesForNodeForUniqueMonitorAndInterface(final int nodeId) {
        final Set<LocationMonitorIpInterface> ifaces = new HashSet<LocationMonitorIpInterface>();
        for (final OnmsLocationSpecificStatus status : m_statuses) {
            ifaces.add(new LocationMonitorIpInterface(status.getLocationMonitor(), status.getMonitoredService().getIpInterface()));
        }
        return ifaces;
    }

    @Override
    public void pauseAll() {
        for (final OnmsLocationMonitor monitor : findAll()) {
            if (monitor.getStatus() != MonitorStatus.STOPPED) monitor.setStatus(MonitorStatus.PAUSED);
        }
    }

    @Override
    public void resumeAll() {
        for (final OnmsLocationMonitor monitor : findAll()) {
            if (monitor.getStatus() == MonitorStatus.PAUSED) monitor.setStatus(MonitorStatus.STARTED);
        }
    }

}
