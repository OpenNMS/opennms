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

package org.opennms.features.backup.api;

import com.google.common.base.Objects;
import org.opennms.core.rpc.api.RpcResponse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "backup-response")
@XmlAccessorType(XmlAccessType.NONE)
public class BackupResponseDTO implements RpcResponse {

    public BackupResponseDTO() {
    }

    public BackupResponseDTO(String error) {
        this.error = error;
    }

    @XmlAttribute(name = "id")
    private Long id;

    @XmlAttribute(name = "error")
    private String error;

    @XmlElement(name = "response", required = false)
    private String response;

    @Override
    public String getErrorMessage() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BackupResponseDTO)) return false;
        BackupResponseDTO that = (BackupResponseDTO) o;
        return Objects.equal(id, that.id) && Objects.equal(error, that.error) && Objects.equal(response, that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, error, response);
    }
}
