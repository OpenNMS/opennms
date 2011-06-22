/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
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
