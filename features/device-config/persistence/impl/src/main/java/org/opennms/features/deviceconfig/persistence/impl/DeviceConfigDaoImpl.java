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

package org.opennms.features.deviceconfig.persistence.impl;

import org.opennms.features.deviceconfig.persistence.api.ConfigType;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.model.OnmsIpInterface;

import java.util.List;
import java.util.Optional;

public class DeviceConfigDaoImpl extends AbstractDaoHibernate<DeviceConfig, Long> implements DeviceConfigDao {

    public DeviceConfigDaoImpl() {
        super(DeviceConfig.class);
    }

    @Override
    public List<DeviceConfig> findConfigsForInterfaceSortedByDate(OnmsIpInterface ipInterface, String configType) {

        return find("from DeviceConfig dc where dc.ipInterface.id = ? AND configType = ? ORDER BY lastUpdated DESC",
                ipInterface.getId(), configType);
    }

    @Override
    public Optional<DeviceConfig> getLatestConfigForInterface(OnmsIpInterface ipInterface, String configType) {
        List<DeviceConfig> deviceConfigs =
                findObjects(DeviceConfig.class,
                        "from DeviceConfig dc where dc.ipInterface.id = ? AND configType = ? " +
                                "ORDER BY lastUpdated DESC LIMIT 1", ipInterface.getId(), configType);
        if (deviceConfigs != null && !deviceConfigs.isEmpty()) {
            return Optional.of(deviceConfigs.get(0));
        }
        return Optional.empty();
    }
}
