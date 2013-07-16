/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.validator;

import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.web.command.LocationMonitorIdCommand;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * <p>LocationMonitorIdValidator class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationMonitorIdValidator implements Validator, InitializingBean {
    private LocationMonitorDao m_locationMonitorDao;

    /** {@inheritDoc} */
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(LocationMonitorIdCommand.class);
    }

    /** {@inheritDoc} */
    @Override
    public void validate(Object obj, Errors errors) {
        LocationMonitorIdCommand cmd = (LocationMonitorIdCommand) obj;
        
        if (cmd.getMonitorId() == null) {
            errors.rejectValue("monitorId", "monitorId.notSpecified",
                               new Object[] { "monitorId" }, 
                               "Value required.");
        } else {
            try {
                int monitorId = cmd.getMonitorId();
                OnmsLocationMonitor monitor = m_locationMonitorDao.get(monitorId);
                if (monitor == null) {
                    throw new ObjectRetrievalFailureException(OnmsLocationMonitor.class, monitorId, "Could not find location monitor with id " + monitorId, null);
                }
            } catch (DataAccessException e) {
                errors.rejectValue("monitorId", "monitorId.notFound",
                                   new Object[] { "monitorId", cmd.getMonitorId() }, 
                                   "Valid location monitor ID required.");
                
            }
        }
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        if (m_locationMonitorDao == null) {
            throw new IllegalStateException("locationMonitorDao property not set");
        }
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

}
