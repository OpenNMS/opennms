/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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