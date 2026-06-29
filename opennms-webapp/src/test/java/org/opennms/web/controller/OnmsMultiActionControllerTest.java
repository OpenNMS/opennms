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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests for {@link OnmsMultiActionController}'s path-to-handler dispatch, in particular the guard that
 * prevents the framework lifecycle methods ({@code handleRequest}/{@code handleRequestInternal}) from being
 * selected as dispatch targets.
 *
 * <p>Without that guard, a request whose last path segment is {@code handleRequest} (e.g.
 * {@code /event/handleRequest.htm} against the wildcard-mapped {@code EventController}) resolves to the public
 * {@code handleRequest} method &mdash; inherited from {@code AbstractController} <em>or</em> overridden by the
 * subclass &mdash; which re-enters {@code handleRequestInternal} and recurses until a {@link StackOverflowError}
 * (a trivially reachable, unauthenticated DoS).</p>
 */
public class OnmsMultiActionControllerTest {

    /**
     * Concrete controller with a normal action method, plus a public {@code handleRequest} override that
     * mirrors {@code EventController}/{@code AlarmFilterController} (so the test proves the guard catches the
     * subclass-declared override, which a {@code getDeclaredMethods()}-only fix would miss).
     */
    public static class SampleController extends OnmsMultiActionController {
        public ModelAndView myAction(final HttpServletRequest request, final HttpServletResponse response) {
            return new ModelAndView("myActionView");
        }

        @Override
        public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
            return super.handleRequest(request, response);
        }
    }

    private final SampleController controller = new SampleController();

    private static MockHttpServletRequest get(final String uri) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI(uri);
        return request;
    }

    @Test
    public void dispatchesToNamedActionMethod() throws Exception {
        final ModelAndView mv = controller.handleRequestInternal(get("/sample/myAction.htm"), new MockHttpServletResponse());
        assertEquals("myActionView", mv.getViewName());
    }

    @Test
    public void handleRequestPathIsRejectedAndDoesNotRecurse() {
        // The fix: 'handleRequest' must not resolve to the framework method (which would recurse to a
        // StackOverflowError); it is rejected as "no handler method". If this regressed, the call below
        // would blow the stack rather than throw ServletException.
        final ServletException ex = assertThrows(ServletException.class,
                () -> controller.handleRequestInternal(get("/sample/handleRequest.htm"), new MockHttpServletResponse()));
        assertTrue(ex.getMessage().contains("No handler method"));
    }

    @Test
    public void handleRequestInternalPathIsRejected() {
        assertThrows(ServletException.class,
                () -> controller.handleRequestInternal(get("/sample/handleRequestInternal.htm"), new MockHttpServletResponse()));
    }
}
