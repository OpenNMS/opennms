/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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
        assertNotNull(m_registry);
        
        assertSame(m_provider, m_registration.getProvider(Hello.class));
        assertSame(m_provider, m_registration.getProvider(Goodbye.class));
        
        assertEquals(1, m_registry.findProviders(Hello.class).size());
        assertEquals(1, m_registry.findProviders(Goodbye.class).size());
        
        assertSame(m_provider, m_registry.findProvider(Hello.class));
        assertSame(m_provider, m_registry.findProvider(Goodbye.class));
        
    }

}
