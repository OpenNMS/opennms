/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class RedirectRestricterTest {

    @Test
    public void shouldRejectNullAndEmptyRedirects() {
        assertThrowsException(NullPointerException.class, () -> RedirectRestricter.builder().allowRedirect(null).build());
        assertThrowsException(IllegalArgumentException.class, () -> RedirectRestricter.builder().allowRedirect("").build());
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

        assertEquals("redirectA?a=b", restricter.getRedirectOrNull("redirectA?a=b"));
        assertEquals("redirectB?c=d", restricter.getRedirectOrNull("redirectB?c=d"));
        assertNull(restricter.getRedirectOrNull("unknownRedirect?e=f"));
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
