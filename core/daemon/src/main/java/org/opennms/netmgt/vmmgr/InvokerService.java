/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vmmgr;

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectInstance;

import org.opennms.netmgt.config.service.Service;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
class InvokerService {
    private Service m_service;
    private ObjectInstance m_mbean;
    private Throwable m_badThrowable;

    /**
     * No public constructor.  Use @{link InvokerService#createServiceArray(Service[])}.
     */
    private InvokerService(Service service) {
        setService(service);
    }
    
    static List<InvokerService> createServiceList(Service[] services) {
        List<InvokerService> invokerServices = new ArrayList<InvokerService>(services.length);
        
        for (Service service : services) {
            invokerServices.add(new InvokerService(service));
        }
        
        return invokerServices;
    }
    
    void setBadThrowable(Throwable badThrowable) {
        m_badThrowable = badThrowable;
    }
    
    Throwable getBadThrowable() {
        return m_badThrowable;
    }
    
    ObjectInstance getMbean() {
        return m_mbean;
    }
    
    void setMbean(ObjectInstance mbean) {
        m_mbean = mbean;
    }
    
    Service getService() {
        return m_service;
    }
    
    private void setService(Service service) {
        m_service = service;
    }

    /**
     * <p>isBadService</p>
     *
     * @return a boolean.
     */
    public boolean isBadService() {
        return (m_badThrowable != null);
    }
}
