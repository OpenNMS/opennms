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
package org.opennms.core.ipc.twin.api;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This Tracks Twin Object Updates for a given SessionKey (key, location).
 * Twin Tracker consists of marshalled object( byte[]), version and sessionId.
 * Version is incremented whenever object updates.
 * sessionId is created only once per a SessionKey.
 * TwinTracker is created and updated by publisher and only consumed by Subscriber.
 * Subscriber will ignore any stale updates based on version but resets version whenever there is new SessionId.
 */
public class TwinTracker {

    private final AtomicInteger version;
    private byte[] obj;
    private final String sessionId;

    public TwinTracker(byte[] obj) {
        this(obj, 0, UUID.randomUUID().toString());
    }
    public TwinTracker(byte[] obj, int version, String sessionId) {
        this.obj = obj;
        this.version = new AtomicInteger(version);
        this.sessionId = Objects.requireNonNull(sessionId);
    }

    public int getVersion() {
        return version.get();
    }

    public byte[] getObj() {
        return obj;
    }

    public String getSessionId() {
        return sessionId;
    }


    public int update(byte[] obj) {
        this.obj = obj;
        return version.incrementAndGet();
    }
}
