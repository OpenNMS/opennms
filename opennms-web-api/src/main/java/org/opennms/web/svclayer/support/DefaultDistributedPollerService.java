/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.web.svclayer.DistributedPollerService;
import org.opennms.web.svclayer.model.LocationMonitorIdCommand;
import org.opennms.web.svclayer.model.LocationMonitorListModel;
import org.opennms.web.svclayer.model.LocationMonitorListModel.LocationMonitorModel;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * <p>DefaultDistributedPollerService class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultDistributedPollerService implements DistributedPollerService {

    private MonitoringLocationDao m_monitoringLocationDao;
    private LocationMonitorDao m_locationMonitorDao;
    
    private OnmsLocationMonitorAreaNameComparator m_comparator =
        new OnmsLocationMonitorAreaNameComparator();

    /**
     * <p>getLocationMonitorList</p>
     *
     * @return a {@link org.opennms.web.svclayer.model.LocationMonitorListModel} object.
     */
    @Override
    public LocationMonitorListModel getLocationMonitorList() {
        List<OnmsLocationMonitor> monitors = m_locationMonitorDao.findAll();
        
        Collections.sort(monitors, m_comparator);
        
        LocationMonitorListModel model = new LocationMonitorListModel();
        for (OnmsLocationMonitor monitor : monitors) {
            OnmsMonitoringLocation def = m_monitoringLocationDao.get(monitor.getLocation());
            model.addLocationMonitor(new LocationMonitorModel(monitor, def));
        }
        
        return model;
    }

    /**
     * <p>getLocationMonitorDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.LocationMonitorDao} object.
     */
    public LocationMonitorDao getLocationMonitorDao() {
        return m_locationMonitorDao;
    }

    /**
     * <p>setMonitoringLocationDao</p>
     *
     * @param monitoringLocationDao a {@link org.opennms.netmgt.dao.api.MonitoringLocationDao} object.
     */
    public void setMonitoringLocationDao(MonitoringLocationDao monitoringLocationDao) {
        m_monitoringLocationDao = monitoringLocationDao;
    }

    /**
     * <p>setLocationMonitorDao</p>
     *
     * @param locationMonitorDao a {@link org.opennms.netmgt.dao.api.LocationMonitorDao} object.
     */
    public void setLocationMonitorDao(LocationMonitorDao locationMonitorDao) {
        m_locationMonitorDao = locationMonitorDao;
    }
    
    /**
     * Sorts OnmsLocationMonitor by the area for the monitoring location
     * definition (if any), then monitoring location definition name, and
     * finally by location monitor ID.
     * 
     * @author djgregor
     */
    public class OnmsLocationMonitorAreaNameComparator
                implements Comparator<OnmsLocationMonitor> {
        @Override
        public int compare(OnmsLocationMonitor o1, OnmsLocationMonitor o2) {
            OnmsMonitoringLocation def1 = null;
            OnmsMonitoringLocation def2 = null;
            
            if (o1.getLocation() != null) {
                def1 = m_monitoringLocationDao.get(o1.getLocation());
            }
            
            if (o2.getLocation() != null) {
                def2 = m_monitoringLocationDao.get(o2.getLocation());
            }
            
            int diff;
            
            if ((def1 == null || def1.getMonitoringArea() == null) && (def2 != null && def2.getMonitoringArea() != null)) {
                return 1;
            } else if ((def1 != null && def1.getMonitoringArea() != null) && (def2 == null || def2.getMonitoringArea() == null)) {
                return -1;
            } else if ((def1 != null && def1.getMonitoringArea() != null) && (def2 != null && def2.getMonitoringArea() != null)) {
                if ((diff = def1.getMonitoringArea().compareToIgnoreCase(def1.getMonitoringArea())) != 0) {
                    return diff;
                }
            }

            if ((diff = o1.getLocation().compareToIgnoreCase(o2.getLocation())) != 0) {
                return diff;
            }
            
            return o1.getId().compareTo(o2.getId());
        }
    }

    /** {@inheritDoc} */
    @Override
    public LocationMonitorListModel getLocationMonitorDetails(LocationMonitorIdCommand cmd, BindingResult errors) {
        LocationMonitorListModel model = new LocationMonitorListModel();
        model.setErrors(errors);
        
        if (errors.getErrorCount() > 0) {
            return model;
        }
        
        OnmsLocationMonitor monitor = m_locationMonitorDao.load(cmd.getMonitorId());
        OnmsMonitoringLocation def = m_monitoringLocationDao.get(monitor.getLocation());
        model.addLocationMonitor(new LocationMonitorModel(monitor, def));

        return model;
    }

    /** {@inheritDoc} */
    @Override
    public void pauseLocationMonitor(LocationMonitorIdCommand command, BindingResult errors) {
        if (command == null) {
            throw new IllegalStateException("command argument cannot be null");
        }
        if (errors == null) {
            throw new IllegalStateException("errors argument cannot be null");
        }
        
        if (errors.hasErrors()) {
            return;
        }
        
        OnmsLocationMonitor monitor = m_locationMonitorDao.load(command.getMonitorId());
        
        if (monitor.getStatus() == MonitorStatus.PAUSED) {
            errors.addError(new ObjectError(MonitorStatus.class.getName(),
                                            new String[] { "distributed.locationMonitor.alreadyPaused" },
                                            new Object[] { command.getMonitorId() },
                                            "Location monitor " + command.getMonitorId() + " is already paused."));
            return;
        }
        
        monitor.setStatus(MonitorStatus.PAUSED);
        m_locationMonitorDao.update(monitor);
    }

    /** {@inheritDoc} */
    @Override
    public void resumeLocationMonitor(LocationMonitorIdCommand command, BindingResult errors) {
        if (command == null) {
            throw new IllegalStateException("command argument cannot be null");
        }
        if (errors == null) {
            throw new IllegalStateException("errors argument cannot be null");
        }
        
        if (errors.hasErrors()) {
            return;
        }
        
        OnmsLocationMonitor monitor = m_locationMonitorDao.load(command.getMonitorId());
        
        if (monitor.getStatus() != MonitorStatus.PAUSED) {
            errors.addError(new ObjectError(MonitorStatus.class.getName(),
                                            new String[] { "distributed.locationMonitor.notPaused" },
                                            new Object[] { command.getMonitorId() },
                                            "Location monitor " + command.getMonitorId() + " is not paused."));
            return;
        }
        
        monitor.setStatus(MonitorStatus.STARTED);
        m_locationMonitorDao.update(monitor);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteLocationMonitor(LocationMonitorIdCommand command, BindingResult errors) {
        if (command == null) {
            throw new IllegalStateException("command argument cannot be null");
        }
        if (errors == null) {
            throw new IllegalStateException("errors argument cannot be null");
        }
        
        if (errors.hasErrors()) {
            return;
        }
        
        OnmsLocationMonitor monitor = m_locationMonitorDao.load(command.getMonitorId());
        m_locationMonitorDao.delete(monitor);
    }

    @Override
    public void pauseAllLocationMonitors() {
        m_locationMonitorDao.pauseAll();
    }

    @Override
    public void resumeAllLocationMonitors() {
        m_locationMonitorDao.resumeAll();
    }

}
