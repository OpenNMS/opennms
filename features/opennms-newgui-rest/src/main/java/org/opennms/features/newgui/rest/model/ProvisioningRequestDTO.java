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

package org.opennms.features.newgui.rest.model;

import java.util.List;

public class ProvisioningRequestDTO {
    private String batchName;
    private long scheduleTime;
    private List<IPAddressScanRequestDTO> discoverIPRanges;
    private List<SNMPFitRequestDTO> snmpConfigList;

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public List<IPAddressScanRequestDTO> getDiscoverIPRanges() {
        return discoverIPRanges;
    }

    public void setDiscoverIPRanges(List<IPAddressScanRequestDTO> discoverIPRanges) {
        this.discoverIPRanges = discoverIPRanges;
    }

    public List<SNMPFitRequestDTO> getSnmpConfigList() {
        return snmpConfigList;
    }

    public void setSnmpConfigList(List<SNMPFitRequestDTO> snmpConfigList) {
        this.snmpConfigList = snmpConfigList;
    }

    public long getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(long scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    @Override
    public String toString() {
        return "ProvisioningRequestDTO{" +
                "batchName='" + batchName + '\'' +
                ", scheduleTime=" + scheduleTime +
                ", discoverIPRanges=" + discoverIPRanges +
                ", snmpConfigList=" + snmpConfigList +
                '}';
    }
}
