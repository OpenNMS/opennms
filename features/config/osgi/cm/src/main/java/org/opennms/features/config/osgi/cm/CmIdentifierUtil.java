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

package org.opennms.features.config.osgi.cm;

import static org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID;

import java.util.Objects;

import org.opennms.features.config.service.util.Substring;
import org.opennms.features.config.exception.ConfigRuntimeException;
import org.opennms.features.config.osgi.del.MigratedServices;
import org.opennms.features.config.service.api.ConfigUpdateInfo;

public class CmIdentifierUtil {

    private final static String DEFAULT_SUFFIX = "-" + DEFAULT_CONFIG_ID;

    /**
     * The delimiter between configName and configId is a dash (-).
     * However, there is no guarantee that the configName or the configId doesn't contain a dash as well.
     * Therefore, we have to check against the PIDS_MULTI_INSTANCE.
     */
    public static ConfigUpdateInfo pidToCmIdentifier(String pid) {
        Objects.requireNonNull(pid);
        String configName;
        String configId;
        if(pid.endsWith(DEFAULT_SUFFIX)) {
            configName =  new Substring(pid).getBeforeLast(DEFAULT_SUFFIX).toString();
            configId = DEFAULT_CONFIG_ID;
        } else {
            configName = MigratedServices.PIDS_MULTI_INSTANCE.stream().filter(pid::startsWith)
                    .findAny()
                    .orElseThrow(() -> new ConfigRuntimeException(String.format("Pid=%s could not be parsed into ConfigUpdateInfo", pid)));
            configId = new Substring(pid).getAfterLast(configName + "-").toString();
        }
        return new ConfigUpdateInfo(configName, configId);
    }

    public static String cmIdentifierToPid(ConfigUpdateInfo identifier) {
        Objects.requireNonNull(identifier);
        StringBuilder b = new StringBuilder(identifier.getConfigName());
        if (!DEFAULT_CONFIG_ID.equals(identifier.getConfigId())) {
            b.append("-")
                    .append(identifier.getConfigId());
        }
        return b.toString();
    }
}
