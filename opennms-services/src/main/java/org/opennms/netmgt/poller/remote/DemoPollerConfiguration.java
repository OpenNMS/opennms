/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.poller.remote;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DemoPollerConfiguration implements PollerConfiguration {
    
    Date m_timestamp;
    PolledService[] m_polledServices;
    
    DemoPollerConfiguration(Date timestamp) {
        m_timestamp = timestamp;
        
        OnmsServiceType http = new OnmsServiceType("HTTP");
        
        List<PolledService> polledServices = new ArrayList<PolledService>();
        
        OnmsDistPoller distPoller = new OnmsDistPoller("locahost", "127.0.0.1");
        NetworkBuilder m_builder = new NetworkBuilder(distPoller);
        m_builder.addNode("Google").setId(1);
        m_builder.addInterface("64.233.161.99").setId(11);
        polledServices.add(createPolledService(111, m_builder.addService(http), new HashMap<String,Object>(), 3000));
        m_builder.addInterface("64.233.161.104").setId(12);
        polledServices.add(createPolledService(121, m_builder.addService(http), new HashMap<String,Object>(), 3000));
        m_builder.addNode("OpenNMS").setId(2);
        m_builder.addInterface("209.61.128.9").setId(21);
        polledServices.add(createPolledService(211, m_builder.addService(http), new HashMap<String,Object>(), 3000));
        
        m_polledServices = (PolledService[]) polledServices.toArray(new PolledService[polledServices.size()]);
        
    }
    
    DemoPollerConfiguration() {
        this(new Date());
    }
	
    public Date getConfigurationTimestamp() {
        return m_timestamp;
    }

    public PolledService[] getPolledServices() {
        return m_polledServices;
    }

    private PolledService createPolledService(int serviceID, OnmsMonitoredService service, Map<String,Object> monitorConfiguration, long interval) {
        service.setId(serviceID);
        return new PolledService(service, monitorConfiguration, new OnmsPollModel(interval));
    }
    
    public int getFirstId() {
        return getFirstService().getServiceId();
    }

    public PolledService getFirstService() {
        return m_polledServices[0];
    }

  

}
