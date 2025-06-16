/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
