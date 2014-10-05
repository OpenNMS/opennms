/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.springframework.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.AntPathRequestMatcher;
import org.springframework.security.web.util.RequestMatcher;
import org.springframework.util.Assert;

/**
 * PatternBasedAuthenticationEntryPointWrapper
 */
public class AntPatternBasedAuthenticationEntryPointChain implements AuthenticationEntryPoint, InitializingBean {

    private List<String> m_patterns;
    private AuthenticationEntryPoint m_matchingEntryPoint;
    private AuthenticationEntryPoint m_nonMatchingEntryPoint;
    
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
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_nonMatchingEntryPoint, "nonMatchingEntryPoint may not be null");
        Assert.notNull(m_matchingEntryPoint, "matchingEntryPoint may not be null");
        Assert.notNull(m_patterns, "patterns may not be null");
    }

    /** {@inheritDoc} */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        
        AuthenticationEntryPoint entryPoint = getAppropriateEntryPoint(request);
        
        entryPoint.commence(request, response, authException);
    }
    
    private AuthenticationEntryPoint getAppropriateEntryPoint(HttpServletRequest request) {
        for (String pattern : m_patterns) {
            RequestMatcher matcher = new AntPathRequestMatcher(pattern);
            if (matcher.matches(request)) {
                return m_matchingEntryPoint;
            }
        }
        
        return m_nonMatchingEntryPoint;
        
    }
}
