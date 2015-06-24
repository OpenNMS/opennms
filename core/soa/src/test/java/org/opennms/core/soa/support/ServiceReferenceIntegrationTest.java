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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.soa.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ServiceReferenceIntegrationTest {
	
    @Autowired
    @Qualifier("reference")
	Hello m_hello;
	
	@Autowired
    @Qualifier("reference")
	Goodbye m_goodbye;
	
	@Autowired
	ServiceRegistry m_serviceRegistry;
	
	@Autowired
	MyProvider m_myProvider;
	
	
	@Test
	@DirtiesContext
	public void testWiring() throws IOException{
		
		assertNotNull(m_serviceRegistry);
        assertNotNull(m_hello);
        assertNotNull(m_goodbye);
		
		assertNotNull(m_myProvider);
		
		assertEquals(0, m_myProvider.helloSaid());

		m_hello.sayHello();
		
		int helloSaid = m_myProvider.helloSaid();

		assertEquals(1, helloSaid);
		
		assertEquals(0, m_myProvider.goodbyeSaid());

		m_goodbye.sayGoodbye();

		assertEquals(1, m_myProvider.goodbyeSaid());
		
        
	}
	
}
