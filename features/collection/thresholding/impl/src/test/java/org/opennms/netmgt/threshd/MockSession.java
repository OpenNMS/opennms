/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
            when(mockKey.getResource()).thenReturn("resource");
            when(mockKey.getServiceName()).thenReturn("service");

            when(mockSession.getKey()).thenReturn(mockKey);

            when(mockSession.getBlobStore()).thenReturn(NoOpBlobStore.getInstance());
            ThresholdStateMonitor mockStateMonitor = new BlobStoreAwareMonitor(mockSession.getBlobStore());
            when(mockSession.getThresholdStateMonitor()).thenReturn(mockStateMonitor);

        }

        return mockSession;
    }
}
