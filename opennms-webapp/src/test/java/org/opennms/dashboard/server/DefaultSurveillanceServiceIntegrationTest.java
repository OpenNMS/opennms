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

import org.opennms.dashboard.client.NodeRtc;
import org.opennms.dashboard.client.SurveillanceService;
import org.opennms.dashboard.client.SurveillanceSet;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.test.WebAppTestConfigBean;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class DefaultSurveillanceServiceIntegrationTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private SurveillanceService m_gwtSurveillanceService;

    @Override
    protected void setUpConfiguration() {
        WebAppTestConfigBean webAppTestConfig = new WebAppTestConfigBean();
        webAppTestConfig.afterPropertiesSet();
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
                "classpath:/org/opennms/web/svclayer/applicationContext-svclayer.xml",
                "classpath*:/META-INF/opennms/component-dao.xml",
                "classpath:/org/opennms/dashboard/applicationContext-svclayer-dashboard-test.xml"
        };
    }
    
    @Override
    protected void onSetUpInTransactionIfEnabled() throws Exception {
        super.onSetUpInTransactionIfEnabled();
        
        /*
         * Since the SecurityContext is stored in a ThreadLocal we need to
         * be sure to clear it before every test.
         */
        SecurityContextHolder.clearContext();
    }
    
    public void testGetRtcForSet() {
        populateSecurityContext();

        NodeRtc[] rtcs = m_gwtSurveillanceService.getRtcForSet(SurveillanceSet.DEFAULT);
        
        assertNotNull("rtcs should not be null", rtcs);
    }
    
    private UserDetails populateSecurityContext() {
        UserDetails details = new User("user", "password", true, true, true, true, new GrantedAuthority[0]);
        Authentication auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return details;
    }

    public SurveillanceService getGwtSurveillanceService() {
        return m_gwtSurveillanceService;
    }

    public void setGwtSurveillanceService(SurveillanceService gwtSurveillanceService) {
        m_gwtSurveillanceService = gwtSurveillanceService;
    }
}
