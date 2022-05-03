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
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;
import org.opennms.netmgt.xml.event.Event;

import java.util.ArrayList;
import java.util.List;

public class ProvisiondConfigurationValidationCallbackTest {
    private String configName = "provisiond";

    @Test(expected = ValidationException.class)
    public void testInvalid() {
        ProvisiondConfigurationValidationCallback callback = new ProvisiondConfigurationValidationCallback();

        ProvisiondConfiguration config = new ProvisiondConfiguration();
        List<RequisitionDef> requisitionDefs = new ArrayList<>();
        RequisitionDef def = new RequisitionDef();
        def.setCronSchedule("0 0 * * * ?");
        requisitionDefs.add(def);
        RequisitionDef def2 = new RequisitionDef();
        // invalid
        def.setCronSchedule("0 0 * * * *");
        requisitionDefs.add(def2);
        config.setRequisitionDefs(requisitionDefs);
        JSONObject json = new JSONObject(ConfigConvertUtil.objectToJson(config));

        ConfigUpdateInfo info = new ConfigUpdateInfo(configName, "default", json);
        callback.accept(info);
    }

    @Test(expected = ValidationException.class)
    public void testNullConfig() {
        ProvisiondConfigurationValidationCallback callback = new ProvisiondConfigurationValidationCallback();
        ConfigUpdateInfo info = new ConfigUpdateInfo(configName, "default", null );
        callback.accept(info);
    }
}
