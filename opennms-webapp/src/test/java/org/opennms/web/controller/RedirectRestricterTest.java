package org.opennms.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class RedirectRestricterTest {

    @Test
    public void shouldRejectNullAndEmptyRedirects() {
        assertThrowsException(NullPointerException.class, () -> RedirectRestricter.builder().allowRedirect(null).build());
        assertThrowsException(NullPointerException.class, () -> RedirectRestricter.builder().allowRedirect("").build());
        RedirectRestricter.builder().allowRedirect("something").build(); // should be ok
    }

    @Test
    public void shouldReturnNullForInvalidRedirects() {
        RedirectRestricter restricter = RedirectRestricter.builder().allowRedirect("something").build();
        assertNull(restricter.getRedirectOrNull(null));
        assertNull(restricter.getRedirectOrNull(""));
        assertNull(restricter.getRedirectOrNull("unknownRedirect"));
    }

    @Test
    public void shouldReturnConfiguredRedirects() {
        RedirectRestricter restricter = RedirectRestricter.builder()
                .allowRedirect("redirectA")
                .allowRedirect("redirectB").build();

        assertEquals("redirectA", restricter.getRedirectOrNull("redirectA"));
        assertEquals("redirectB", restricter.getRedirectOrNull("redirectB"));
        assertNull(restricter.getRedirectOrNull("unknownRedirect"));
    }

    @Test
    public void shouldReturnConfiguredRedirectsWithParameters() {
        RedirectRestricter restricter = RedirectRestricter.builder()
                .allowRedirect("redirectA")
                .allowRedirect("redirectB").build();

        assertEquals("redirectA?blah", restricter.getRedirectOrNull("redirectA?blah"));
        assertEquals("redirectB?blub", restricter.getRedirectOrNull("redirectB?blub"));
        assertEquals("default", restricter.getRedirectOrNull("unknownRedirect=blah"));
    }

    public static void assertThrowsException(Class<? extends Throwable> expectedException, Runnable function) {
        try {
            function.run();
        } catch(Exception e) {
            if(!expectedException.isAssignableFrom(e.getClass())) {
                fail(String.format("Expected exception: %s but was %s", expectedException.getName(), e.getClass().getName()));
            }
            return;
        }
        fail(String.format("Expected exception: %s but none was thrown.", expectedException.getName()));
    }
}
