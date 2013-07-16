/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.tip.ram;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;


/**
 * RestClientTest
 *
 * @author brozow
 */
public class RestClientTest {
    
    private Client m_client;
    private WebResource m_resource;
    
    @Before
    public void setUp() {
        ClientConfig config = new DefaultClientConfig();
        
        m_client = Client.create(config);
        m_client.addFilter(new HTTPBasicAuthFilter("demo", "demo"));
        m_resource = m_client.resource("http://demo.opennms.org/opennms/rest/alarms");

    }
    
    @Test
    public void testGetAllAlarms() {
        
        AlarmList alarms = m_resource.get(AlarmList.class);
        
        for (Alarm alarm:alarms) {
            System.err.println(alarm);
        }
        //System.err.println(alarms);
        
    }

}
