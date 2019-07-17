/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.ro;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.api.ThreshdConfig;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;

public class ThreshdConfigReadOnlyDao extends AbstractReadOnlyConfigDao<ThreshdConfiguration> implements ThreshdConfig {

    private final String fileName = ConfigFileConstants.getFileName(ConfigFileConstants.THRESHD_CONFIG_FILE_NAME);

    private final long cacheLengthInMillis = SystemProperties.getLong("org.opennms.netmgt.config.ro.ThreshdConfig.cacheTtlMillis", 1440);

    @Override
    public ThreshdConfiguration getConfig() {
        return getByKey(ThreshdConfiguration.class, fileName, cacheLengthInMillis);
    }

    @Override
    public ThreshdConfiguration getConfiguration() {
        return getConfig();
    }

    @Override
    public boolean interfaceInPackage(String iface, Package pkg) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean serviceInPackageAndEnabled(String svcName, Package pkg) {
        // TODO Auto-generated method stub
        return false;
    }

}
