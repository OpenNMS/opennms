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
