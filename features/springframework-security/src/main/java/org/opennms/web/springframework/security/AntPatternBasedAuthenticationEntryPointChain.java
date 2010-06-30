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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.web.springframework.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.AuthenticationException;
import org.springframework.security.ui.AbstractProcessingFilter;
import org.springframework.security.ui.AuthenticationEntryPoint;
import org.springframework.security.ui.savedrequest.SavedRequest;
import org.springframework.security.util.AntUrlPathMatcher;
import org.springframework.security.util.PortResolverImpl;
import org.springframework.util.Assert;

/**
 * PatternBasedAuthenticationEntryPointWrapper
 *
 * @author brozow
 * @version $Id: $
 */
public class AntPatternBasedAuthenticationEntryPointChain implements AuthenticationEntryPoint, InitializingBean {

    private List<String> m_patterns;
    private AuthenticationEntryPoint m_matchingEntryPoint;
    private AuthenticationEntryPoint m_nonMatchingEntryPoint;
    
    private AntUrlPathMatcher m_urlPathMatcher = new AntUrlPathMatcher();
    
    
    /**
     * <p>setPatterns</p>
     *
     * @param patterns the patterns to set
     */
    public void setPatterns(List<String> patterns) {
        m_patterns = patterns;
    }

    /**
     * <p>setMatchingEntryPoint</p>
     *
     * @param matchedEntryPoint the matchedEntryPoint to set
     */
    public void setMatchingEntryPoint(AuthenticationEntryPoint matchedEntryPoint) {
        m_matchingEntryPoint = matchedEntryPoint;
    }

    /**
     * <p>setNonMatchingEntryPoint</p>
     *
     * @param unmatchedEntryPoint the unmatchedEntryPoint to set
     */
    public void setNonMatchingEntryPoint(AuthenticationEntryPoint unmatchedEntryPoint) {
        m_nonMatchingEntryPoint = unmatchedEntryPoint;
    }
    
    
    /**
     * <p>setRequiresLowerCaseUrl</p>
     *
     * @param requiresLowerCaseUrl a boolean.
     */
    public void setRequiresLowerCaseUrl(boolean requiresLowerCaseUrl) {
        m_urlPathMatcher.setRequiresLowerCaseUrl(requiresLowerCaseUrl);
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_nonMatchingEntryPoint, "nonMatchingEntryPoint may not be null");
        Assert.notNull(m_matchingEntryPoint, "matchingEntryPoint may not be null");
        Assert.notNull(m_patterns, "patterns may not be null");
    }

    /** {@inheritDoc} */
    public void commence(ServletRequest request, ServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        
        String url = getUrl(request);

        AuthenticationEntryPoint entryPoint = getAppropriateEntryPoint(url);
        
        entryPoint.commence(request, response, authException);
        
    }
    
    private AuthenticationEntryPoint getAppropriateEntryPoint(String url) {
        for (String pattern : m_patterns) {
            if (m_urlPathMatcher.pathMatchesUrl(m_urlPathMatcher.compile(pattern), url)) {
                return m_matchingEntryPoint;
            }
        }
        
        return m_nonMatchingEntryPoint;
        
    }
    
    private String getUrl(ServletRequest request) {
        return getSavedRequest(request).getRequestUrl();
    }
    
    private SavedRequest getSavedRequest(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        HttpSession httpSession = httpRequest.getSession(false);
        if (httpSession == null) {
            return new SavedRequest(httpRequest, new PortResolverImpl());
        } else {
            return (SavedRequest) httpSession.getAttribute(AbstractProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY);
        }
        
    }


}
