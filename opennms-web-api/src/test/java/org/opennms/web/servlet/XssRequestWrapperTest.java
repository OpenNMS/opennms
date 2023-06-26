/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.web.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.springframework.mock.web.MockHttpServletRequest;

public class XssRequestWrapperTest {

    @Before
    public void setup() {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    public void testSomething() {
        final HttpServletRequest httpServletRequest = new MockHttpServletRequest() {
            @Override
            public Map<String, String[]> getParameterMap() {
                Map<String, String[]> map = new TreeMap<>();
                map.put("foo", new String[] {"bar"});
                map.put("pass1", new String[] {"secret"});
                map.put("oldPass", new String[] {"secret"});
                map.put("currentPassword", new String[] {"secret"});
                return map;
            }
        };

        final XssRequestWrapper xssRequestWrapper = new XssRequestWrapper(httpServletRequest);
        final Set<String> messages = Arrays.stream(MockLogAppender.getEvents()).map(e -> e.getMessage()).collect(Collectors.toSet());

        assertEquals(4, messages.size());
        assertTrue(messages.contains("Sanitization. Param seems safe: pass1[0]=<output omitted>"));
        assertTrue(messages.contains("Sanitization. Param seems safe: oldPass[0]=<output omitted>"));
        assertTrue(messages.contains("Sanitization. Param seems safe: currentPassword[0]=<output omitted>"));
        assertTrue(messages.contains("Sanitization. Param seems safe: foo[0]=bar"));
    }
}

