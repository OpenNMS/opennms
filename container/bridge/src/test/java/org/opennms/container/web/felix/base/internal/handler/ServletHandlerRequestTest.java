/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.container.web.felix.base.internal.handler;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(Parameterized.class)
public class ServletHandlerRequestTest {
    private HttpServletRequest m_request;
    private String m_alias;
    private String m_expectedContextPath;
    private String m_expectedServletPath;
    private String m_expectedPathInfo;

    public ServletHandlerRequestTest(final HttpServletRequest request, final String alias, final String expectedContextPath, final String expectedServletPath, final String expectedPathInfo) {
        m_request = request;
        m_alias = alias;
        m_expectedContextPath = expectedContextPath;
        m_expectedServletPath = expectedServletPath;
        m_expectedPathInfo    = expectedPathInfo;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {
                createRequest("/opennms/osgi", "/mib-compiler", null),
                "/mib-compiler",
                "/opennms/osgi",
                "/mib-compiler",
                null
            },
            {
                createRequest("/opennms", "/mib-compiler", null),
                "/mib-compiler",
                "/opennms",
                "/mib-compiler",
                null
            },
            {
                createRequest("/opennms", "/VAADIN/themes/runo/styles.css", null),
                "/VAADIN",
                "/opennms",
                "/VAADIN",
                "/themes/runo/styles.css"
            },
            {
                createRequest("/opennms", "/osgi", "/mib-compiler/UIDL"),
                "/mib-compiler",
                "/opennms",
                "/mib-compiler",
                "/UIDL"
            },
            {
                createRequest("/opennms", "/mib-compiler/UIDL", null),
                "/mib-compiler",
                "/opennms",
                "/mib-compiler",
                "/UIDL"
            }
        });
    }

    @Test
    public void testRequest() {
        final ServletHandlerRequest request = new ServletHandlerRequest(m_request, m_alias);
        assertEquals(m_expectedContextPath, request.getContextPath());
        assertEquals(m_expectedServletPath, request.getServletPath());
        assertEquals(m_expectedPathInfo,    request.getPathInfo());
    }

    private static HttpServletRequest createRequest(final String contextPath, final String servletPath, final String pathInfo) {
        return new MockHttpServletRequest() {
            @Override public String getContextPath() { return contextPath; }
            @Override public String getServletPath() { return servletPath; }
            @Override public String getPathInfo()    { return pathInfo;    }
        };
    }
}
