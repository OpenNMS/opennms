/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.web.command.LocationMonitorIdCommand;
import org.opennms.web.svclayer.DistributedPollerService;
import org.opennms.web.svclayer.LocationMonitorListModel;
import org.opennms.web.svclayer.LocationMonitorListModel.LocationMonitorModel;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;

/**
 * <p>DefaultDistributedPollerService class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultDistributedPollerService implements
        DistributedPollerService {
    private LocationMonitorDao m_locationMonitorDao;
    
    private OnmsLocationMonitorAreaNameComparator m_comparator =
        new OnmsLocationMonitorAreaNameComparator();

    /**
     * <p>getLocationMonitorList</p>
     *
     * @return a {@link org.opennms.web.svclayer.LocationMonitorListModel} object.
     */
    @Override
    public LocationMonitorListModel getLocationMonitorList() {
        List<OnmsLocationMonitor> monitors = m_locationMonitorDao.findAll();
        
        Collections.sort(monitors, m_comparator);
        
        LocationMonitorListModel model = new LocationMonitorListModel();
        for (OnmsLocationMonitor monitor : monitors) {
            OnmsMonitoringLocationDefinition def = m_locationMonitorDao.findMonitoringLocationDefinition(monitor.getDefinitionName());
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
            OnmsMonitoringLocationDefinition def1 = null;
            OnmsMonitoringLocationDefinition def2 = null;
            
            if (o1.getDefinitionName() != null) {
                def1 = m_locationMonitorDao.findMonitoringLocationDefinition(o1.getDefinitionName());
            }
            
            if (o2.getDefinitionName() != null) {
                def2 = m_locationMonitorDao.findMonitoringLocationDefinition(o2.getDefinitionName());
            }
            
            int diff;
            
            if ((def1 == null || def1.getArea() == null) && (def2 != null && def2.getArea() != null)) {
                return 1;
            } else if ((def1 != null && def1.getArea() != null) && (def2 == null || def2.getArea() == null)) {
                return -1;
            } else if ((def1 != null && def1.getArea() != null) && (def2 != null && def2.getArea() != null)) {
                if ((diff = def1.getArea().compareToIgnoreCase(def1.getArea())) != 0) {
                    return diff;
                }
            }

            if ((diff = o1.getDefinitionName().compareToIgnoreCase(o2.getDefinitionName())) != 0) {
                return diff;
            }
            
            return o1.getId().compareTo(o2.getId());
        }
    }

    /** {@inheritDoc} */
    @Override
    public LocationMonitorListModel getLocationMonitorDetails(LocationMonitorIdCommand cmd, BindException errors) {
        LocationMonitorListModel model = new LocationMonitorListModel();
        model.setErrors(errors);
        
        if (errors.getErrorCount() > 0) {
            return model;
        }
        
        OnmsLocationMonitor monitor = m_locationMonitorDao.load(cmd.getMonitorId());
        OnmsMonitoringLocationDefinition def = m_locationMonitorDao.findMonitoringLocationDefinition(monitor.getDefinitionName());
        model.addLocationMonitor(new LocationMonitorModel(monitor, def));

        return model;
    }

    /** {@inheritDoc} */
    @Override
    public void pauseLocationMonitor(LocationMonitorIdCommand command, BindException errors) {
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
    public void resumeLocationMonitor(LocationMonitorIdCommand command, BindException errors) {
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
    public void deleteLocationMonitor(LocationMonitorIdCommand command, BindException errors) {
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
