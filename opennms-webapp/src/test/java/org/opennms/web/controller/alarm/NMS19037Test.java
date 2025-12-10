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
package org.opennms.web.controller.alarm;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;

public class NMS19037Test {

    public ModelAndView testAcknowledgeAlarmController(final String uri) throws Exception {
        final AcknowledgeAlarmController controller = new AcknowledgeAlarmController();
        controller.setAlarmRepository(Mockito.mock(AlarmRepository.class));
        controller.setRedirectView("/alarm/list.htm");
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Mockito.when(request.getParameterValues("alarm")).thenReturn(new String[]{"2"});
        Mockito.when(request.getParameter("actionCode")).thenReturn("ack");
        Mockito.when(request.getParameter("redirect")).thenReturn(uri);
        return controller.handleRequestInternal(request, response);
    }

    public ModelAndView testAlarmSeverityChangeController(final String uri) throws Exception {
        final AlarmSeverityChangeController controller = new AlarmSeverityChangeController();
        controller.setAlarmRepository(Mockito.mock(AlarmRepository.class));
        controller.setRedirectView("/alarm/list.htm");
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Mockito.when(request.getParameterValues("alarm")).thenReturn(new String[]{"2"});
        Mockito.when(request.getParameter("actionCode")).thenReturn("1");
        Mockito.when(request.getParameter("redirect")).thenReturn(uri);
        return controller.handleRequestInternal(request, response);
    }

    @Test
    public void testAcknowledgeAlarmControllerAbsoluteRedirect() throws Exception {
        assertEquals("/alarm/list.htm", ((RedirectView)testAcknowledgeAlarmController("https://www.opennms.com").getView()).getUrl());
    }

    @Test
    public void testAcknowledgeAlarmControllerRedirectRelative() throws Exception {
        assertEquals("/alarm/list.htm", ((RedirectView)testAcknowledgeAlarmController("/foo").getView()).getUrl());
    }

    @Test
    public void testAcknowledgeAlarmControllerRedirectParams() throws Exception {
        assertEquals("/alarm/list.htm", ((RedirectView)testAcknowledgeAlarmController("foo").getView()).getUrl());
    }

    @Test
    public void testAlarmSeverityChangeControllerAbsoluteRedirect() throws Exception {
        assertEquals("/alarm/list.htm", ((RedirectView)testAlarmSeverityChangeController("https://www.opennms.com").getView()).getUrl());

    }

    @Test
    public void testAlarmSeverityChangeControllerRedirectRelative() throws Exception {
        assertEquals("/alarm/list.htm", ((RedirectView)testAlarmSeverityChangeController("/foo").getView()).getUrl());
    }

    @Test
    public void testAlarmSeverityChangeControllerRedirectParams() throws Exception {
        assertEquals("/alarm/list.htm", ((RedirectView)testAlarmSeverityChangeController("foo").getView()).getUrl());
    }
}
