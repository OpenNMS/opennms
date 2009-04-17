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
 * Created: February 16, 2007
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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.opennms.dashboard.client.NodeRtc;
import org.opennms.dashboard.client.SurveillanceSet;
import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.surveillanceViews.Columns;
import org.opennms.netmgt.config.surveillanceViews.Rows;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.dao.SurveillanceViewConfigDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.web.svclayer.support.DefaultRtcService;
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
public class DefaultSurveillanceServiceTest {
    private DefaultSurveillanceService m_service;

    @Before
    public void setUp() throws Exception {
               
        m_service = new DefaultSurveillanceService();
        
        /*
         * Since the SecurityContext is stored in a ThreadLocal we need to
         * be sure to clear it before every test.
         */
        SecurityContextHolder.clearContext();
    }
    
    @Test
    public void testGetUsernameWithUserDetails() {
        UserDetails details = populateSecurityContext();
        
        String user = m_service.getUsername();
        assertNotNull("user should not be null", user);
        assertEquals("user name", details.getUsername(), user);
    }
    
    @Test
    public void testGetUsernameWithStringPrincipal() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", null, new GrantedAuthority[0]);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        String user = m_service.getUsername();
        assertNotNull("user should not be null", user);
        assertEquals("user name", "user", user);
    }
    
    @Test
    public void testGetUsernameNoAuthenticationObject() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("No Authentication object found when calling getAuthentication on our SecurityContext object"));
        
        try {
            m_service.getUsername();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }
    
    @Test
    public void testGetUsernameNoPrincipalObject() {
        Authentication auth = new UsernamePasswordAuthenticationToken(null, null, new GrantedAuthority[0]);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("No principal object found when calling getPrinticpal on our Authentication object"));
        
        try {
            m_service.getUsername();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }
    
    @Test
    public void testGetRtcForSet() {
        UserDetails details = populateSecurityContext();
        
        EasyMockUtils mock = new EasyMockUtils();
        
        MonitoredServiceDao monSvcDao = mock.createMock(MonitoredServiceDao.class);
        OutageDao outageDao = mock.createMock(OutageDao.class);
        SurveillanceViewConfigDao survViewConfigDao = mock.createMock(SurveillanceViewConfigDao.class);
        GroupDao groupDao = mock.createMock(GroupDao.class);
        CategoryDao categoryDao = mock.createMock(CategoryDao.class);
        
        mock.replayAll();
        
        DefaultRtcService rtcService = new DefaultRtcService();
        rtcService.setMonitoredServiceDao(monSvcDao);
        rtcService.setOutageDao(outageDao);
        rtcService.afterPropertiesSet();
        
        m_service.setRtcService(rtcService);
        m_service.setSurveillanceViewConfigDao(survViewConfigDao);
        m_service.setGroupDao(groupDao);
        m_service.setCategoryDao(categoryDao);
        mock.verifyAll();

        expect(survViewConfigDao.getView(details.getUsername())).andReturn(null).atLeastOnce();
        expect(groupDao.findGroupsForUser(details.getUsername())).andReturn(new ArrayList<Group>()).atLeastOnce();
        
        View defaultView = new View();
        defaultView.setColumns(new Columns());
        defaultView.setRows(new Rows());
        expect(survViewConfigDao.getDefaultView()).andReturn(defaultView).atLeastOnce();
        
        expect(monSvcDao.findMatching(isA(OnmsCriteria.class))).andReturn(new ArrayList<OnmsMonitoredService>());
        expect(outageDao.findMatching(isA(OnmsCriteria.class))).andReturn(new ArrayList<OnmsOutage>());

        mock.replayAll();
        NodeRtc[] rtcs = m_service.getRtcForSet(SurveillanceSet.DEFAULT);
        mock.verifyAll();
        
        assertNotNull("rtcs should not be null", rtcs);
    }
    
    private UserDetails populateSecurityContext() {
        UserDetails details = new User("user", "password", true, true, true, true, new GrantedAuthority[0]);
        Authentication auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return details;
    }
}
