/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2022 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.deviceconfig.rest.api;

import java.util.Date;

public class DeviceConfigDTO {

    private final long id;
    private final int ipInterfaceId;
    private final byte[] config;
    private final String encoding;
    private final Date createdTime;

    public DeviceConfigDTO(long id, int ipInterfaceId, byte[] config, String encoding, Date createdTime) {
        this.id = id;
        this.ipInterfaceId = ipInterfaceId;
        this.config = config;
        this.encoding = encoding;
        this.createdTime = createdTime;
    }

    public long getId() {
        return id;
    }

    public int getIpInterfaceId() {
        return ipInterfaceId;
    }


    public byte[] getConfig() {
        return config;
    }

    public String getEncoding() {
        return encoding;
    }

    public Date getCreatedTime() {
        return createdTime;
    }
}
