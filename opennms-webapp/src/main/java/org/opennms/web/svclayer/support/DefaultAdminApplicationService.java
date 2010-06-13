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
 * 2007 Jul 24: Organize imports. - dj@opennms.org
 * 
 * Created: October 30, 2006
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.svclayer.AdminApplicationService;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultAdminApplicationService implements
        AdminApplicationService {
    
    private ApplicationDao m_applicationDao;
    private MonitoredServiceDao m_monitoredServiceDao;

    public ApplicationAndMemberServices getApplication(String applicationIdString) {
        if (applicationIdString == null) {
            throw new IllegalArgumentException("applicationIdString must not be null");
        }

        OnmsApplication application = findApplication(applicationIdString);
        
        Collection<OnmsMonitoredService> memberServices =
            m_monitoredServiceDao.findByApplication(application);
        for (OnmsMonitoredService service : memberServices) {
            m_applicationDao.initialize(service.getIpInterface());
            m_applicationDao.initialize(service.getIpInterface().getNode());
        }
        
        return new ApplicationAndMemberServices(application, memberServices);
    }

    public List<OnmsMonitoredService> findAllMonitoredServices() {
        List<OnmsMonitoredService> list =
            new ArrayList<OnmsMonitoredService>(m_monitoredServiceDao.findAll());
        Collections.sort(list);
        
        return list;
    }
    
    public EditModel findApplicationAndAllMonitoredServices(String applicationIdString) {
        ApplicationAndMemberServices app = getApplication(applicationIdString); 
        
        List<OnmsMonitoredService> monitoredServices =
            findAllMonitoredServices();
        return new EditModel(app.getApplication(), monitoredServices,
                             app.getMemberServices());
    }

    public ApplicationDao getApplicationDao() {
        return m_applicationDao;
    }

    public void setApplicationDao(ApplicationDao dao) {
        m_applicationDao = dao;
    }

    public MonitoredServiceDao getMonitoredServiceDao() {
        return m_monitoredServiceDao;
    }

    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
    }

    public void performEdit(String applicationIdString, String editAction,
            String[] toAdd, String[] toDelete) {
        if (applicationIdString == null) {
            throw new IllegalArgumentException("applicationIdString cannot be null");
        }
        if (editAction == null) {
            throw new IllegalArgumentException("editAction cannot be null");
        }
        
        OnmsApplication application = findApplication(applicationIdString); 
       
        if (editAction.contains("Add")) { // @i18n
            if (toAdd == null) {
                return;
                //throw new IllegalArgumentException("toAdd cannot be null if editAction is 'Add'");
            }
           
            for (String idString : toAdd) {
                Integer id;
                try {
                    id = WebSecurityUtils.safeParseInt(idString);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("toAdd element '"
                                                       + idString
                                                       + "' is not an integer");
                }
                OnmsMonitoredService service = m_monitoredServiceDao.get(id);
                if (service == null) {
                    throw new IllegalArgumentException("monitored service with "
                                                       + "id of " + id
                                                       + "could not be found");
                }
                if (service.getApplications().contains(application)) {
                    throw new IllegalArgumentException("monitored service with "
                                                       + "id of " + id
                                                       + "is already a member of "
                                                       + "application "
                                                       + application.getName());
                }
                
                service.addApplication(application);
                m_monitoredServiceDao.save(service);
            }
       } else if (editAction.contains("Remove")) { // @i18n
            if (toDelete == null) {
                return;
                //throw new IllegalArgumentException("toDelete cannot be null if editAction is 'Remove'");
            }
            
            for (String idString : toDelete) {
                Integer id;
                try {
                    id = WebSecurityUtils.safeParseInt(idString);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("toDelete element '"
                                                       + idString
                                                       + "' is not an integer");
                }
                OnmsMonitoredService service = m_monitoredServiceDao.get(id);
                if (service == null) {
                    throw new IllegalArgumentException("monitored service with "
                                                       + "id of " + id
                                                       + "could not be found");
                }
                if (!service.getApplications().contains(application)) {
                    throw new IllegalArgumentException("monitored service with "
                                                       + "id of " + id
                                                       + "is not a member of "
                                                       + "application "
                                                       + application.getName());
                }
                
                service.removeApplication(application);
                m_monitoredServiceDao.save(service);
            }

            m_applicationDao.save(application);
       } else {
           throw new IllegalArgumentException("editAction of '"
                                              + editAction
                                              + "' is not allowed");
       }
    }

    public OnmsApplication addNewApplication(String name) {
        OnmsApplication application = new OnmsApplication();
        application.setName(name);
        m_applicationDao.save(application);
        return application;
    }

    public List<OnmsApplication> findAllApplications() {
        Collection<OnmsApplication> applications = m_applicationDao.findAll();
        List<OnmsApplication> sortedApplications =
            new ArrayList<OnmsApplication>(applications);
        Collections.sort(sortedApplications);

        return sortedApplications;
    }

    public void removeApplication(String applicationIdString) {
        OnmsApplication application = findApplication(applicationIdString);
        m_applicationDao.delete(application);
    }

    public List<OnmsApplication> findByMonitoredService(int id) {
        OnmsMonitoredService service = m_monitoredServiceDao.get(id);
        if (service == null) {
            throw new IllegalArgumentException("monitored service with "
                                               + "id of " + id
                                               + " could not be found");
        }
        
        List<OnmsApplication> sortedApplications =
            new ArrayList<OnmsApplication>(service.getApplications());
        Collections.sort(sortedApplications);
        
        return sortedApplications;
    }

    public void performServiceEdit(String ifServiceIdString, String editAction,
            String[] toAdd, String[] toDelete) {
        if (ifServiceIdString == null) {
            throw new IllegalArgumentException("ifServiceIdString cannot be null");
        }
        if (editAction == null) {
            throw new IllegalArgumentException("editAction cannot be null");
        }
        
        OnmsMonitoredService service = findService(ifServiceIdString);

        if (editAction.contains("Add")) { // @i18n
            if (toAdd == null) {
                return;
                //throw new IllegalArgumentException("toAdd cannot be null if editAction is 'Add'");
            }
           
            for (String idString : toAdd) {
                Integer id;
                try {
                    id = WebSecurityUtils.safeParseInt(idString);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("toAdd element '"
                                                       + idString
                                                       + "' is not an integer");
                }
                OnmsApplication application = m_applicationDao.get(id);
                if (application == null) {
                    throw new IllegalArgumentException("application with "
                                                       + "id of " + id
                                                       + " could not be found");
                }
                if (service.getApplications().contains(application)) {
                    throw new IllegalArgumentException("application with "
                                                       + "id of " + id
                                                       + " is already a member of "
                                                       + "service "
                                                       + service.getServiceName());
                }
                service.getApplications().add(application);
            }
            
            m_monitoredServiceDao.save(service);
       } else if (editAction.contains("Remove")) { // @i18n
            if (toDelete == null) {
                return;
                //throw new IllegalArgumentException("toDelete cannot be null if editAction is 'Remove'");
            }
            
            for (String idString : toDelete) {
                Integer id;
                try {
                    id = WebSecurityUtils.safeParseInt(idString);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("toDelete element '"
                                                       + idString
                                                       + "' is not an integer");
                }
                OnmsApplication application = m_applicationDao.get(id);
                if (application == null) {
                    throw new IllegalArgumentException("application with "
                                                       + "id of " + id
                                                       + " could not be found");
                }
                if (!service.getApplications().contains(application)) {
                    throw new IllegalArgumentException("application with "
                                                       + "id of " + id
                                                       + " is not a member of "
                                                       + "service "
                                                       + service.getServiceName());
                }
                service.getApplications().add(application);
            }

            m_monitoredServiceDao.save(service);
       } else {
           throw new IllegalArgumentException("editAction of '"
                                              + editAction
                                              + "' is not allowed");
       }
    }


    public ServiceEditModel findServiceApplications(String ifServiceIdString) {
        if (ifServiceIdString == null) {
            throw new IllegalArgumentException("ifServiceIdString must not be null");
        }

        OnmsMonitoredService service = findService(ifServiceIdString);
        List<OnmsApplication> applications = findAllApplications();
        
        m_monitoredServiceDao.initialize(service.getIpInterface());
        m_monitoredServiceDao.initialize(service.getIpInterface().getNode());
        
        return new ServiceEditModel(service, applications);
    }

    public OnmsApplication findApplication(String name) {
        int applicationId = -1;
        try {
            applicationId = WebSecurityUtils.safeParseInt(name);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("parameter 'applicationid' "
                                               + "with value '"
                                               + name
                                               + "' could not be parsed "
                                               + "as an integer");
        }

        OnmsApplication application = m_applicationDao.get(applicationId);
        if (application == null) {
            throw new IllegalArgumentException("Could not find application "
                                               + "with application ID "
                                               + applicationId);
        }
        return application;
        }



    private OnmsMonitoredService findService(String ifServiceIdString) {
        int ifServiceId;
        
        try {
            ifServiceId = WebSecurityUtils.safeParseInt(ifServiceIdString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("parameter ifserviceid '"
                                               + ifServiceIdString
                                               + "' is not an integer");
        }
        
        OnmsMonitoredService service = m_monitoredServiceDao.get(ifServiceId);
        if (service == null) {
            throw new IllegalArgumentException("monitored service with "
                                               + "id of " + ifServiceId
                                               + " could not be found");
        }
        
        return service;
    }

    public class ApplicationAndMemberServices {
        private OnmsApplication m_application;
        private Collection<OnmsMonitoredService> m_memberServices;

        public ApplicationAndMemberServices(OnmsApplication application,
                Collection<OnmsMonitoredService> memberServices) {
            m_application = application;
            m_memberServices = memberServices;
        }

        public OnmsApplication getApplication() {
            return m_application;
        }

        public Collection<OnmsMonitoredService> getMemberServices() {
            return m_memberServices;
        }
    }

    public class EditModel {
        private OnmsApplication m_application;
        private List<OnmsMonitoredService> m_monitoredServices;
        private List<OnmsMonitoredService> m_sortedMemberServices;

        public EditModel(OnmsApplication application,
                List<OnmsMonitoredService> monitoredServices,
                Collection<OnmsMonitoredService> memberServices) {
            m_application = application;
            m_monitoredServices = monitoredServices;
            
            m_monitoredServices.removeAll(memberServices);
            
            m_sortedMemberServices =
                new ArrayList<OnmsMonitoredService>(memberServices);
            Collections.sort(m_sortedMemberServices);
        }

        public OnmsApplication getApplication() {
            return m_application;
        }

        public List<OnmsMonitoredService> getMonitoredServices() {
            return m_monitoredServices;
        }

        public List<OnmsMonitoredService> getSortedMemberServices() {
            return m_sortedMemberServices;
        }
        
    }


    public class ServiceEditModel {
        private OnmsMonitoredService m_service;
        private List<OnmsApplication> m_applications;
        private List<OnmsApplication> m_sortedApplications;

        public ServiceEditModel(OnmsMonitoredService service, List<OnmsApplication> applications) {
            m_service = service;
            m_applications = applications;
            
            for (OnmsApplication application : service.getApplications()) {
                m_applications.remove(application);
            }
            
            m_sortedApplications =
                new ArrayList<OnmsApplication>(m_service.getApplications());
            Collections.sort(m_sortedApplications);
        }
        
        public OnmsMonitoredService getService() {
            return m_service;
        }

        public List<OnmsApplication> getApplications() {
            return m_applications;
        }

        public List<OnmsApplication> getSortedApplications() {
            return m_sortedApplications;
        }
        
    }

}
