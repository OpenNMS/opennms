package org.opennms.web.validator;

import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.web.command.LocationMonitorIdCommand;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class LocationMonitorIdValidator implements Validator, InitializingBean {
    private LocationMonitorDao m_locationMonitorDao;

    public boolean supports(Class clazz) {
        return clazz.equals(LocationMonitorIdCommand.class);
    }

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

    public void afterPropertiesSet() {
        if (m_locationMonitorDao == null) {
            throw new IllegalStateException("locationMonitorDao property not set");
        }
    }

    public LocationMonitorDao getLocationMonitorDao() {
        return m_locationMonitorDao;
    }

    public void setLocationMonitorDao(LocationMonitorDao locationMonitorDao) {
        m_locationMonitorDao = locationMonitorDao;
    }

}
