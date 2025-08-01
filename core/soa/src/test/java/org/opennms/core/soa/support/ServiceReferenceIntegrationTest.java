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
