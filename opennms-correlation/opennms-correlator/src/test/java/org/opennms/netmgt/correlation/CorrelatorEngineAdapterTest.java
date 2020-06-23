/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.model.events.EventBuilder;

public class CorrelatorEngineAdapterTest {

    /**
     * See NMS-10257 for further details.
     */
    @Test
    public void verifyEventListenerHandlingOnReload() throws Exception {
        // Setup the correlator with mocks
        EventIpcManager eventIpcManager = mock(EventIpcManager.class);
        CorrelationEngine engine = mock(CorrelationEngine.class);
        when(engine.getName()).thenReturn("test-engine");

        Correlator correlator = new Correlator();
        correlator.setEventIpcManager(eventIpcManager);
        correlator.setCorrelationEngines(Collections.singletonList(engine));
        correlator.afterPropertiesSet();

        // Grab the event listener for our engine
        ArgumentCaptor<EventListener> listenerCaptor = ArgumentCaptor.forClass(EventListener.class);
        verify(eventIpcManager, times(1)).addEventListener(listenerCaptor.capture(), eq(EventConstants.RELOAD_DAEMON_CONFIG_UEI));
        EventListener listener = listenerCaptor.getValue();

        // Now send a reloadDaemonConfig event targeting a different daemon
        listener.onEvent(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test")
                .addParam(EventConstants.PARM_DAEMON_NAME, "some-other-name")
                .getEvent());
        // No invocations to add/remove event listeners should have been made
        verify(eventIpcManager, never()).removeEventListener(any(EventListener.class));
        verify(eventIpcManager, times(1)).addEventListener(any(EventListener.class), any(String.class));

        // Now send a reloadDaemonConfig event targeting our engine
        listener.onEvent(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test")
                .addParam(EventConstants.PARM_DAEMON_NAME, listener.getName())
                .getEvent());
        // The event listener should have been removed and re-added
        verify(eventIpcManager, times(1)).removeEventListener(any(EventListener.class));
        verify(eventIpcManager, times(2)).addEventListener(any(EventListener.class), any(String.class));
    }
}
