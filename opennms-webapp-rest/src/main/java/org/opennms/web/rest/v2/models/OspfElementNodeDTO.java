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

package org.opennms.web.rest.v2.models;

import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlRootElement(name="ospfElementNode")
@JsonRootName("ospfElementNode")
public class OspfElementNodeDTO {

    @XmlElement(name="ospfRouterId")
    @JsonProperty("ospfRouterId")
    private String ospfRouterId;

    @XmlElement(name="ospfVersionNumber")
    @JsonProperty("ospfVersionNumber")
    private Integer ospfVersionNumber;

    @XmlElement(name="ospfAdminStat")
    @JsonProperty("ospfAdminStat")
    private String ospfAdminStat;

    @XmlElement(name="ospfCreateTime")
    @JsonProperty("ospfCreateTime")
    private String ospfCreateTime;

    @XmlElement(name="ospfLastPollTime")
    @JsonProperty("ospfLastPollTime")
    private String ospfLastPollTime;

    public String getOspfRouterId() {
        return ospfRouterId;
    }

    public void setOspfRouterId(String ospfRouterId) {
        this.ospfRouterId = ospfRouterId;
    }

    public OspfElementNodeDTO withOspfRouterId(String ospfRouterId) {
        this.ospfRouterId = ospfRouterId;
        return this;
    }

    public Integer getOspfVersionNumber() {
        return ospfVersionNumber;
    }

    public void setOspfVersionNumber(Integer ospfVersionNumber) {
        this.ospfVersionNumber = ospfVersionNumber;
    }

    public OspfElementNodeDTO withOspfVersionNumber(Integer ospfVersionNumber) {
        this.ospfVersionNumber = ospfVersionNumber;
        return this;
    }

    public String getOspfAdminStat() {
        return ospfAdminStat;
    }

    public void setOspfAdminStat(String ospfAdminStat) {
        this.ospfAdminStat = ospfAdminStat;
    }

    public OspfElementNodeDTO withOspfAdminStat(String ospfAdminStat) {
        this.ospfAdminStat = ospfAdminStat;
        return this;
    }

    public String getOspfCreateTime() {
        return ospfCreateTime;
    }

    public void setOspfCreateTime(String ospfCreateTime) {
        this.ospfCreateTime = ospfCreateTime;
    }

    public OspfElementNodeDTO withOspfCreateTime(String ospfCreateTime) {
        this.ospfCreateTime = ospfCreateTime;
        return this;
    }

    public String getOspfLastPollTime() {
        return ospfLastPollTime;
    }

    public void setOspfLastPollTime(String ospfLastPollTime) {
        this.ospfLastPollTime = ospfLastPollTime;
    }

    public OspfElementNodeDTO withOspfLastPollTime(String ospfLastPollTime) {
        this.ospfLastPollTime = ospfLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OspfElementNodeDTO that = (OspfElementNodeDTO) o;
        return Objects.equals(ospfRouterId, that.ospfRouterId) && Objects.equals(ospfVersionNumber, that.ospfVersionNumber) && Objects.equals(ospfAdminStat, that.ospfAdminStat) && Objects.equals(ospfCreateTime, that.ospfCreateTime) && Objects.equals(ospfLastPollTime, that.ospfLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ospfRouterId, ospfVersionNumber, ospfAdminStat, ospfCreateTime, ospfLastPollTime);
    }
}
