//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.poller;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;

/**
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ServiceConfig {
    
    private final Package m_pkg;
    private final Service m_service;
    private final PollOutagesConfig m_pollOutages;
    private final Map m_svcProperties;
    private String m_svcPropKey;

    public ServiceConfig(Package pkg, String svcName, PollOutagesConfig config) {
        m_pkg = pkg;
        m_pollOutages = config;
        Service svc = null;
        Enumeration esvc = getPackage().enumerateService();
        while (esvc.hasMoreElements()) {
            Service s = (Service) esvc.nextElement();
            if (s.getName().equalsIgnoreCase(svcName)) {
                svc = s;
                break;
            }
        }
        if (svc == null)
            throw new RuntimeException("Service name not part of package!");
        
        m_service = svc;
        
        m_svcProperties = createPropertyMap();
    }

    /**
     * @return
     */
    public Package getPackage() {
        return m_pkg;
    }
    
    public String getPackageName() {
        return getPackage().getName();
    }
    
    public Service getService() {
        return m_service;
    }
    
    public String getServiceName() {
        return getService().getName();
    }

    private Map createPropertyMap() {
        Map m = Collections.synchronizedMap(new TreeMap());
        Enumeration ep = getService().enumerateParameter();
        while (ep.hasMoreElements()) {
            Parameter p = (Parameter) ep.nextElement();
            m.put(p.getKey(), p.getValue());
        }
        return m;
    }
    
    public Map getPropertyMap() {
        return m_svcProperties;
    }


    public PollOutagesConfig getPollOutages() {
        return m_pollOutages;
    }

    boolean scheduledOutage(PollableService svc) {
        boolean outageFound = false;
    
        PollOutagesConfig outageFactory = getPollOutages();
    
        // Iterate over the outage names defined in the interface's package.
        // For each outage...if the outage contains a calendar entry which
        // applies to the current time and the outage applies to this
        // interface then break and return true. Otherwise process the
        // next outage.
        // 
        Iterator iter = getPackage().getOutageCalendarCollection().iterator();
        while (iter.hasNext()) {
            String outageName = (String) iter.next();
    
            // Does the outage apply to the current time?
            if (outageFactory.isCurTimeInOutage(outageName)) {
                // Does the outage apply to this interface?
    
                if ((outageFactory.isInterfaceInOutage(svc.getAddress().getHostAddress(), outageName)) || (outageFactory.isInterfaceInOutage("match-any", outageName))) {
                    if (ThreadCategory.getInstance(getClass()).isDebugEnabled())
                        ThreadCategory.getInstance(getClass()).debug("scheduledOutage: configured outage '" + outageName + "' applies, " + svc + " will not be polled.");
                    outageFound = true;
                    break;
                }
            }
        }
    
        return outageFound;
    }
}
