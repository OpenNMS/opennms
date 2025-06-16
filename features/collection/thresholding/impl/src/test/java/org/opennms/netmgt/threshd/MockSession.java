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
package org.opennms.netmgt.threshd;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.opennms.features.distributed.kvstore.blob.noop.NoOpBlobStore;
import org.opennms.netmgt.threshd.api.ThresholdStateMonitor;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.netmgt.threshd.api.ThresholdingSessionKey;

public class MockSession {
    private static ThresholdingSession mockSession;

    static ThresholdingSession getSession() {
        if (mockSession == null) {
            mockSession = mock(ThresholdingSession.class);

            ThresholdingSessionKey mockKey = mock(ThresholdingSessionKey.class);
            when(mockKey.getNodeId()).thenReturn(1);
            when(mockKey.getLocation()).thenReturn("1.1.1.1");
            when(mockKey.getServiceName()).thenReturn("service");

            when(mockSession.getKey()).thenReturn(mockKey);

            when(mockSession.getBlobStore()).thenReturn(NoOpBlobStore.getInstance());
            ThresholdStateMonitor mockStateMonitor = new BlobStoreAwareMonitor(mockSession.getBlobStore());
            when(mockSession.getThresholdStateMonitor()).thenReturn(mockStateMonitor);

        }

        return mockSession;
    }
}
