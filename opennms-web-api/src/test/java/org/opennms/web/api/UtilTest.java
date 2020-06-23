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

package org.opennms.web.api;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.springframework.mock.web.MockHttpServletRequest;

public class UtilTest {

    @Before
    public void setUp() {
        Locale.setDefault(Locale.US);
    }

    @Test
    public void testFormatDateToUIStringOK() throws ParseException {
        final Date inputDate = new SimpleDateFormat("yyyy-MM-dd").parse("2014-10-30");
        final String formattedDate = Util.formatDateToUIString(inputDate);
        Assert.assertEquals("10/30/14 12:00:00 AM", formattedDate);
    }

    @Test
    public void testFormatDateToUIStringNull()  {
        Assert.assertEquals("", Util.formatDateToUIString(null));
    }

    @Test
    public void testDirectHostHeader() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("direct.example.org");
        request.setServerPort(1234);

        final String host = Util.getHostHeader(request);

        Assert.assertEquals("direct.example.org:1234", host);
    }

    @Test
    public void testForwardedHostHeader() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-Host", "first.example.org");

        final String host = Util.getHostHeader(request);

        Assert.assertEquals("first.example.org", host);
    }

    @Test
    public void testMultivaluedForwardedHostHeader() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-Host", "first.example.org, second.example.org, third.example.org");

        final String host = Util.getHostHeader(request);

        Assert.assertEquals("first.example.org", host);
    }

    @Test
    public void testMultipleForwardedHostHeader() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-Host", "first.example.org");
        request.addHeader("X-Forwarded-Host", "second.example.org");
        request.addHeader("X-Forwarded-Host", "third.example.org");

        final String host = Util.getHostHeader(request);

        Assert.assertEquals("first.example.org", host);
    }
}
