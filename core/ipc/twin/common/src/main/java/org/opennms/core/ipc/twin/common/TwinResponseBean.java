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
import java.util.StringJoiner;

public class TwinResponseBean extends TwinRequestBean {


    public TwinResponseBean() {
    }

    public TwinResponseBean(String key, String location, byte[] object) {
        super(key, location);
        this.object = object;
    }
    public TwinResponseBean(String key, String location) {
        super(key, location);
    }

    private byte[] object;

    private int version;

    private boolean isRpc;

    private boolean isPatch;

    private String sessionId;

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

    public boolean isRpc() {
        return isRpc;
    }

    public void setRpc(boolean rpc) {
        isRpc = rpc;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TwinResponseBean)) return false;
        if (!super.equals(o)) return false;
        TwinResponseBean that = (TwinResponseBean) o;
        return version == that.version && isRpc == that.isRpc && Objects.equal(object, that.object) && Objects.equal(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), object, version, isRpc, sessionId);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TwinResponseBean.class.getSimpleName() + "[", "]")
                .add("key='" + key + "'")
                .add("location='" + location + "'")
                .add("object=" + Arrays.toString(object))
                .add("version=" + version)
                .add("isRpc=" + isRpc)
                .add("sessionId='" + sessionId + "'")
                .toString();
    }
}
