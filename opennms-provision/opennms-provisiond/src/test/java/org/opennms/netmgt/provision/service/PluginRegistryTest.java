/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.provision.service;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.NodePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * PluginRegistryTest
 *
 * @author brozow
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= { "classpath:/pluginRegistryTest-context.xml" } )
public class PluginRegistryTest {
    
    @Autowired
    ApplicationContext m_appContext;
    
    @Autowired
    PluginRegistry m_pluginRegistry;
    
    interface BeanMatcher<T> {
        boolean matches(T t);
    }
    
    @Test
    public void testGo() {
        
        Collection<NodePolicy> nodePolicies = m_pluginRegistry.getAllPlugins(NodePolicy.class);
        
        Collection<IpInterfacePolicy> ifPolicies = m_pluginRegistry.getAllPlugins(IpInterfacePolicy.class);
        

        assertEquals(3, nodePolicies.size());
        assertEquals(1, ifPolicies.size());
        
    }
    
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return (Map<String, T>)m_appContext.getBeansOfType(clazz, true, true);
    }
    
}
