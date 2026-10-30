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

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class GraphCollectionRedirectControllerTest {

    private static String target(final String... params) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        for (int i = 0; i < params.length; i += 2) {
            request.setParameter(params[i], params[i + 1]);
        }
        return GraphCollectionRedirectController.targetFor(request);
    }

    @Test
    public void sendsEachLegacyReportTypeToItsView() {
        assertEquals("/ui/index.html#/graph-collections/3", target("type", "custom", "report", "3"));
        assertEquals("/ui/index.html#/graph-collections/node/7", target("type", "node", "report", "7"));
        assertEquals("/ui/index.html#/graph-collections/nodeSource/lab%3Arouter%201", target("type", "nodeSource", "report", "lab:router 1"));
        assertEquals("/ui/index.html#/graph-collections/domain/core", target("type", "domain", "report", "core"));
    }

    @Test
    public void carriesTheOverridesOver() {
        assertEquals("/ui/index.html#/graph-collections/3?timespan=1_day&graphtype=mib2.bits",
                target("type", "custom", "report", "3", "timespan", "1_day", "graphtype", "mib2.bits"));
        assertEquals("/ui/index.html#/graph-collections/3?graphtype=mib2.bits",
                target("type", "custom", "report", "3", "timespan", "none", "graphtype", "mib2.bits"));
        assertEquals("/ui/index.html#/graph-collections/3", target("type", "custom", "report", "3", "timespan", "null", "graphtype", ""));
    }

    @Test
    public void landsOnTheListWhenTheRequestNamesNoCollection() {
        assertEquals("/ui/index.html#/graph-collections", target());
        assertEquals("/ui/index.html#/graph-collections", target("type", "custom"));
        assertEquals("/ui/index.html#/graph-collections", target("type", "custom", "report", "x"));
        assertEquals("/ui/index.html#/graph-collections", target("type", "node", "report", "0"));
        assertEquals("/ui/index.html#/graph-collections", target("type", "weekly", "report", "3"));
        assertEquals("/ui/index.html#/graph-collections?timespan=7_day", target("timespan", "7_day"));
    }

    @Test
    public void redirectsUnderTheContextPath() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/opennms/KSC/customView.htm");
        request.setContextPath("/opennms");
        request.setParameter("type", "custom");
        request.setParameter("report", "3");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final ModelAndView mav = new GraphCollectionRedirectController().handleRequest(request, response);
        mav.getView().render(mav.getModel(), request, response);
        assertEquals("/opennms/ui/index.html#/graph-collections/3", response.getRedirectedUrl());
    }
}
