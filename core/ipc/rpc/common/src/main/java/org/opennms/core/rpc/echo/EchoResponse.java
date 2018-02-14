/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.echo;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RpcResponse;

@XmlRootElement(name="echo-response")
@XmlAccessorType(XmlAccessType.NONE)
public class EchoResponse implements RpcResponse {

    @XmlAttribute(name="id")
    private Long id;

    @XmlAttribute(name="error")
    private String error;

    @XmlAttribute(name="message")
    private String message;

    public EchoResponse() { }

    public EchoResponse(String message) {
        this.message = message;
    }

    public EchoResponse(Throwable t) {
        this.error = RemoteExecutionException.toErrorMessage(t);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String getErrorMessage() {
        return error;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, error);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final EchoResponse other = (EchoResponse) obj;
        return Objects.equals(this.id, other.id) &&
                Objects.equals(this.message, other.message) &&
                Objects.equals(this.error, other.error);
    }

    @Override
    public String toString() {
        return String.format("EchoResponse[id=%d, message=%s, error=%s]",
                id, message, error);
    }
}
