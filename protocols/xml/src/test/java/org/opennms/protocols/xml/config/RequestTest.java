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