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

@XmlRootElement(name="cdpElementNode")
@JsonRootName("cdpElementNode")
public class CdpElementNodeDTO {
    private String cdpGlobalRun;

    private String cdpGlobalDeviceId;

    private String cdpGlobalDeviceIdFormat;

    private String cdpCreateTime;

    private String cdpLastPollTime;

    @XmlElement(name="cdpGlobalRun")
    @JsonProperty("cdpGlobalRun")
    public String getCdpGlobalRun() {
        return cdpGlobalRun;
    }

    public void setCdpGlobalRun(String cdpGlobalRun) {
        this.cdpGlobalRun = cdpGlobalRun;
    }

    public CdpElementNodeDTO withCdpGlobalRun(String cdpGlobalRun) {
        this.cdpGlobalRun = cdpGlobalRun;
        return this;
    }

    @XmlElement(name="cdpGlobalDeviceId")
    @JsonProperty("cdpGlobalDeviceId")
    public String getCdpGlobalDeviceId() {
        return cdpGlobalDeviceId;
    }

    public void setCdpGlobalDeviceId(String cdpGlobalDeviceId) {
        this.cdpGlobalDeviceId = cdpGlobalDeviceId;
    }

    public CdpElementNodeDTO withCdpGlobalDeviceId(String cdpGlobalDeviceId) {
        this.cdpGlobalDeviceId = cdpGlobalDeviceId;
        return this;
    }

    @XmlElement(name="cdpGlobalDeviceIdFormat")
    @JsonProperty("cdpGlobalDeviceIdFormat")
    public String getCdpGlobalDeviceIdFormat() {
        return cdpGlobalDeviceIdFormat;
    }

    public void setCdpGlobalDeviceIdFormat(String cdpGlobalDeviceIdFormat) {
        this.cdpGlobalDeviceIdFormat = cdpGlobalDeviceIdFormat;
    }

    public CdpElementNodeDTO withCdpGlobalDeviceIdFormat(String cdpGlobalDeviceIdFormat) {
        this.cdpGlobalDeviceIdFormat = cdpGlobalDeviceIdFormat;
        return this;
    }

    @XmlElement(name="cdpCreateTime")
    @JsonProperty("cdpCreateTime")
    public String getCdpCreateTime() {
        return cdpCreateTime;
    }

    public void setCdpCreateTime(String cdpCreateTime) {
        this.cdpCreateTime = cdpCreateTime;
    }

    public CdpElementNodeDTO withCdpCreateTime(String cdpCreateTime) {
        this.cdpCreateTime = cdpCreateTime;
        return this;
    }

    @XmlElement(name="cdpLastPollTime")
    @JsonProperty("cdpLastPollTime")
    public String getCdpLastPollTime() {
        return cdpLastPollTime;
    }

    public void setCdpLastPollTime(String cdpLastPollTime) {
        this.cdpLastPollTime = cdpLastPollTime;
    }

    public CdpElementNodeDTO withCdpLastPollTime(String cdpLastPollTime) {
        this.cdpLastPollTime = cdpLastPollTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CdpElementNodeDTO that = (CdpElementNodeDTO) o;
        return Objects.equals(cdpGlobalRun, that.cdpGlobalRun) && Objects.equals(cdpGlobalDeviceId, that.cdpGlobalDeviceId) && Objects.equals(cdpGlobalDeviceIdFormat, that.cdpGlobalDeviceIdFormat) && Objects.equals(cdpCreateTime, that.cdpCreateTime) && Objects.equals(cdpLastPollTime, that.cdpLastPollTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cdpGlobalRun, cdpGlobalDeviceId, cdpGlobalDeviceIdFormat, cdpCreateTime, cdpLastPollTime);
    }

    @Override
    public String toString() {
        return "CdpElementNodeDTO{" +
                "cdpGlobalRun='" + cdpGlobalRun + '\'' +
                ", cdpGlobalDeviceId='" + cdpGlobalDeviceId + '\'' +
                ", cdpGlobalDeviceIdFormat='" + cdpGlobalDeviceIdFormat + '\'' +
                ", cdpCreateTime='" + cdpCreateTime + '\'' +
                ", cdpLastPollTime='" + cdpLastPollTime + '\'' +
                '}';
    }
}
