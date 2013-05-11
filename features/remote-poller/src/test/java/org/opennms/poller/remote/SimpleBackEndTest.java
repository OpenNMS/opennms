/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.poller.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/applicationContext-client.xml")
public class SimpleBackEndTest {
	@Resource(name="noAuthBean")
	private SimpleBackEnd m_noAuthBackEnd;
	
	@Resource(name="authBean")
	private SimpleBackEnd m_authBackEnd;
	
	@BeforeClass
	public static void setup() {
		MockLogAppender.setupLogging();
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
	}

	@Test
	@JUnitHttpServer(port=9162, webapps=@Webapp(context="/", path="src/test/resources/simple-test-webapp"))
	public void testBackend() throws Exception {
		assertNotNull(m_noAuthBackEnd);
		assertEquals("first get should be 0", 0, m_noAuthBackEnd.getCount());
		assertEquals("second should be 1", 1, m_noAuthBackEnd.getCount());
	}

	@Test
	@JUnitHttpServer(port=9162, basicAuth=true, webapps=@Webapp(context="/", path="src/test/resources/simple-test-webapp"))
	public void testBackendWithBasicAuth() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("testuser", "testpassword"));
		assertNotNull(m_authBackEnd);
		assertEquals("first get should be 0", 0, m_authBackEnd.getCount());
		assertEquals("second should be 1", 1, m_authBackEnd.getCount());
	}

	@Test
	@JUnitHttpServer(port=9162, basicAuth=true, webapps=@Webapp(context="/", path="src/test/resources/simple-test-webapp"))
	public void testBackendWithBasicAuthInDifferentThread() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("testuser", "testpassword"));
		assertNotNull(m_authBackEnd);
		
		final AtomicInteger first = new AtomicInteger(-1);
		final AtomicInteger second = new AtomicInteger(-1);
		
		Thread t = new Thread() {
                        @Override
			public void run() {
				first.set(m_authBackEnd.getCount());
				second.set(m_authBackEnd.getCount());
			}
		};
		t.start();
		t.join();
		assertEquals("first get should be 0", 0, first.get());
		assertEquals("second should be 1", 1, second.get());
	}
}
