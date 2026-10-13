/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
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
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.Test;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.web.controller.alarm.AcknowledgeAlarmByFilterController;
import org.opennms.web.controller.alarm.AcknowledgeAlarmController;
import org.opennms.web.controller.alarm.AlarmSeverityChangeController;
import org.opennms.web.controller.notification.AcknowledgeNotificationController;
import org.opennms.web.notification.WebNotificationRepository;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * The alarm and notification action controllers change state, so they must accept only POST.
 */
public class ActionControllerPostOnlyTest {

    private final AlarmRepository alarmRepository = mock(AlarmRepository.class);

    private final WebNotificationRepository notificationRepository = mock(WebNotificationRepository.class);

    @Test
    public void alarmAndNotificationActionsRejectGet() throws Exception {
        assertRejectsGet(acknowledgeAlarmController(), "alarm", "1", "actionCode", "1");
        assertRejectsGet(acknowledgeAlarmByFilterController(), "filter", "node=1", "actionCode", "1");
        assertRejectsGet(alarmSeverityChangeController(), "alarm", "1", "actionCode", "2");
        assertRejectsGet(acknowledgeNotificationController(), "notices", "1", "curUser", "admin");
        verifyNoInteractions(alarmRepository, notificationRepository);
    }

    @Test
    public void notificationAcknowledgeIgnoresClientSuppliedUser() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/opennms/notification/acknowledge");
        request.setRemoteUser("operator");
        request.addParameter("notices", "42");
        request.addParameter("curUser", "admin");

        acknowledgeNotificationController().handleRequest(request, new MockHttpServletResponse());

        verify(notificationRepository).acknowledgeMatchingNotification(eq("operator"), any(), any());
    }

    @Test
    public void notificationAcknowledgeRedirectsBackToDetailPage() throws Exception {
        assertEquals("/opennms/notification/detail.jsp?notice=42", acknowledgeNotificationAndGetLocation("/notification/detail.jsp?notice=42"));
        assertEquals("/opennms/notification/list.htm", acknowledgeNotificationAndGetLocation("https://evil.example/"));
        assertEquals("/opennms/notification/list.htm", acknowledgeNotificationAndGetLocation("?"));
    }

    private String acknowledgeNotificationAndGetLocation(final String redirect) throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/opennms/notification/acknowledge");
        request.setContextPath("/opennms");
        request.setRemoteUser("operator");
        request.addParameter("notices", "42");
        request.addParameter("redirect", redirect);
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final ModelAndView mv = acknowledgeNotificationController().handleRequest(request, response);
        mv.getView().render(mv.getModel(), request, response);
        return response.getRedirectedUrl();
    }

    private static void assertRejectsGet(final AbstractController controller, final String... parameters) throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/opennms/action");
        request.setRemoteUser("operator");
        for (int i = 0; i < parameters.length; i += 2) {
            request.addParameter(parameters[i], parameters[i + 1]);
        }
        try {
            controller.handleRequest(request, new MockHttpServletResponse());
            fail(controller.getClass().getSimpleName() + " accepted a GET request");
        } catch (final HttpRequestMethodNotSupportedException e) {
            assertEquals("GET", e.getMethod());
        }
    }

    private AcknowledgeAlarmController acknowledgeAlarmController() throws Exception {
        final AcknowledgeAlarmController controller = new AcknowledgeAlarmController();
        controller.setAlarmRepository(alarmRepository);
        controller.setRedirectView("/alarm/list.htm");
        controller.afterPropertiesSet();
        return controller;
    }

    private AcknowledgeAlarmByFilterController acknowledgeAlarmByFilterController() throws Exception {
        final AcknowledgeAlarmByFilterController controller = new AcknowledgeAlarmByFilterController();
        controller.setAlarmRepository(alarmRepository);
        controller.setRedirectView("/alarm/list.htm");
        controller.afterPropertiesSet();
        return controller;
    }

    private AlarmSeverityChangeController alarmSeverityChangeController() throws Exception {
        final AlarmSeverityChangeController controller = new AlarmSeverityChangeController();
        controller.setAlarmRepository(alarmRepository);
        controller.setRedirectView("/alarm/list.htm");
        controller.afterPropertiesSet();
        return controller;
    }

    private AcknowledgeNotificationController acknowledgeNotificationController() throws Exception {
        final AcknowledgeNotificationController controller = new AcknowledgeNotificationController();
        controller.setWebNotificationRepository(notificationRepository);
        controller.setRedirectView("/notification/list.htm");
        controller.afterPropertiesSet();
        return controller;
    }
}
