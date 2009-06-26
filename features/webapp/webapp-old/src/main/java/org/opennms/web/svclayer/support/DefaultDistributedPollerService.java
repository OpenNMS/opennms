/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: November 30, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.support;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opennms.netmgt.dao.LocationMonitorDao;
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
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DefaultDistributedPollerService implements
        DistributedPollerService {
    private LocationMonitorDao m_locationMonitorDao;
    
    private OnmsLocationMonitorAreaNameComparator m_comparator =
        new OnmsLocationMonitorAreaNameComparator();

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

    public LocationMonitorDao getLocationMonitorDao() {
        return m_locationMonitorDao;
    }

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

}
