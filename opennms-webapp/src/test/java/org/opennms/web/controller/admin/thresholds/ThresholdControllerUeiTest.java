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
package org.opennms.web.controller.admin.thresholds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.config.threshd.Threshold;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.web.services.EventConfProgrammaticService;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * The threshold form's optional Triggered/Rearmed UEI fields: whitespace around a UEI is not part of it,
 * and a field of only whitespace means no custom UEI (NMS-20608).
 */
public class ThresholdControllerUeiTest {

    private ThresholdController controller;
    private EventConfDao eventConfDao;
    private EventConfProgrammaticService eventConfProgrammaticService;

    @Before
    public void setUp() {
        controller = new ThresholdController();
        eventConfDao = mock(EventConfDao.class);
        eventConfProgrammaticService = mock(EventConfProgrammaticService.class);
        controller.setEventConfDao(eventConfDao);
        controller.setEventConfProgrammaticService(eventConfProgrammaticService);
    }

    private static MockHttpServletRequest form(final String triggeredUei, final String rearmedUei) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("type", "high");
        request.setParameter("dsType", "node");
        request.setParameter("rearm", "1");
        request.setParameter("trigger", "1");
        request.setParameter("value", "2");
        request.setParameter("triggeredUEI", triggeredUei);
        request.setParameter("rearmedUEI", rearmedUei);
        return request;
    }

    @Test
    public void whitespaceOnlyUeisMeanNoCustomUei() {
        final Threshold threshold = new Threshold();
        controller.commonFinishEdit(form("   ", " \t "), threshold);

        assertFalse(threshold.getTriggeredUEI().isPresent());
        assertFalse(threshold.getRearmedUEI().isPresent());
        verify(eventConfProgrammaticService, never()).saveEventToDB(any(), anyString());
    }

    @Test
    public void paddedUeisAreTrimmed() {
        final Threshold threshold = new Threshold();
        controller.commonFinishEdit(form("  uei.opennms.org/test/exceeded \n", "\tuei.opennms.org/test/rearmed "), threshold);

        assertEquals(Optional.of("uei.opennms.org/test/exceeded"), threshold.getTriggeredUEI());
        assertEquals(Optional.of("uei.opennms.org/test/rearmed"), threshold.getRearmedUEI());
        verify(eventConfDao).getEvents("uei.opennms.org/test/exceeded");
        verify(eventConfDao).getEvents("uei.opennms.org/test/rearmed");
        final ArgumentCaptor<Event> saved = ArgumentCaptor.forClass(Event.class);
        verify(eventConfProgrammaticService, org.mockito.Mockito.times(2)).saveEventToDB(saved.capture(), anyString());
        assertEquals("uei.opennms.org/test/exceeded", saved.getAllValues().get(0).getUei());
        assertEquals("uei.opennms.org/test/rearmed", saved.getAllValues().get(1).getUei());
    }
}
