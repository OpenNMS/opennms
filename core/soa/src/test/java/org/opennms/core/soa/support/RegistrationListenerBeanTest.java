/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

import java.util.Map;

import org.junit.Test;
import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;


/**
 * RegistrationListenerBeanTest
 *
 * @author brozow
 */
public class RegistrationListenerBeanTest {
    
    private int m_totalProvided = 0;
    
    @Test
    public void testCallBindUnbindMethods() throws Exception {
       
        RegistrationListenerBean<Hello> listener = new RegistrationListenerBean<>();
        listener.setServiceInterface(Hello.class);
        listener.setTarget(this);
        listener.setBindMethod("bind");
        listener.setUnbindMethod("unbind");
        listener.afterPropertiesSet();
        
        ServiceRegistry registry = new DefaultServiceRegistry();
        
        Registration reg1 = registry.register(new MyProvider("prov1"), Hello.class, Goodbye.class);
        Registration reg2 = registry.register(new MyProvider("prov2"), Hello.class, Goodbye.class);
        
        ReferenceListFactoryBean<Hello> bean = new ReferenceListFactoryBean<>();
        bean.setServiceInterface(Hello.class);
        bean.setServiceRegistry(registry);
        
        bean.addListener(listener);

        bean.afterPropertiesSet();
        
        assertEquals(2, getTotalProvided());
        
        Registration reg3 = registry.register(new MyProvider("prov3"), Hello.class, Goodbye.class);
        
        assertEquals(3, getTotalProvided());
        
        reg2.unregister();
        
        assertEquals(2, getTotalProvided());
        
        reg1.unregister();
        reg3.unregister();
        
        assertEquals(0, getTotalProvided());

        
        
    }
    
    public int getTotalProvided() {
        return m_totalProvided;
    }
    
    public void bind(Hello hello, Map<String, String> properties) {
        m_totalProvided++;
    }
    
    public void unbind(Hello hello, Map<String, String> properties) {
        m_totalProvided--;
    }

}
