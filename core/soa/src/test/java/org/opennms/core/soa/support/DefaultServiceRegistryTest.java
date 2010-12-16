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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.opennms.core.soa.Registration;
import org.opennms.core.soa.RegistrationListener;
import org.opennms.core.soa.ServiceRegistry;



/**
 * DefaultServiceRegistryTest
 *
 * @author brozow
 */
public class DefaultServiceRegistryTest {
    
    private ServiceRegistry m_registry = new DefaultServiceRegistry();
    
    public static class Listener<T> implements RegistrationListener<T> {
        
        Set<T> m_providers = new LinkedHashSet<T>();

        public void providerRegistered(Registration registration, T provider) {
            m_providers.add(provider);
        }

        public void providerUnregistered(Registration registration, T provider) {
            m_providers.remove(provider);
        }
        
        public int size() {
            return m_providers.size();
        }
        
        public boolean contains(T provider) {
           return m_providers.contains(provider);
        }
        
    }
    
    
    
    @Test
    public void testRegisterUnregister() {
        
        MyProvider provider = new MyProvider();
        
        
        Registration registration = m_registry.register(provider, Hello.class, Goodbye.class);
        
        
        Collection<Hello> hellos = m_registry.findProviders(Hello.class);
        Collection<Goodbye> goodbyes = m_registry.findProviders(Goodbye.class);
        
        assertEquals(1, hellos.size());
        assertEquals(1, goodbyes.size());
        
        assertSame(provider, hellos.iterator().next());
        assertSame(provider, goodbyes.iterator().next());
        
        registration.unregister();
        
        hellos = m_registry.findProviders(Hello.class);
        goodbyes = m_registry.findProviders(Goodbye.class);
        
        assertTrue(hellos.isEmpty());
        assertTrue(goodbyes.isEmpty());
        
    }
    
    @Test
    public void testRegisterUnregisterUsingFilters() {
        
        MyProvider bigProvider = new MyProvider();
        MyProvider smallProvider = new MyProvider();
        
        Map<String, String> bigProps = new HashMap<String, String>();
        bigProps.put("size", "big");

        Map<String, String> smallProps = new HashMap<String, String>();
        smallProps.put("size", "small");

        Registration bigRegistration = m_registry.register(bigProvider, bigProps, Hello.class, Goodbye.class);
        Registration smallRegistration = m_registry.register(smallProvider, smallProps, Hello.class, Goodbye.class);
        
        Collection<Hello> hellos = m_registry.findProviders(Hello.class);
        Collection<Goodbye> goodbyes = m_registry.findProviders(Goodbye.class);
        
        assertEquals(2, hellos.size());
        assertEquals(2, goodbyes.size());
        
        Collection<Hello> bigHellos = m_registry.findProviders(Hello.class, "(size=big)");
        Collection<Goodbye> bigGoodbyes = m_registry.findProviders(Goodbye.class, "(size=big)");
        
        assertEquals(1, bigHellos.size());
        assertEquals(1, bigGoodbyes.size());
        
        assertSame(bigProvider, bigHellos.iterator().next());
        assertSame(bigProvider, bigGoodbyes.iterator().next());
        
        Collection<Hello> smallHellos = m_registry.findProviders(Hello.class, "(size=small)");
        Collection<Goodbye> smallGoodbyes = m_registry.findProviders(Goodbye.class, "(size=small)");
        
        assertEquals(1, smallHellos.size());
        assertEquals(1, smallGoodbyes.size());
        
        assertSame(smallProvider, smallHellos.iterator().next());
        assertSame(smallProvider, smallGoodbyes.iterator().next());
        
        bigRegistration.unregister();
        
        assertTrue(m_registry.findProviders(Hello.class, "(size=big)").isEmpty());
        assertEquals(1, m_registry.findProviders(Hello.class, "(size=small)").size());
        
        smallRegistration.unregister();
        
        assertTrue(m_registry.findProviders(Hello.class, "(size=big)").isEmpty());
        assertTrue(m_registry.findProviders(Hello.class, "(size=small)").isEmpty());
        

        hellos = m_registry.findProviders(Hello.class);
        goodbyes = m_registry.findProviders(Goodbye.class);
        
        assertTrue(hellos.isEmpty());
        assertTrue(goodbyes.isEmpty());
        
    }
    
    @Test
    public void testRegistrationListener() {
        
        Listener<Hello> helloListener = new Listener<Hello>();
        Listener<Goodbye> goodbyeListener = new Listener<Goodbye>();
        
        m_registry.addListener(Hello.class, helloListener);
        m_registry.addListener(Goodbye.class, goodbyeListener);
        
        MyProvider provider = new MyProvider();
        
        Registration registration = m_registry.register(provider, Hello.class, Goodbye.class);
        
        assertEquals(1, helloListener.size());
        assertEquals(1, goodbyeListener.size());
        
        assertTrue(helloListener.contains(provider));
        assertTrue(goodbyeListener.contains(provider));
        
        registration.unregister();
        
        assertEquals(0, helloListener.size());
        assertEquals(0, goodbyeListener.size());
    }

}
