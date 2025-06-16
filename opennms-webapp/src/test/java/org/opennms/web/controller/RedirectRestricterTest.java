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
