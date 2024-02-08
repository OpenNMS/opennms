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
package org.opennms.netmgt.mock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.netmgt.threshd.api.ThresholdingSessionKey;
import org.opennms.netmgt.threshd.api.ThresholdingSetPersister;

public class MockThresholdingService implements ThresholdingService {

    private final ThresholdingSetPersister persister = mock(ThresholdingSetPersister.class);
    
    @Override
    public ThresholdingSession createSession(int m_nodeId, String hostAddress, String serviceName, ServiceParameters serviceParameters) {
        ThresholdingSession mockSession = mock(ThresholdingSession.class);
        when(mockSession.getKey()).thenReturn(mock(ThresholdingSessionKey.class));
        return mockSession;
    }

    @Override
    public ThresholdingSetPersister getThresholdingSetPersister() {
        return persister;
    }
}
