/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.core.utils.url;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

/**
 * <p>GenericURLConnectionTest class.</p>
 *
 * @author Ronny Trommer <ronny@opennms.org>
 * @version $Id: $
 * @since 1.10.1
 */
public class GenericURLConnectionTest extends TestCase {

    private GenericURLConnection m_genericURLConnection;

    private URL m_baseUrl;

    private URL m_userOnlyUrl;

    private URL m_userPassUrl;

    private URL m_baseUrlPathQueryString;

    @Before
    public void setUp() throws Exception {
        m_baseUrl = new URL("http://host.subdomain.domain.tld");
        m_userOnlyUrl = new URL("http://user@host.subdomain.domain.tld");
        m_userPassUrl = new URL("http://user:pass@host.subdomain.domain.tld");
        m_baseUrlPathQueryString = new URL("http://host.subdomain.domain.tld/path1/path2?arg1=value1&arg2=value2&arg3=value3");
    }

    @Test
    public void testUrlComponents() {
        m_genericURLConnection = new StubGenericURLConnection(m_baseUrl);
        assertEquals("Test host", m_genericURLConnection.getURL().getHost(), "host.subdomain.domain.tld");
        assertEquals("Test port", m_genericURLConnection.getURL().getPort(), -1);
        assertEquals("Test default port", m_genericURLConnection.getURL().getDefaultPort(), 80);
        assertEquals("Test protocol", m_genericURLConnection.getURL().getProtocol(), "http");
    }

    @Test
    public void testBaseUrlUsername() {
        m_genericURLConnection = new StubGenericURLConnection(m_baseUrl);
        assertNull("Test user URL", m_genericURLConnection.getUsername());
    }

    @Test
    public void testBaseUrlPassword() {
        m_genericURLConnection = new StubGenericURLConnection(m_baseUrl);
        assertNull("Test base URL", m_genericURLConnection.getPassword());
    }

    @Test
    public void testUserOnlyUrlUsername() {
        m_genericURLConnection = new StubGenericURLConnection(m_userOnlyUrl);
        assertEquals("Test user only URL", m_genericURLConnection.getUsername(), "user");
    }

    @Test
    public void testUserOnlyUrlPassword() {
        m_genericURLConnection = new StubGenericURLConnection(m_userOnlyUrl);
        assertEquals("Test user only URL", m_genericURLConnection.getPassword(), "");
    }

    @Test
    public void testUserPassUrlUsername() {
        m_genericURLConnection = new StubGenericURLConnection(m_userPassUrl);
        assertEquals("Test user pass URL", m_genericURLConnection.getUsername(), "user");
    }

    @Test
    public void testUserPassUrlPassword() {
        m_genericURLConnection = new StubGenericURLConnection(m_userPassUrl);
        assertEquals("Test user pass URL", m_genericURLConnection.getPassword(), "pass");
    }

    @Test
    public void testQueryString() {
        m_genericURLConnection = new StubGenericURLConnection(m_baseUrlPathQueryString);
        assertEquals("Argument 1", m_genericURLConnection.getQueryArgs().get("arg1"), "value1");
        assertEquals("Argument 2", m_genericURLConnection.getQueryArgs().get("arg2"), "value2");
        assertEquals("Argument 3", m_genericURLConnection.getQueryArgs().get("arg3"), "value3");
    }

    @Test
    public void testQueryStringWithPath() {
        m_genericURLConnection = new StubGenericURLConnection(m_baseUrlPathQueryString);
        assertEquals("Full path", m_genericURLConnection.getURL().getPath(), "/path1/path2");
    }
}
