/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.config.osgi.cm;

import static org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID;

import java.util.Objects;

import org.opennms.features.config.service.util.Substring;
import org.opennms.features.config.exception.ConfigRuntimeException;
import org.opennms.features.config.osgi.del.MigratedServices;
import org.opennms.features.config.service.api.ConfigUpdateInfo;

public class CmIdentifierUtil {

    /**
     * The delimiter between configName and configId is a dash (-).
     * However, there is no guarantee that the configName or the configId doesn't contain a dash as well.
     * Therefore, we have to check against the PIDS_MULTI_INSTANCE.
     */
    public static ConfigUpdateInfo pidToCmIdentifier(String pid) {
        Objects.requireNonNull(pid);
        String configName;
        String configId;

        // Single instance pid
        if (MigratedServices.PIDS_SINGLE_INSTANCE.contains(pid)) {
            configName =  pid;
            configId = DEFAULT_CONFIG_ID;
        } else {
            // multi instance pid: has a suffix
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
