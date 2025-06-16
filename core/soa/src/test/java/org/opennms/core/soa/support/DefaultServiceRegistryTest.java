/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
import org.opennms.core.soa.RegistrationHook;
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
        
        Set<T> m_providers = new LinkedHashSet<>();

        @Override
        public void providerRegistered(Registration registration, T provider) {
            m_providers.add(provider);
        }

        @Override
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
    
    public static class Hook implements RegistrationHook {
    	
    	private int m_registrationCount = 0;

		@Override
		public void registrationAdded(Registration registration) {
			m_registrationCount++;
		}

		@Override
		public void registrationRemoved(Registration registration) {
			m_registrationCount--;
		}

		public int getCount() {
			return m_registrationCount;
		}
    	
    }
    
    
    
    @Test
    public void testRegisterUnregister() {
        
        MyProvider provider = new MyProvider();
        
        
        Registration registration = m_registry.register(provider, Hello.class, Goodbye.class);
        
        Hook hook = new Hook();
        
        m_registry.addRegistrationHook(hook, true);
        
        assertEquals(1, hook.getCount());
        
        Collection<Hello> hellos = m_registry.findProviders(Hello.class);
        Collection<Goodbye> goodbyes = m_registry.findProviders(Goodbye.class);
        
        
        assertEquals(1, hellos.size());
        assertEquals(1, goodbyes.size());
        
        assertSame(provider, hellos.iterator().next());
        assertSame(provider, goodbyes.iterator().next());
        
        registration.unregister();
        
        assertEquals(0, hook.getCount());
        
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
        
        Hook hook = new Hook();
        
        m_registry.addRegistrationHook(hook, true);
        
        assertEquals(2, hook.getCount());

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
        
        assertEquals(1, hook.getCount());

        assertTrue(m_registry.findProviders(Hello.class, "(size=big)").isEmpty());
        assertEquals(1, m_registry.findProviders(Hello.class, "(size=small)").size());
        
        smallRegistration.unregister();
        
        assertEquals(0, hook.getCount());

        assertTrue(m_registry.findProviders(Hello.class, "(size=big)").isEmpty());
        assertTrue(m_registry.findProviders(Hello.class, "(size=small)").isEmpty());
        

        hellos = m_registry.findProviders(Hello.class);
        goodbyes = m_registry.findProviders(Goodbye.class);
        
        assertTrue(hellos.isEmpty());
        assertTrue(goodbyes.isEmpty());
        
    }
    
    @Test
    public void testRegistrationListener() {
        
        Listener<Hello> helloListener = new Listener<>();
        Listener<Goodbye> goodbyeListener = new Listener<>();
        
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
