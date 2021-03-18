/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2021 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.model.v2;

import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlRootElement(name="isisElementNode")
@JsonRootName("isisElementNode")
public class IsisElementNodeDTO {
    private String isisSysID;

    private String isisSysAdminState;

    private String isisCreateTime;

    private String isisLastPollTime;

    @XmlElement(name="isisSysID")
    @JsonProperty("isisSysID")
    public String getIsisSysID() {
        return isisSysID;
    }

    public void setIsisSysID(String isisSysID) {
        this.isisSysID = isisSysID;
    }

    public IsisElementNodeDTO withIsisSysID(String isisSysID) {
        this.isisSysID = isisSysID;
        return this;
    }

    @XmlElement(name="isisSysAdminState")
    @JsonProperty("isisSysAdminState")
    public String getIsisSysAdminState() {
        return isisSysAdminState;
    }

    public void setIsisSysAdminState(String isisSysAdminState) {
        this.isisSysAdminState = isisSysAdminState;
    }

    public IsisElementNodeDTO withIsisSysAdminState(String isisSysAdminState) {
        this.isisSysAdminState = isisSysAdminState;
        return this;
    }

    @XmlElement(name="isisCreateTime")
    @JsonProperty("isisCreateTime")
    public String getIsisCreateTime() {
        return isisCreateTime;
    }

    public void setIsisCreateTime(String isisCreateTime) {
        this.isisCreateTime = isisCreateTime;
    }

    public IsisElementNodeDTO withIsisCreateTime(String isisCreateTime) {
        this.isisCreateTime = isisCreateTime;
        return this;
    }

    @XmlElement(name="isisLastPollTime")
    @JsonProperty("isisLastPollTime")
    public String getIsisLastPollTime() {
        return isisLastPollTime;
    }

    public void setIsisLastPollTime(String isisLastPollTime) {
        this.isisLastPollTime = isisLastPollTime;
    }

    public IsisElementNodeDTO withIsisLastPollTime(String isisLastPollTime) {
        this.isisLastPollTime = isisLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IsisElementNodeDTO that = (IsisElementNodeDTO) o;
        return Objects.equals(isisSysID, that.isisSysID) && Objects.equals(isisSysAdminState, that.isisSysAdminState) && Objects.equals(isisCreateTime, that.isisCreateTime) && Objects.equals(isisLastPollTime, that.isisLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isisSysID, isisSysAdminState, isisCreateTime, isisLastPollTime);
    }

    @Override
    public String toString() {
        return "IsisElementNodeDTO{" +
                "isisSysID='" + isisSysID + '\'' +
                ", isisSysAdminState='" + isisSysAdminState + '\'' +
                ", isisCreateTime='" + isisCreateTime + '\'' +
                ", isisLastPollTime='" + isisLastPollTime + '\'' +
                '}';
    }
}
