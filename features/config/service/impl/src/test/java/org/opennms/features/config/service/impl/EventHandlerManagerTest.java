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
package org.opennms.features.config.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.EventType;

public class EventHandlerManagerTest {

    private EventHandlerManager manager;

    @Before
    public void setUp() {
        this.manager = new EventHandlerManager();
    }

    @Test
    public void shouldHandleWildcards() {
        Consumer<ConfigUpdateInfo> noWildcardA = Mockito.mock(Consumer.class);
        Consumer<ConfigUpdateInfo> wildcardA = Mockito.mock(Consumer.class);
        Consumer<ConfigUpdateInfo> noWildcardB = Mockito.mock(Consumer.class);
        Consumer<ConfigUpdateInfo> wildcardB = Mockito.mock(Consumer.class);
        manager.registerEventHandler(EventType.UPDATE, new ConfigUpdateInfo("a", "a"), noWildcardA);
        manager.registerEventHandler(EventType.UPDATE, new ConfigUpdateInfo("a", "*"), wildcardA);
        manager.registerEventHandler(EventType.UPDATE, new ConfigUpdateInfo("a", "b"), noWildcardB);
        manager.registerEventHandler(EventType.UPDATE, new ConfigUpdateInfo("b", "*"), wildcardB);

        // we expect to have the specific and the wildcard handlers for be called but no others
        manager.callEventHandlers(EventType.UPDATE, new ConfigUpdateInfo("a", "a"));

        verify(noWildcardA,times(1)).accept(any());
        verify(wildcardA,times(1)).accept(any());
        verify(noWildcardB,times(0)).accept(any());
        verify(wildcardB,times(0)).accept(any());
    }

}