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

package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.enlinkd.model.LldpLink;

public class TimeTetraLldpLink  {
    private Integer tmnxLldpRemLocalDestMACAddress;
    private LldpLink lldpLink = new LldpLink();

    public TimeTetraLldpLink() {
    }

    public LldpLink getLldpLink() {
        return lldpLink;
    }

    public void setLldpLink(LldpLink lldpLink) {
        this.lldpLink = lldpLink;
    }

    public Integer getTmnxLldpRemLocalDestMACAddress() {
        return tmnxLldpRemLocalDestMACAddress;
    }

    public void setTmnxLldpRemLocalDestMACAddress(Integer tmnxLldpRemLocalDestMACAddress) {
        this.tmnxLldpRemLocalDestMACAddress = tmnxLldpRemLocalDestMACAddress;
    }
}
