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

import com.google.common.base.Objects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

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
