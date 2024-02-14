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
package org.opennms.core.soa.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.soa.support.Goodbye;
import org.opennms.core.soa.support.Hello;
import org.opennms.core.soa.support.HelloListListener;
import org.opennms.core.soa.support.MyProvider;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * NamespaceHandlerTest
 *
 * @author brozow
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class NamespaceHandlerTest {
    
    @Resource(name="myProvider")
    MyProvider m_provider;
    
    @Resource(name="bigProvider")
    MyProvider m_bigProvider;

    @Resource(name="smallProvider")
    MyProvider m_smallProvider;

    @Resource(name="simple")
    Registration m_simpleRegistration;
    
    @Resource(name="nested")
    Registration m_nestedRegistration;
    
    @Resource(name="big")
    Registration m_bigRegistration;

    @Resource(name="small")
    Registration m_smallRegistration;
    
    @Resource(name="hello")
    Hello m_hello;
    
    @Resource(name="bigGoodbye")
    Goodbye m_bigGoodbye;
    
    @Resource(name="smallGoodbye")
    Goodbye m_smallGoodbye;
    
    @Resource(name="helloList")
    List<Hello> m_helloList;
    
    @Resource(name="bigGoodbyeList")
    List<Goodbye> m_bigGoodbyeList;
    
	@Resource(name="serviceRegistry")
	ServiceRegistry m_defaultServiceRegistry;
	
	@Resource(name="helloListListener")
	HelloListListener m_helloListListener;
    
    @Test
    @DirtiesContext
    public void testInjected() {
        
        assertEquals(m_provider, m_simpleRegistration.getProvider(Hello.class));
        
        assertContains(m_simpleRegistration.getProvidedInterfaces(), Hello.class);
        
        assertEquals(m_provider, m_nestedRegistration.getProvider(Hello.class));
        
        assertContains(m_nestedRegistration.getProvidedInterfaces(), Hello.class, Goodbye.class);
        
    }
    
    @Test
    @DirtiesContext
    public void testServiceProperties() {
        
        assertNotNull(m_bigRegistration);
        assertNotNull(m_bigRegistration.getProperties());
        assertEquals("big", m_bigRegistration.getProperties().get("size"));

        assertNotNull(m_smallRegistration);
        assertNotNull(m_smallRegistration.getProperties());
        assertEquals("small", m_smallRegistration.getProperties().get("size"));
}
    
    @Test
    @DirtiesContext
    public void testReferenceBeanDefinition() throws IOException{

        assertNotNull(m_hello);
        assertEquals("provider", m_hello.toString());
        
        int expected = m_provider.helloSaid() + 1;
        
        m_hello.sayHello();

        assertEquals(expected, m_provider.helloSaid());
    }
    
    @Test
    @DirtiesContext
    public void testFilteredReferenceBeanDefinition() throws IOException{

        assertNotNull(m_bigGoodbye);
        assertEquals("big", m_bigGoodbye.toString());
        
        int bigExpected = m_bigProvider.goodbyeSaid() + 1;
        
        m_bigGoodbye.sayGoodbye();
        
        assertEquals(bigExpected, m_bigProvider.goodbyeSaid());

        assertNotNull(m_smallGoodbye);
        assertEquals("small", m_smallGoodbye.toString());
        
        int smallExpected = m_smallProvider.goodbyeSaid() + 1;
        
        m_smallGoodbye.sayGoodbye();

        assertEquals(smallExpected, m_smallProvider.goodbyeSaid());
    }
    
    @Test
    @DirtiesContext
    public void testReferenceListBeanDefinition() {
 
        assertNotNull(m_helloList);
        
        int expected = m_helloList.size() + 1;
 
        Registration registration = m_defaultServiceRegistry.register(new MyProvider(), Hello.class);
        
        assertEquals(expected, m_helloList.size());
        
        expected = m_helloList.size() - 1;

        registration.unregister();
        
        assertEquals(expected, m_helloList.size());
        
    }
    
    @Test
    @DirtiesContext
    public void testFilteredReferenceListBeanDefinition() {
 
        assertNotNull(m_bigGoodbyeList);
        
        int expected = m_bigGoodbyeList.size() + 1;
 
        Map<String, String> bigProps = new HashMap<String, String>();
        bigProps.put("size", "big");
        Registration bigRegistration = m_defaultServiceRegistry.register(new MyProvider("alsoBig"), bigProps, Goodbye.class);

        Map<String, String> props = new HashMap<String, String>();
        props.put("size", "small");
        Registration smallRegistration = m_defaultServiceRegistry.register(new MyProvider("alsoSmall"), props, Goodbye.class);
        
        assertEquals(expected, m_bigGoodbyeList.size());
        
        expected = m_bigGoodbyeList.size() - 1;

        bigRegistration.unregister();
        smallRegistration.unregister();
        
        assertEquals(expected, m_bigGoodbyeList.size());
        
    }
    
    @Test
    @DirtiesContext
    public void testRegistrationListenerBeanDefinition() {

        assertNotNull(m_helloListListener);
    	
    	MyProvider myProvider = new MyProvider();
    	
    	int expected = m_helloListListener.getTotalProviders() + 1;
    	
    	Registration registration = m_defaultServiceRegistry.register(myProvider, Hello.class);
    	
    	assertEquals(expected, m_helloListListener.getTotalProviders());
    	
    	expected = m_helloListListener.getTotalProviders() - 1;
    	
    	registration.unregister();
    	
    	assertEquals(expected, m_helloListListener.getTotalProviders());
    	
    }
    
    private void assertContains(Class<?>[] provided, Class<?>... expected) {

        Set<Class<?>> actual = new HashSet<Class<?>>(Arrays.asList(provided));
        Set<Class<?>> expect = new HashSet<Class<?>>(Arrays.asList(expected));
        
        assertEquals(actual, expect);
        
    }

}
