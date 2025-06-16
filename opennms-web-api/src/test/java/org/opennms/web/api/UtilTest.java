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
        Assert.assertEquals("10/30/14, 12:00:00 AM", formattedDate);
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
