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
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 *
 */

package org.opennms.web.springframework.security;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.util.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author brozow
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/applicationContext-ldapTest.xml",
})
public class LdapAuthTest {
    
    @Autowired
    FilterChainProxy m_authFilterChain;
    
    MockServletContext m_servletContext;
    
    String m_contextPath = "/opennms";
    
    @Before
    public void setUp() {
        m_servletContext = new MockServletContext();
        m_servletContext.setContextPath(m_contextPath);
    }
    
    @Test
    public void testNoAuth() throws IOException, ServletException {
        
        ServletRequest request = createRequest("GET", "/index.htm");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        FilterChain chain = new FilterChain() {
            public void doFilter(ServletRequest arg0, ServletResponse arg1) throws IOException, ServletException {
                fail("Expected not to get here!");
            }
        };
        
        m_authFilterChain.doFilter(request, response, chain);
        
        assertEquals(401, response.getStatus());

    }
    

    @Test
    public void testBasicAuth() throws IOException, ServletException {
        
        MockHttpServletRequest request = createRequest("GET", "/index.htm", "bob", "bobspassword");

        MockHttpServletResponse response = new MockHttpServletResponse();
        
        FilterChain chain = new FilterChain() {
            public void doFilter(ServletRequest arg0, ServletResponse arg1) throws IOException, ServletException {
                //fail("Expected not to get here!");
            }
        };
        
        m_authFilterChain.doFilter(request, response, chain);
        
        assertEquals(200, response.getStatus());

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
        
        byte[] prefix = "Basic ".getBytes("UTF-8");
        
        
        byte[] headerBytes = new byte[prefix.length + encodedToken.length];
        
        System.arraycopy(prefix, 0, headerBytes, 0, prefix.length);
        System.arraycopy(encodedToken, 0, headerBytes, prefix.length, encodedToken.length);
        
        request.addHeader("Authorization", new String(headerBytes, "UTF-8"));
        return request;
    }

}
