/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.opennms.core.resource.Vault;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.PortMapper;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

public class NMS16450Test {
    private static MockedStatic<Vault> vault;

    @BeforeClass
    public static void beforeClass() {
        vault = mockStatic(Vault.class);
    }

    @Test
    public void testComputedUrls() throws Exception {
        compareOriginalToOverwrittenMethod("%s://%x%c/", "http", "opennms.com", 8980, "/opennms", "http://opennms.com:8980/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("%s://%x%c/", "http", "opennms.com", 80, "/opennms", "http://opennms.com/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("http://%x%c/", "http", "opennms.com", 8980, "/opennms", "http://opennms.com:8980/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("http://%x%c/", "http", "opennms.com", 80, "/opennms", "http://opennms.com/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("%s://%x%c/", "https", "opennms.com", 8443, "/opennms", "https://opennms.com:8443/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("%s://%x%c/", "https", "opennms.com", 443, "/opennms", "https://opennms.com/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("https://%x%c/", "https", "opennms.com", 8443, "/opennms", "https://opennms.com:8443/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("https://%x%c/", "https", "opennms.com", 443, "/opennms", "https://opennms.com/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("http://opennms.com:8980%c/", "http", "opennms.com", 8980, "/opennms", "http://opennms.com:8980/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("http://opennms.com:80%c/", "http", "opennms.com", 80, "/opennms", "http://opennms.com/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("https://opennms.com:8443%c/", "https", "opennms.com", 8443, "/opennms", "https://opennms.com:8443/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("https://opennms.com:443%c/", "https", "opennms.com", 443, "/opennms", "https://opennms.com/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("http://opennms.com:8980/opennms", "http", "opennms.com", 8980, "/opennms", "http://opennms.com:8980/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("http://opennms.com:80/opennms", "http", "opennms.com", 80, "/opennms", "http://opennms.com/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("https://opennms.com:8443/opennms", "https", "opennms.com", 8443, "/opennms", "https://opennms.com:8443/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("https://opennms.com:443/opennms", "https", "opennms.com", 443, "/opennms", "https://opennms.com/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("https://%x%c/", "http", "opennms.com", 8443, "/opennms", "https://opennms.com:8443/opennms/login.jsp");
        compareOriginalToOverwrittenMethod("https://%x%c/", "http", "opennms.com", 443, "/opennms", "https://opennms.com/opennms/login.jsp");
    }

    @Test
    public void testNMS16450() throws Exception {
        testBuildRedirectUrlToLoginPage("http://opennms.com:8980/opennms", "http", "opennms.com", 8980, "/opennms", "http://opennms.com:8980/opennms/login.jsp");
        testBuildRedirectUrlToLoginPage("http://opennms.com/opennms", "http", "opennms.com", 80, "/opennms", "http://opennms.com/opennms/login.jsp");
        testBuildRedirectUrlToLoginPage("https://opennms.com:8443/opennms", "https", "opennms.com", 8443, "/opennms", "https://opennms.com:8443/opennms/login.jsp");
        testBuildRedirectUrlToLoginPage("https://opennms.com:443/opennms", "https", "opennms.com", 443, "/opennms", "https://opennms.com/opennms/login.jsp");

        testBuildRedirectUrlToLoginPage("http://opennms.com:8980/opennms", "http", "foobar.com", 8980, "/opennms", "http://opennms.com:8980/opennms/login.jsp");
        testBuildRedirectUrlToLoginPage("http://opennms.com/opennms", "http", "foobar.com", 80, "/opennms", "http://opennms.com/opennms/login.jsp");
        testBuildRedirectUrlToLoginPage("https://opennms.com:8443/opennms", "https", "foobar.com", 8443, "/opennms", "https://opennms.com:8443/opennms/login.jsp");
        testBuildRedirectUrlToLoginPage("https://opennms.com:443/opennms", "https", "foobar.com", 443, "/opennms", "https://opennms.com/opennms/login.jsp");
    }

    private void compareOriginalToOverwrittenMethod(final String baseUrl, final String requestScheme, final String requestName, final int requestPort, final String requestContextPath, final String expectedUrl) throws Exception {
        checkCorrectnessOfUrl(baseUrl, requestScheme, requestName, requestPort, requestContextPath, expectedUrl, true);
    }

    private void testBuildRedirectUrlToLoginPage(final String baseUrl, final String requestScheme, final String requestName, final int requestPort, final String requestContextPath, final String expectedUrl) throws Exception {
        checkCorrectnessOfUrl(baseUrl, requestScheme, requestName, requestPort, requestContextPath, expectedUrl, false);
    }

    private void checkCorrectnessOfUrl(final String baseUrl, final String requestScheme, final String requestName, final int requestPort, final String requestContextPath, final String expectedUrl, final boolean checkOriginal) throws Exception {
        vault.when(() -> Vault.getProperty("opennms.web.base-url")).thenReturn(baseUrl);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final PortResolver portResolver = mock(PortResolver.class);
        final PortMapper portMapper = mock(PortMapper.class);

        when(portResolver.getServerPort(request)).thenReturn(requestPort);
        when(request.getScheme()).thenReturn(requestScheme);
        when(request.getServerName()).thenReturn(requestName);
        when(request.getServerPort()).thenReturn(requestPort);
        when(request.getContextPath()).thenReturn(requestContextPath);
        when(portMapper.lookupHttpsPort(requestPort)).thenReturn(requestPort);
        when(portMapper.lookupHttpPort(requestPort)).thenReturn(requestPort);

        if (checkOriginal) {
            final LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint = new LoginUrlAuthenticationEntryPoint("/login.jsp");
            loginUrlAuthenticationEntryPoint.setForceHttps(baseUrl.startsWith("https"));
            loginUrlAuthenticationEntryPoint.setPortResolver(portResolver);
            loginUrlAuthenticationEntryPoint.setPortMapper(portMapper);
            final Method method = loginUrlAuthenticationEntryPoint.getClass().getDeclaredMethod("buildRedirectUrlToLoginPage", HttpServletRequest.class, HttpServletResponse.class, AuthenticationException.class);
            method.setAccessible(true);
            final String result = (String) method.invoke(loginUrlAuthenticationEntryPoint, request, response, null);
            assertEquals(expectedUrl, result);
        }

        final OpenNMSLoginUrlAuthEntryPoint openNMSLoginUrlAuthEntryPoint = new OpenNMSLoginUrlAuthEntryPoint("/login.jsp");
        openNMSLoginUrlAuthEntryPoint.setPortResolver(portResolver);
        openNMSLoginUrlAuthEntryPoint.setPortMapper(portMapper);

        assertEquals(expectedUrl, openNMSLoginUrlAuthEntryPoint.buildRedirectUrlToLoginPage(request, response, null));
    }
}
