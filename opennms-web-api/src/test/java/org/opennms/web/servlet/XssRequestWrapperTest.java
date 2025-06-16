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
