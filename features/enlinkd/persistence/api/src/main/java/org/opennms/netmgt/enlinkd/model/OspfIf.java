/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.model;

import java.net.InetAddress;

public class OspfIf {
    private InetAddress ospfIfIpaddress;
    private InetAddress ospfIfNetmask;
    private Integer ospfIfAddressLessIf;
    private InetAddress ospfIfAreaId;
    private Integer ospfIfIfindex;

    public InetAddress getOspfIfIpaddress() {
        return ospfIfIpaddress;
    }

    public void setOspfIfIpaddress(InetAddress ospfIfIpaddress) {
        this.ospfIfIpaddress = ospfIfIpaddress;
    }

    public InetAddress getOspfIfNetmask() {
        return ospfIfNetmask;
    }

    public void setOspfIfNetmask(InetAddress ospfIfNetmask) {
        this.ospfIfNetmask = ospfIfNetmask;
    }

    public Integer getOspfIfAddressLessIf() {
        return ospfIfAddressLessIf;
    }

    public void setOspfIfAddressLessIf(Integer ospfIfAddressLessIf) {
        this.ospfIfAddressLessIf = ospfIfAddressLessIf;
    }

    public InetAddress getOspfIfAreaId() {
        return ospfIfAreaId;
    }

    public void setOspfIfAreaId(InetAddress ospfIfAreaId) {
        this.ospfIfAreaId = ospfIfAreaId;
    }

    public Integer getOspfIfIfindex() {
        return ospfIfIfindex;
    }

    public void setOspfIfIfindex(Integer ospfIfIfindex) {
        this.ospfIfIfindex = ospfIfIfindex;
    }
}
