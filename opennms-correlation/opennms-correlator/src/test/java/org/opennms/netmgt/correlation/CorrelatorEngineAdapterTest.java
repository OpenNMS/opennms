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
package org.opennms.netmgt.correlation;

import static org.mockito.ArgumentMatchers.any;
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
import org.opennms.netmgt.events.api.model.ImmutableMapper;
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
        listener.onEvent(ImmutableMapper.fromMutableEvent(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test")
                .addParam(EventConstants.PARM_DAEMON_NAME, "some-other-name")
                .getEvent()));
        // No invocations to add/remove event listeners should have been made
        verify(eventIpcManager, never()).removeEventListener(any(EventListener.class));
        verify(eventIpcManager, times(1)).addEventListener(any(EventListener.class), any(String.class));

        // Now send a reloadDaemonConfig event targeting our engine
        listener.onEvent(ImmutableMapper.fromMutableEvent(
                new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test")
                .addParam(EventConstants.PARM_DAEMON_NAME, listener.getName())
                .getEvent()));
        // The event listener should have been removed and re-added
        verify(eventIpcManager, times(1)).removeEventListener(any(EventListener.class));
        verify(eventIpcManager, times(2)).addEventListener(any(EventListener.class), any(String.class));
    }
}
