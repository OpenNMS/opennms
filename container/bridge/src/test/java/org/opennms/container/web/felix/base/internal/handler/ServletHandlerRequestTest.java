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
                "/opennms/osgi",
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
