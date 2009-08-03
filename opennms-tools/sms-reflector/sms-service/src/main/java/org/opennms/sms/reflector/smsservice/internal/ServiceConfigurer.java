/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.sms.reflector.smsservice.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServiceConfigurer
 *
 * @author brozow
 */
public class ServiceConfigurer implements ManagedServiceFactory {
    
    public static Logger log = LoggerFactory.getLogger(ServiceConfigurer.class);
    
    private BundleContext m_context;
    private Map<String, ServiceRegistration> m_servicesByPID = new HashMap<String, ServiceRegistration>();


    public ServiceConfigurer(BundleContext context) {
        
        try {
            
            m_context = context;

        } catch(Exception e) {
            e.printStackTrace();
        }

        debugf("Constructor called");
    }

    public String getName() {
        return "Factory for creating org.smslib.Service 'services'";
    }

    public void updated(String pid, Dictionary properties) throws ConfigurationException {
        
        debugf("XXXXXXXXXXXXXXX updated("+pid+", "+properties+")");
    }

    public void deleted(String pid) {
        debugf("XXXXXXXXXXXXXXX deleted("+pid+")");
    }
    
    private void debugf(String fmt, Object... args) {
        log.debug(String.format(fmt, args));
    }

}
