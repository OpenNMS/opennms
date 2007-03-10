package org.opennms.dashboard.server;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import java.util.ArrayList;

import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.opennms.dashboard.client.NodeRtc;
import org.opennms.dashboard.client.SurveillanceSet;
import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.surveillanceViews.Columns;
import org.opennms.netmgt.config.surveillanceViews.Rows;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.dao.SurveillanceViewConfigDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.test.ThrowableAnticipator;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.web.svclayer.support.DefaultRtcService;
import org.opennms.web.svclayer.support.RtcNodeModel;

import junit.framework.TestCase;

public class DefaultSurveillanceServiceTest extends TestCase {
    private DefaultSurveillanceService m_service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        m_service = new DefaultSurveillanceService();
        
        /*
         * Since the SecurityContext is stored in a ThreadLocal we need to
         * be sure to clear it before every test.
         */
        SecurityContextHolder.clearContext();
    }
    
    public void testGetUsernameWithUserDetails() {
        UserDetails details = populateSecurityContext();
        
        String user = m_service.getUsername();
        assertNotNull("user should not be null", user);
        assertEquals("user name", details.getUsername(), user);
    }
    
    public void testGetUsernameWithStringPrincipal() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", null, new GrantedAuthority[0]);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        String user = m_service.getUsername();
        assertNotNull("user should not be null", user);
        assertEquals("user name", "user", user);
    }
    
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
    
    public void NOTYETWORKINGtestGetOutagesForSet() {
        m_service.getOutagesForSet(SurveillanceSet.DEFAULT);
    }
    
    private UserDetails populateSecurityContext() {
        UserDetails details = new User("user", "password", true, true, true, true, new GrantedAuthority[0]);
        Authentication auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        return details;
    }
}
