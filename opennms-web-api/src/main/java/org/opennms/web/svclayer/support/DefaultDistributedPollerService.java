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

import org.opennms.netmgt.dao.api.LocationSpecificStatusDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.web.svclayer.DistributedPollerService;
import org.opennms.web.svclayer.model.LocationMonitorIdCommand;
import org.opennms.web.svclayer.model.LocationMonitorListModel;
import org.opennms.web.svclayer.model.LocationMonitorListModel.LocationMonitorModel;
import org.springframework.validation.BindingResult;

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
    private LocationSpecificStatusDao m_locationSpecificStatusDao;

    /**
     * <p>getLocationMonitorList</p>
     *
     * @return a {@link org.opennms.web.svclayer.model.LocationMonitorListModel} object.
     */
    @Override
    public LocationMonitorListModel getLocationMonitorList() {
        List<OnmsMonitoringLocation> monitors = m_monitoringLocationDao.findAll();
        
        Collections.sort(monitors, Comparator.comparing(OnmsMonitoringLocation::getLocationName));
        
        LocationMonitorListModel model = new LocationMonitorListModel();
        for (OnmsMonitoringLocation monitor : monitors) {
            model.addLocationMonitor(new LocationMonitorModel(monitor));
        }
        
        return model;
    }

    /**
     * <p>getLocationMonitorDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.LocationSpecificStatusDao} object.
     */
    public LocationSpecificStatusDao getLocationSpecificStatusDao() {
        return m_locationSpecificStatusDao;
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
     * @param locationSpecificStatusDao a {@link org.opennms.netmgt.dao.api.LocationSpecificStatusDao} object.
     */
    public void setLocationSpecificStatusDao(LocationSpecificStatusDao locationSpecificStatusDao) {
        m_locationSpecificStatusDao = locationSpecificStatusDao;
    }

    /** {@inheritDoc} */
    @Override
    public LocationMonitorListModel getLocationMonitorDetails(LocationMonitorIdCommand cmd, BindingResult errors) {
        LocationMonitorListModel model = new LocationMonitorListModel();
        model.setErrors(errors);
        
        if (errors.getErrorCount() > 0) {
            return model;
        }
        
        OnmsMonitoringLocation def = m_monitoringLocationDao.get(cmd.getLocation());
        model.addLocationMonitor(new LocationMonitorModel(def));

        return model;
    }
}
