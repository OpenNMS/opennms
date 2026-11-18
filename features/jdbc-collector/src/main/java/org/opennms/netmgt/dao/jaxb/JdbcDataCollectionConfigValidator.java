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
package org.opennms.netmgt.dao.jaxb;

import org.json.JSONObject;
import org.opennms.features.config.exception.ConfigConversionException;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigValidator;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfigRules;

/**
 * Rejects JDBC data collection configurations that pass the schema but that the collector can't use: a column type
 * such as {@code timeticks} that the schema allows but {@link org.opennms.netmgt.collection.api.AttributeType}
 * doesn't know, and everything {@link JdbcDataCollectionConfigRules} checks, including the id of the configuration.
 *
 * <p>The configuration manager finds it with {@link java.util.ServiceLoader} and runs it on every write: the v2 and
 * the {@code /rest/cm} REST APIs, the DAO, and the import of the configuration upgrade, which runs before the DAO
 * has started.</p>
 */
public class JdbcDataCollectionConfigValidator implements ConfigValidator {

    @Override
    public String getConfigName() {
        return JdbcDataCollectionConfigDaoCm.CONFIG_NAME;
    }

    @Override
    public void validate(final ConfigUpdateInfo info) {
        final String idProblem = JdbcDataCollectionConfigRules.validateSourceId(info.getConfigId());
        if (idProblem != null) {
            throw new ValidationException(idProblem);
        }
        final JSONObject json = info.getConfigJson();
        if (json == null) {
            throw new ValidationException(String.format("%s config %s is empty.", info.getConfigName(), info.getConfigId()));
        }
        final JdbcDataCollectionConfig config;
        try {
            config = ConfigConvertUtil.jsonToObject(json.toString(), JdbcDataCollectionConfig.class);
        } catch (final ConfigConversionException e) {
            throw new ValidationException(JdbcDataCollectionConfigDaoCm.rootCauseMessage(e));
        }
        final String problem = JdbcDataCollectionConfigRules.validate(config);
        if (problem != null) {
            throw new ValidationException(problem);
        }
    }
}
