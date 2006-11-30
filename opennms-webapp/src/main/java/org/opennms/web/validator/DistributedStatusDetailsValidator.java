package org.opennms.web.validator;

import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.web.command.DistributedStatusDetailsCommand;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class DistributedStatusDetailsValidator implements Validator {
    
    private LocationMonitorDao m_locationMonitorDao;
    private ApplicationDao m_applicationDao;

    public boolean supports(Class clazz) {
        return clazz.equals(DistributedStatusDetailsCommand.class);
    }

    public void validate(Object obj, Errors errors) {
        assertPropertiesSet();

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

    private void assertPropertiesSet() {
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
