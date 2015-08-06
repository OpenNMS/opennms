package org.opennms.web.api;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

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
}
