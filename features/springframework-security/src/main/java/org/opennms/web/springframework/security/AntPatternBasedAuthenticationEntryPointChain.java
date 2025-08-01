/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.springframework.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
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
