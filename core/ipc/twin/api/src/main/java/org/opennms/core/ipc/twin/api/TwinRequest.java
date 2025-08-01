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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TwinRequest {

    protected String key;

    protected String location;

    private Map<String, String> tracingInfo = new HashMap<>();

    public TwinRequest(String key, String location) {
        this.key = key;
        this.location = location;
    }

    public TwinRequest() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
        if (o == null || getClass() != o.getClass()) return false;
        TwinRequest that = (TwinRequest) o;
        return Objects.equals(key, that.key) && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, location);
    }

    @Override
    public String toString() {
        return "TwinRequestBean{" +
                "key='" + key + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
