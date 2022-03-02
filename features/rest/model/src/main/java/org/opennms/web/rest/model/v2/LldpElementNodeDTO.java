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

@XmlRootElement(name="lldpElementNode")
@JsonRootName("lldpElementNode")
public class LldpElementNodeDTO {

    private String lldpChassisId;

    private String lldpSysName;

    private String lldpCreateTime;

    private String lldpLastPollTime;

    @XmlElement(name="lldpChassisId")
    @JsonProperty("lldpChassisId")
    public String getLldpChassisId() {
        return lldpChassisId;
    }

    public void setLldpChassisId(String lldpChassisId) {
        this.lldpChassisId = lldpChassisId;
    }

    public LldpElementNodeDTO withLldpChassisId(String lldpChassisId) {
        this.lldpChassisId = lldpChassisId;
        return this;
    }

    @XmlElement(name="lldpSysName")
    @JsonProperty("lldpSysName")
    public String getLldpSysName() {
        return lldpSysName;
    }

    public void setLldpSysName(String lldpSysName) {
        this.lldpSysName = lldpSysName;
    }

    public LldpElementNodeDTO withLldpSysName(String lldpSysName) {
        this.lldpSysName = lldpSysName;
        return this;
    }

    @XmlElement(name="lldpCreateTime")
    @JsonProperty("lldpCreateTime")
    public String getLldpCreateTime() {
        return lldpCreateTime;
    }

    public void setLldpCreateTime(String lldpCreateTime) {
        this.lldpCreateTime = lldpCreateTime;
    }

    public LldpElementNodeDTO withLldpCreateTime(String lldpCreateTime) {
        this.lldpCreateTime = lldpCreateTime;
        return this;
    }

    @XmlElement(name="lldpLastPollTime")
    @JsonProperty("lldpLastPollTime")
    public String getLldpLastPollTime() {
        return lldpLastPollTime;
    }

    public void setLldpLastPollTime(String lldpLastPollTime) {
        this.lldpLastPollTime = lldpLastPollTime;
    }

    public LldpElementNodeDTO withLldpLastPollTime(String lldpLastPollTime) {
        this.lldpLastPollTime = lldpLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LldpElementNodeDTO that = (LldpElementNodeDTO) o;
        return Objects.equals(lldpChassisId, that.lldpChassisId) && Objects.equals(lldpSysName, that.lldpSysName) && Objects.equals(lldpCreateTime, that.lldpCreateTime) && Objects.equals(lldpLastPollTime, that.lldpLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lldpChassisId, lldpSysName, lldpCreateTime, lldpLastPollTime);
    }

    @Override
    public String toString() {
        return "LldpElementNodeDTO{" +
                "lldpChassisId='" + lldpChassisId + '\'' +
                ", lldpSysName='" + lldpSysName + '\'' +
                ", lldpCreateTime='" + lldpCreateTime + '\'' +
                ", lldpLastPollTime='" + lldpLastPollTime + '\'' +
                '}';
    }
}
