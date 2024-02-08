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
package org.opennms.web.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class NMS12517_Test {

    @Test
    public void singleQuoteTest() throws Exception {
        final EventQueryServlet eventQueryServlet = new EventQueryServlet();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("eventtext", "'öäü<>'");

        eventQueryServlet.doPost(request, response);

        assertEquals(302, response.getStatus());
        System.err.println("->"+response.getHeader("Location")+"<-");
        assertEquals("filter?filter=eventtext%3D%27%C3%B6%C3%A4%C3%BC%26lt%3B%26gt%3B%27", response.getHeader("Location"));
    }
}
