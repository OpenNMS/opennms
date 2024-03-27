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
package org.opennms.netmgt.dao.jaxb.callback;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.opennms.features.config.exception.ConfigConversionException;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.quartz.CronExpression;

public class ProvisiondConfigurationValidationCallback implements Consumer<ConfigUpdateInfo> {
    @Override
    public void accept(ConfigUpdateInfo configUpdateInfo) {
        JSONObject json = configUpdateInfo.getConfigJson();
        if (json == null) {
            throw new ValidationException(String.format("%s config is empty.", configUpdateInfo.getConfigName()));
        }
        try {
            ProvisiondConfiguration provisiondConfiguration = ConfigConvertUtil.jsonToObject(json.toString(), ProvisiondConfiguration.class);
            this.validateCron(provisiondConfiguration);
            this.checkNames(provisiondConfiguration.getRequisitionDefs());
        } catch (ConfigConversionException e) {
            // convert to validation error so that the event handler can forward to RESTful API
            throw new ValidationException(e.getMessage());
        }
    }

    private static void validateCron(ProvisiondConfiguration provisiondConfiguration) {
        for (RequisitionDef r : provisiondConfiguration.getRequisitionDefs()) {
            r.getCronSchedule().ifPresent(exp -> {
                if (!CronExpression.isValidExpression(exp)) {
                    throw new ValidationException(String.format("Invalid cron expression. %s", exp));
                }
            });
        }
    }

    private static void checkNames(List<RequisitionDef> requisitionDefs) {
        if (requisitionDefs != null) {
            final Set<String> uniqueNames = requisitionDefs.stream()
                    .map(requisitionDef -> requisitionDef.getImportName().orElse(null))
                    .filter(name -> name != null && !name.isBlank()) //empty names ere not allowed
                    .collect(Collectors.toSet()); // Duplicates are not allowed (Set filters duplicates)
            if (uniqueNames.size() != requisitionDefs.size()) {
                throw new ValidationException(String.format("All requisition definitions must have unique names"));
            }
        }
    }
}