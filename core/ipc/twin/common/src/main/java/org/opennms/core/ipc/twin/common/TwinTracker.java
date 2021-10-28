/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.twin.common;

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

    private AtomicInteger version = new AtomicInteger(0);
    private byte[] obj;
    private String sessionId;
    private boolean isPatch;

    public TwinTracker(byte[] obj) {
        this.obj = obj;
        this.sessionId = UUID.randomUUID().toString();
    }

    public TwinTracker(byte[] obj, int version, String sessionId) {
        this(obj);
        this.version = new AtomicInteger(version);
        this.sessionId = sessionId;
    }

    public int getVersion() {
        return version.get();
    }

    public int incrementVersion() {
        return version.incrementAndGet();
    }

    public void setVersion(int version) {
        this.version = new AtomicInteger(version);
    }

    public byte[] getObj() {
        return obj;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isPatch() {
        return isPatch;
    }

    public void setPatch(boolean patch) {
        isPatch = patch;
    }

    public void setObj(byte[] obj) {
        this.obj = obj;
    }
}
