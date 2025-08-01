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
