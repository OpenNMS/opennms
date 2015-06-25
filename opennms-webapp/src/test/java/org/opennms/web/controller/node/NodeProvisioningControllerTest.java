/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.node;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.provision.persist.DefaultNodeProvisionService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.api.Authentication;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
		"classpath:/org/opennms/web/rest/applicationContext-test.xml",
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-mockDao.xml",
		"classpath*:/META-INF/opennms/component-service.xml",
		"classpath*:/META-INF/opennms/component-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-reportingCore.xml",
		"classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
		"classpath:/org/opennms/web/svclayer/applicationContext-svclayer.xml",
		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		"classpath:/META-INF/opennms/applicationContext-reporting.xml",
		"classpath:/META-INF/opennms/applicationContext-mock-usergroup.xml",
		"file:src/main/webapp/WEB-INF/applicationContext-spring-security.xml",
		"file:src/main/webapp/WEB-INF/applicationContext-jersey.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Ignore("This test doesn't quite work yet because we don't have mock provisioning classes in the webapp")
public class NodeProvisioningControllerTest {

	private static final GrantedAuthority ROLE_USER = new SimpleGrantedAuthority(
			Authentication.ROLE_USER);
	private static final GrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority(
			Authentication.ROLE_ADMIN);
	private static final GrantedAuthority ROLE_PROVISION = new SimpleGrantedAuthority(
			Authentication.ROLE_PROVISION);
	private static final GrantedAuthority ROLE_ANONYMOUS = new SimpleGrantedAuthority(
			"ROLE_ANONYMOUS");
	private static final GrantedAuthority ROLE_DASHBOARD = new SimpleGrantedAuthority(
			Authentication.ROLE_DASHBOARD);

	private static final String USERNAME = "opennms";

	private static final String PASS = "r0c|<Z";

	NodeProvisioningController m_controller = new NodeProvisioningController();

	@Before
	public void setUp() throws Exception {
		SecurityContext context = new SecurityContextImpl();
		User principal = new User(USERNAME, PASS, true, true, true, true,
				Arrays.asList(new GrantedAuthority[] { ROLE_ADMIN, ROLE_PROVISION }));
		org.springframework.security.core.Authentication auth = new PreAuthenticatedAuthenticationToken(
				principal, new Object());
		context.setAuthentication(auth);
		SecurityContextHolder.setContext(context);

		m_controller.setRedirectView("REDIRECTED");
		m_controller.setSuccessView("REDIRECTED");
		m_controller.setNodeProvisionService(new DefaultNodeProvisionService());
		m_controller.setServletContext(new MockServletContext());
		m_controller.afterPropertiesSet();
	}

	@Test
	public void testProvisionNode() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		ModelAndView mv = m_controller.handleRequest(request, new MockHttpServletResponse());
		ModelAndViewAssert.assertViewName(mv, m_controller.getSuccessView());
	}
}
