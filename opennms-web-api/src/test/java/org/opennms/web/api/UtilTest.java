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
