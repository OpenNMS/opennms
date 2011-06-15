/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 4, 2007
 *
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.dashboard.server;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.dashboard.client.NodeRtc;
import org.opennms.dashboard.client.SurveillanceService;
import org.opennms.dashboard.client.SurveillanceSet;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/org/opennms/web/svclayer/applicationContext-svclayer.xml",
        "classpath*:/META-INF/opennms/applicationContext-reportingCore.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/org/opennms/dashboard/applicationContext-svclayer-dashboard-test.xml",
        "classpath:/META-INF/opennms/applicationContext-insertData-enabled.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DefaultSurveillanceServiceIntegrationTest {
    @Autowired
    private SurveillanceService m_gwtSurveillanceService;

    @Before
    public void setUp() throws Exception {
        /*
         * Since the SecurityContext is stored in a ThreadLocal we need to
         * be sure to clear it before every test.
         */
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetRtcForSet() {
        populateSecurityContext();

        NodeRtc[] rtcs = m_gwtSurveillanceService.getRtcForSet(SurveillanceSet.DEFAULT);

        assertNotNull("rtcs should not be null", rtcs);
    }

    private static UserDetails populateSecurityContext() {
        UserDetails details = new User("user", "password", true, true, true, true, new GrantedAuthority[0]);
        Authentication auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return details;
    }
}
