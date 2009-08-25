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
package org.opennms.core.soa.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * ServiceRegistrationBeanTest
 *
 * @author brozow
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ServiceFactoryBeanTest {
    
    @Autowired
    MyProvider m_provider;
    
    @Autowired
    Registration m_registration;
    
    @Autowired
    ServiceRegistry m_registry;
    
    @Test
    @DirtiesContext
    public void testRegistration() {
        
        assertNotNull(m_provider);
        assertNotNull(m_registration);
        
        assertSame(m_provider, m_registration.getProvider(Hello.class));
        assertSame(m_provider, m_registration.getProvider(Goodbye.class));
        
        assertEquals(1, m_registry.findProviders(Hello.class).size());
        assertEquals(1, m_registry.findProviders(Goodbye.class).size());
        
        assertSame(m_provider, m_registry.findProvider(Hello.class));
        assertSame(m_provider, m_registry.findProvider(Goodbye.class));
        
    }

}
