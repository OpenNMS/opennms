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
package org.opennms.config.upgrade;

import java.util.Objects;
import java.util.Optional;

import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One-time, idempotent backfill of server-assigned ids onto trapd SNMPv3 users
 * that predate the id feature (NMS-19723). Runs at every startup from
 * {@link UpgradeConfigService} after the CM changelog has applied the v1.1
 * trapd schema. Fills only id-less users, so it is a no-op once every user has
 * an id (and self-repairs an id-less config restored after a DB reset on the
 * next boot). New users added at runtime are stamped by
 * DefaultTrapdConfigDao.replaceConfig(), not here.
 */
public class TrapdSnmpv3UserIdBackfill {

    private static final Logger LOG = LoggerFactory.getLogger(TrapdSnmpv3UserIdBackfill.class);

    static final String CONFIG_NAME = "trapd-config";

    private final ConfigurationManagerService cm;

    public TrapdSnmpv3UserIdBackfill(final ConfigurationManagerService cm) {
        this.cm = Objects.requireNonNull(cm);
    }

    /**
     * @return {@code true} if at least one id was assigned and the config was
     *         persisted; {@code false} if there was nothing to do.
     */
    public boolean execute() {
        final Optional<String> json = cm.getJSONStrConfiguration(CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID);
        if (json.isEmpty()) {
            LOG.debug("No trapd configuration registered in CM; skipping SNMPv3 user id backfill.");
            return false;
        }

        final TrapdConfiguration config = ConfigConvertUtil.jsonToObject(json.get(), TrapdConfiguration.class);
        if (config == null || !config.ensureSnmpv3UserIds()) {
            return false;
        }

        cm.updateConfiguration(CONFIG_NAME, ConfigDefinition.DEFAULT_CONFIG_ID,
                new JsonAsString(ConfigConvertUtil.objectToJson(config)), true);
        LOG.info("Backfilled ids onto trapd SNMPv3 user(s) lacking one.");
        return true;
    }
}
