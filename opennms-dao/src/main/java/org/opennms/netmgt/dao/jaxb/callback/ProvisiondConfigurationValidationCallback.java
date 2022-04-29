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

package org.opennms.netmgt.dao.jaxb.callback;

import org.json.JSONObject;
import org.opennms.features.config.exception.ConfigConversionException;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.quartz.CronExpression;

import java.util.function.Consumer;

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
        } catch (ConfigConversionException e) {
            // convert to validation error so that the event handler can forward to RESTful API
            throw new ValidationException(e.getMessage());
        }
    }

    private void validateCron(ProvisiondConfiguration provisiondConfiguration) {
        for (RequisitionDef r : provisiondConfiguration.getRequisitionDefs()) {
            r.getCronSchedule().ifPresent(exp -> {
                if (!CronExpression.isValidExpression(exp)) {
                    throw new ValidationException(String.format("Invalid cron expression. %s", exp));
                }
            });
        }
    }
}