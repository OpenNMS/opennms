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

package org.opennms.web.validator;

import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.web.svclayer.model.DistributedStatusDetailsCommand;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * <p>DistributedStatusDetailsValidator class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DistributedStatusDetailsValidator implements Validator, InitializingBean {
    
    private MonitoringLocationDao m_monitoringLocationDao;
    private LocationMonitorDao m_locationMonitorDao;
    private ApplicationDao m_applicationDao;

    /** {@inheritDoc} */
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(DistributedStatusDetailsCommand.class);
    }

    /** {@inheritDoc} */
    @Override
    public void validate(Object obj, Errors errors) {
        DistributedStatusDetailsCommand cmd = (DistributedStatusDetailsCommand) obj;
        
        if (cmd.getLocation() == null) {
            errors.rejectValue("location", "location.not-specified",
                               new Object[] { "location" }, 
                               "Value required.");
        } else {
            OnmsMonitoringLocation locationDef =
                m_monitoringLocationDao.get(cmd.getLocation());
            if (locationDef == null) {
                errors.rejectValue("location", "location.not-found",
                                   new Object[] { cmd.getLocation() },
                "Valid location definition required.");
            }
        }
          
        if (cmd.getApplication() == null) {
            errors.rejectValue("application", "application.not-specified",
                               new Object[] { "application" }, 
                               "Value required.");
        } else {
            OnmsApplication app =
                m_applicationDao.findByName(cmd.getApplication());
            if (app == null) {
                errors.rejectValue("application", "application.not-found",
                                   new Object[] { cmd.getApplication() },
                                   "Valid application required.");
            }
        }
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        if (m_applicationDao == null) {
            throw new IllegalStateException("applicationDao property not set");
        }
        if (m_locationMonitorDao == null) {
            throw new IllegalStateException("locationMonitorDao property not set");
        }
    }

    /**
     * <p>getApplicationDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.ApplicationDao} object.
     */
    public ApplicationDao getApplicationDao() {
        return m_applicationDao;
    }

    /**
     * <p>setApplicationDao</p>
     *
     * @param applicationDao a {@link org.opennms.netmgt.dao.api.ApplicationDao} object.
     */
    public void setApplicationDao(ApplicationDao applicationDao) {
        m_applicationDao = applicationDao;
    }

    /**
     * <p>getLocationMonitorDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.LocationMonitorDao} object.
     */
    public LocationMonitorDao getLocationMonitorDao() {
        return m_locationMonitorDao;
    }

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

}
