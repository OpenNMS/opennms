/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.client.rpc;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.dto.CollectionSetDTO;

@XmlRootElement(name = "collector-response")
@XmlAccessorType(XmlAccessType.NONE)
public class CollectorResponseDTO implements RpcResponse {

    @XmlAttribute(name="error")
    private String error;

    @XmlElement(name = "collection-set", type=CollectionSetDTO.class)
    private CollectionSet collectionSet;

    public CollectorResponseDTO() { }

    public CollectorResponseDTO(CollectionSet collectionSet) {
        this.collectionSet = collectionSet;
    }

    public CollectorResponseDTO(Throwable ex) {
        this.error = RemoteExecutionException.toErrorMessage(ex);
    }

    public CollectionSet getCollectionSet() {
        return collectionSet;
    }

    @Override
    public String getErrorMessage() {
        return error;
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, collectionSet);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof CollectorResponseDTO)) {
            return false;
        }
        CollectorResponseDTO other = (CollectorResponseDTO) obj;
        return Objects.equals(this.error, other.error)
                && Objects.equals(this.collectionSet, other.collectionSet);
    }


}
