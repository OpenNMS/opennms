/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 11, 2007
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
package org.opennms.web.validator;

import org.opennms.netmgt.dao.LocationMonitorDao;
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
 * @since 1.6.12
 */
public class LocationMonitorIdValidator implements Validator, InitializingBean {
    private LocationMonitorDao m_locationMonitorDao;

    /** {@inheritDoc} */
    public boolean supports(Class clazz) {
        return clazz.equals(LocationMonitorIdCommand.class);
    }

    /** {@inheritDoc} */
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
    public void afterPropertiesSet() {
        if (m_locationMonitorDao == null) {
            throw new IllegalStateException("locationMonitorDao property not set");
        }
    }

    /**
     * <p>getLocationMonitorDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.LocationMonitorDao} object.
     */
    public LocationMonitorDao getLocationMonitorDao() {
        return m_locationMonitorDao;
    }

    /**
     * <p>setLocationMonitorDao</p>
     *
     * @param locationMonitorDao a {@link org.opennms.netmgt.dao.LocationMonitorDao} object.
     */
    public void setLocationMonitorDao(LocationMonitorDao locationMonitorDao) {
        m_locationMonitorDao = locationMonitorDao;
    }

}
