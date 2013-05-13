/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
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
 * <p>DemoPollerConfiguration class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DemoPollerConfiguration implements PollerConfiguration {
    
    Date m_timestamp;
    PolledService[] m_polledServices;
    private long m_serverTime = 0;
    
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
	
    /**
     * <p>getConfigurationTimestamp</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Override
    public Date getConfigurationTimestamp() {
        return m_timestamp;
    }

    /**
     * <p>getPolledServices</p>
     *
     * @return an array of {@link org.opennms.netmgt.poller.remote.PolledService} objects.
     */
    @Override
    public PolledService[] getPolledServices() {
        return m_polledServices;
    }

    private PolledService createPolledService(int serviceID, OnmsMonitoredService service, Map<String,Object> monitorConfiguration, long interval) {
        service.setId(serviceID);
        return new PolledService(service, monitorConfiguration, new OnmsPollModel(interval));
    }
    
    /**
     * <p>getFirstId</p>
     *
     * @return a int.
     */
    public int getFirstId() {
        return getFirstService().getServiceId();
    }

    /**
     * <p>getFirstService</p>
     *
     * @return a {@link org.opennms.netmgt.poller.remote.PolledService} object.
     */
    public PolledService getFirstService() {
        return m_polledServices[0];
    }

    /**
     * @param serverTime the serverTime to set
     */
    public void setServerTime(long serverTime) {
        m_serverTime = serverTime;
    }

    /**
     * @return the serverTime
     */
    @Override
    public long getServerTime() {
        return m_serverTime;
    }

  

}
