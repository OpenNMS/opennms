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
package org.opennms.web.validator;

import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.web.command.DistributedStatusDetailsCommand;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DistributedStatusDetailsValidator implements Validator, InitializingBean {
    
    private LocationMonitorDao m_locationMonitorDao;
    private ApplicationDao m_applicationDao;

    public boolean supports(Class clazz) {
        return clazz.equals(DistributedStatusDetailsCommand.class);
    }

    public void validate(Object obj, Errors errors) {
        DistributedStatusDetailsCommand cmd = (DistributedStatusDetailsCommand) obj;
        
        if (cmd.getLocation() == null) {
            errors.rejectValue("location", "location.not-specified",
                               new Object[] { "location" }, 
                               "Value required.");
        } else {
            OnmsMonitoringLocationDefinition locationDef =
                m_locationMonitorDao.findMonitoringLocationDefinition(cmd.getLocation());
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

    public void afterPropertiesSet() {
        if (m_applicationDao == null) {
            throw new IllegalStateException("applicationDao property not set");
        }
        if (m_locationMonitorDao == null) {
            throw new IllegalStateException("locationMonitorDao property not set");
        }
    }

    public ApplicationDao getApplicationDao() {
        return m_applicationDao;
    }

    public void setApplicationDao(ApplicationDao applicationDao) {
        m_applicationDao = applicationDao;
    }

    public LocationMonitorDao getLocationMonitorDao() {
        return m_locationMonitorDao;
    }

    public void setLocationMonitorDao(LocationMonitorDao locationMonitorDao) {
        m_locationMonitorDao = locationMonitorDao;
    }

}
