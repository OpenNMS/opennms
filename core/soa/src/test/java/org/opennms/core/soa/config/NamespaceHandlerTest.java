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
package org.opennms.core.soa.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
    
    @Resource(name="simple")
    Registration m_simpleRegistration;
    
    @Resource(name="nested")
    Registration m_nestedRegistration;
    
    @Resource(name="hello")
    Hello m_hello;
    
	@Resource(name="helloList")
	List<Object> m_helloList;
	
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
    public void testReferenceBeanDefinition() throws IOException{

        assertNotNull(m_hello);
    	
    	int expected = m_provider.helloSaid() + 1;
    	
    	m_hello.sayHello();

		assertEquals(expected, m_provider.helloSaid());
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
