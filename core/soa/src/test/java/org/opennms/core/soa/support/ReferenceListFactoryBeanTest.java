/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.soa.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.opennms.core.soa.Registration;
import org.opennms.core.soa.RegistrationListener;
import org.opennms.core.soa.ServiceRegistry;


/**
 * ReferenceListFactoryBeanTest
 *
 * @author brozow
 */
public class ReferenceListFactoryBeanTest {
    
    /**
     * RegistrationListenerImplementation
     *
     * @author brozow
     */
    private final class CountingListener implements RegistrationListener<Hello> {
        private int m_totalProvided = 0;

        @Override
        public void providerRegistered(Registration registration, Hello provider) {
            m_totalProvided++;
        }

        @Override
        public void providerUnregistered(Registration registration, Hello provider) {
            m_totalProvided--;
        }
        
        public int getTotalProvided() {
            return m_totalProvided;
        }
    }

    @Test
    public void testDynamicList() throws Exception {
        
        ServiceRegistry registry = new DefaultServiceRegistry();
        
        
        Registration reg1 = registry.register(new MyProvider("prov1"), Hello.class, Goodbye.class);
        Registration reg2 = registry.register(new MyProvider("prov2"), Hello.class, Goodbye.class);
        
        ReferenceListFactoryBean<Hello> bean = new ReferenceListFactoryBean<Hello>();
        bean.setServiceInterface(Hello.class);
        bean.setServiceRegistry(registry);
        bean.afterPropertiesSet();
        
        List<Hello> helloList = getObject(bean);
        
        assertEquals(2, helloList.size());
        
        Registration reg3 = registry.register(new MyProvider("prov3"), Hello.class, Goodbye.class);
        
        assertEquals(3, helloList.size());
        
        reg2.unregister();
        
        assertEquals(2, helloList.size());
        
        reg1.unregister();
        reg3.unregister();
        
        assertTrue(helloList.isEmpty());
        
    }

    private List<Hello> getObject(ReferenceListFactoryBean<Hello> bean) throws Exception {
        return bean.getObject();
    }
    
    @Test
    public void testListListeners() throws Exception {
        
        ServiceRegistry registry = new DefaultServiceRegistry();
        
        Registration reg1 = registry.register(new MyProvider("prov1"), Hello.class, Goodbye.class);
        Registration reg2 = registry.register(new MyProvider("prov2"), Hello.class, Goodbye.class);
        
        ReferenceListFactoryBean<Hello> bean = new ReferenceListFactoryBean<Hello>();
        bean.setServiceInterface(Hello.class);
        bean.setServiceRegistry(registry);
        
        CountingListener listener = new CountingListener();

        bean.addListener(listener);

        bean.afterPropertiesSet();
        
        assertEquals(2, listener.getTotalProvided());
        
        Registration reg3 = registry.register(new MyProvider("prov3"), Hello.class, Goodbye.class);
        
        assertEquals(3, listener.getTotalProvided());
        
        reg2.unregister();
        
        assertEquals(2, listener.getTotalProvided());
        
        reg1.unregister();
        reg3.unregister();
        
        assertEquals(0, listener.getTotalProvided());
    }

}
