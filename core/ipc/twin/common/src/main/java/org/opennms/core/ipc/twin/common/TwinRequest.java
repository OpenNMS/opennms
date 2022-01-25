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
