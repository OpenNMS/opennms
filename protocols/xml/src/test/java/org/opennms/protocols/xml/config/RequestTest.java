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
package org.opennms.protocols.xml.config;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RequestTest {

    @Test
    public void shouldResolveGetParameter() {
        Request request = new Request();
        request.addParameter("null value", null);
        request.addParameter("string value", "string");
        assertNull(request.getParameter("null value"));
        assertEquals("string", request.getParameter("string value"));
        assertNull(request.getParameter("not present value"));
    }

    @Test
    public void shouldResolveGetParameterAsInt() {
        Request request = new Request();
        request.addParameter("null value", null);
        request.addParameter("int value", "3");
        request.addParameter("not valid int value", "blah");
        assertEquals(-1, request.getParameterAsInt("null value"));
        assertEquals(3, request.getParameterAsInt("int value"));
        assertEquals(-1, request.getParameterAsInt("not valid int value"));
        assertEquals(-1, request.getParameterAsInt("not present value"));
    }

    @Test
    public void shouldResolveGetParameterAsBoolean() {
        Request request = new Request();
        request.addParameter("null value", null);
        request.addParameter("true", "true");
        request.addParameter("false", "false");
        request.addParameter("not alid boolean value", "blah");
        assertFalse(request.getParameterAsBoolean("null value"));
        assertTrue(request.getParameterAsBoolean("true"));
        assertFalse(request.getParameterAsBoolean("false"));
        assertFalse(request.getParameterAsBoolean("not valid boolean value"));
        assertFalse(request.getParameterAsBoolean("not present value"));
    }
}