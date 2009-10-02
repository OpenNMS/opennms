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
       
        RegistrationListenerBean listener = new RegistrationListenerBean();
        listener.setServiceInterface(Hello.class);
        listener.setTarget(this);
        listener.setBindMethod("bind");
        listener.setUnbindMethod("unbind");
        listener.afterPropertiesSet();
        
        ServiceRegistry registry = new DefaultServiceRegistry();
        
        Registration reg1 = registry.register(new MyProvider("prov1"), Hello.class, Goodbye.class);
        Registration reg2 = registry.register(new MyProvider("prov2"), Hello.class, Goodbye.class);
        
        ReferenceListFactoryBean bean = new ReferenceListFactoryBean();
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
