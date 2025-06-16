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
        
        ReferenceListFactoryBean<Hello> bean = new ReferenceListFactoryBean<>();
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
        
        ReferenceListFactoryBean<Hello> bean = new ReferenceListFactoryBean<>();
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
