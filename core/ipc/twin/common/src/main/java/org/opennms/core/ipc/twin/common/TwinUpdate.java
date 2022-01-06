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

import com.google.common.base.Objects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

public class TwinUpdate {


    private final TwinRequest twinRequest;

    private byte[] object;

    private int version;

    private boolean isPatch;

    private String sessionId;

    private Map<String, String> tracingInfo = new HashMap<>();

    public TwinUpdate() {
        this.twinRequest = new TwinRequest();
    }

    public TwinUpdate(String key, String location, byte[] object) {
        this.twinRequest = new TwinRequest(key, location);
        this.object = object;
    }
    public TwinUpdate(String key, String location) {
        this.twinRequest = new TwinRequest(key, location);
    }



    public byte[] getObject() {
        return object;
    }

    public void setObject(byte[] object) {
        this.object = object;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }


    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isPatch() {
        return isPatch;
    }

    public void setPatch(boolean patch) {
        isPatch = patch;
    }

    public void setLocation(String location) {
        twinRequest.setLocation(location);
    }

    public void setKey(String key) {
        twinRequest.setKey(key);
    }

    public String getLocation() {
        return twinRequest.getLocation();
    }

    public String getKey() {
        return twinRequest.getKey();
    }

    public Map<String, String> getTracingInfo() {
        return tracingInfo;
    }

    public void addTracingInfo(String key, String value) {
        this.tracingInfo.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TwinUpdate)) return false;
        TwinUpdate that = (TwinUpdate) o;
        return version == that.version && isPatch == that.isPatch
                && Objects.equal(twinRequest, that.twinRequest)
                && Objects.equal(object, that.object)
                && Objects.equal(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(twinRequest, object, version, isPatch, sessionId);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TwinUpdate.class.getSimpleName() + "[", "]")
                .add("twinRequest=" + twinRequest)
                .add("object=" + Arrays.toString(object))
                .add("version=" + version)
                .add("isPatch=" + isPatch)
                .add("sessionId='" + sessionId + "'")
                .toString();
    }

}
