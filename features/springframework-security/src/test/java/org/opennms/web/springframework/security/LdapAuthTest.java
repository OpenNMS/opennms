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

package org.opennms.web.springframework.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.utils.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.web.FilterChainProxy;


/**
 * @author brozow
 *
 */
/*@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/applicationContext-ldapTest.xml"
})*/
public class LdapAuthTest implements InitializingBean {
    
    /**
     * @author brozow
     *
     */
    private static class AccesAnticipator implements FilterChain {
        private boolean m_called = false;
        @Override
        public void doFilter(ServletRequest arg0, ServletResponse arg1) throws IOException, ServletException {
            m_called = true;
        }
        
        public void assertAccessDenied() {
            assertFalse("Expected access to be denied", m_called);
        }
        
        public void assertAccessAllowed() {
            assertTrue("Expected access to be allowed", m_called);
        }
    }

    @Autowired
    FilterChainProxy m_authFilterChain;
    
    MockServletContext m_servletContext;
    
    AccesAnticipator m_chain = new AccesAnticipator();
    
    String m_contextPath = "/opennms";
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Before
    public void setUp() {
        m_servletContext = new MockServletContext();
        m_servletContext.setContextPath(m_contextPath);
    }
    
    @Test
    @Ignore
    public void testNoAuth() throws IOException, ServletException {
        
        MockHttpServletRequest request = createRequest("GET", "/index.htm");

        assertAccessDenied(request);
    }
    

    @Test
    @Ignore
    public void testBasicAuth() throws IOException, ServletException {
        
        MockHttpServletRequest request = createRequest("GET", "/index.htm", "bob", "bobspassword");

        assertAccessAllowed(request);

    }

    @Test
    @Ignore
    public void testBasicAuthInvalidPassword() throws IOException, ServletException {
        
        MockHttpServletRequest request = createRequest("GET", "/index.htm", "bob", "invalid");

        assertAccessDenied(request);

    }

    /**
     * @param request
     * @throws IOException
     * @throws ServletException
     */
    private void assertAccessAllowed(MockHttpServletRequest request)
            throws IOException, ServletException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        m_authFilterChain.doFilter(request, response, m_chain);
        
        assertEquals(200, response.getStatus());
        m_chain.assertAccessAllowed();
    }
    
    /**
     * @param request
     * @throws IOException
     * @throws ServletException
     */
    private void assertAccessDenied(MockHttpServletRequest request)
            throws IOException, ServletException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        m_authFilterChain.doFilter(request, response, m_chain);
        
        assertEquals(401, response.getStatus());
        m_chain.assertAccessDenied();
    }
    

    protected MockHttpServletRequest createRequest(String requestType, String urlPath) {
        MockHttpServletRequest request = new MockHttpServletRequest(m_servletContext, requestType, m_contextPath + urlPath);
        request.setServletPath(m_contextPath + urlPath);
        request.setContextPath(m_contextPath);
        return request;
    }
    
    private MockHttpServletRequest createRequest(String requestType, String urlPath, String user, String passwd) throws UnsupportedEncodingException {
        MockHttpServletRequest request = createRequest(requestType, urlPath);
        
        String token = user + ":"  + passwd;
        byte[] encodedToken = Base64.encodeBase64(token.getBytes("UTF-8"));
        request.addHeader("Authorization", "Basic " + new String(encodedToken, "UTF-8"));

        return request;
    }

}
