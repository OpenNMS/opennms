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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConfigDaoImpl extends AbstractDaoHibernate<DeviceConfig, Long> implements DeviceConfigDao {

    private static Logger LOG = LoggerFactory.getLogger(DeviceConfigDaoImpl.class);

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

    @Override
    public void updateDeviceConfigContent(
            OnmsIpInterface ipInterface,
            String configType,
            String encoding,
            byte[] deviceConfigBytes,
            String fileName
    ) {
        Date currentTime = new Date();
        Optional<DeviceConfig> configOptional = getLatestConfigForInterface(ipInterface, configType);
        DeviceConfig lastDeviceConfig = configOptional.orElse(null);
        // Config retrieval succeeded
        if (lastDeviceConfig != null &&
            // Config didn't change, just update last updated field.
            Arrays.equals(lastDeviceConfig.getConfig(), deviceConfigBytes) &&
            Objects.equals(lastDeviceConfig.getFileName(), fileName)) {
            lastDeviceConfig.setLastUpdated(currentTime);
            lastDeviceConfig.setLastSucceeded(currentTime);
            saveOrUpdate(lastDeviceConfig);
            LOG.debug("Device config did not change - ipInterface: {}; type: {}", ipInterface, configType);
        } else if (lastDeviceConfig != null
                   // last config was failure, update config now.
                   && lastDeviceConfig.getConfig() == null) {
            lastDeviceConfig.setConfig(deviceConfigBytes);
            lastDeviceConfig.setFileName(fileName);
            lastDeviceConfig.setCreatedTime(currentTime);
            lastDeviceConfig.setLastUpdated(currentTime);
            lastDeviceConfig.setLastSucceeded(currentTime);
            lastDeviceConfig.setFailureReason(null);
            saveOrUpdate(lastDeviceConfig);
            LOG.info("Persisted device config - ipInterface: {}; type: {}", ipInterface, configType);
        } else {
            // Config changed, or there is no config for the device yet, create new entry.
            DeviceConfig deviceConfig = new DeviceConfig();
            deviceConfig.setConfig(deviceConfigBytes);
            deviceConfig.setFileName(fileName);
            deviceConfig.setCreatedTime(currentTime);
            deviceConfig.setIpInterface(ipInterface);
            deviceConfig.setEncoding(encoding);
            deviceConfig.setConfigType(configType);
            deviceConfig.setLastUpdated(currentTime);
            deviceConfig.setLastSucceeded(currentTime);
            saveOrUpdate(deviceConfig);
            LOG.info("Persisted changed device config - ipInterface: {}; type: {}", ipInterface, configType);
        }
    }

    @Override
    public void updateDeviceConfigFailure(
            OnmsIpInterface ipInterface,
            String configType,
            String encoding,
            String reason
    ) {
        Date currentTime = new Date();
        Optional<DeviceConfig> configOptional = getLatestConfigForInterface(ipInterface, configType);
        DeviceConfig lastDeviceConfig = configOptional.orElse(null);
        DeviceConfig deviceConfig;
        // If there is config already, update the same entry.
        if (lastDeviceConfig != null) {
            deviceConfig = lastDeviceConfig;
        } else {
            deviceConfig = new DeviceConfig();
            deviceConfig.setIpInterface(ipInterface);
            deviceConfig.setConfigType(configType);
            deviceConfig.setEncoding(encoding);
        }
        deviceConfig.setFailureReason(reason);
        deviceConfig.setLastFailed(currentTime);
        deviceConfig.setLastUpdated(currentTime);
        saveOrUpdate(deviceConfig);
        LOG.warn("Persisted device config backup failure - ipInterface: {}; type: {}; reason: {}", ipInterface, configType, reason);
    }
}
