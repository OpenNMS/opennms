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

import static org.opennms.features.config.dao.api.ConfigDefinition.DEFAULT_CONFIG_ID;
import static org.opennms.netmgt.dao.jaxb.DefaultProvisiondConfigurationDao.CONFIG_NAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.util.ConfigConvertUtil;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;

public class ProvisiondConfigurationValidationCallbackTest {

    @Test
    public void testOk() {
        doTest(createConfig());
    }

    @Test
    public void testEmptyConfigDefs() {
        ProvisiondConfiguration config = createConfig();
        config.setRequisitionDefs(Collections.emptyList());
        doTest(config);
    }

    @Test(expected = ValidationException.class)
    public void testInvalid() {
        ProvisiondConfiguration config = createConfig();
        // invalid
        config.getRequisitionDefs().get(1).setCronSchedule("0 0 * * * *");

        doTest(config);
    }

    @Test(expected = ValidationException.class)
    public void testConfigIsNull() {
        doTest(null);
    }

    @Test(expected = ValidationException.class)
    public void testNullNameInAConfigDef() {
        ProvisiondConfiguration config = createConfig();
        // add a def with null as name
        RequisitionDef reqDef = new RequisitionDef();
        reqDef.setCronSchedule("0 0 * * * ?");
        config.getRequisitionDefs().add(reqDef);
        doTest(config);
    }

    @Test(expected = ValidationException.class)
    public void testTwoDefsWithSameName() {
        ProvisiondConfiguration config = createConfig();
        // invalid
        config.getRequisitionDefs().get(0).setImportName(config.getRequisitionDefs().get(1).getImportName().get());

        doTest(config);
    }

    @Test(expected = ValidationException.class)
    public void testBlankName() {
        ProvisiondConfiguration config = createConfig();
        // invalid
        config.getRequisitionDefs().get(0).setImportName(" \t\n");

        doTest(config);
    }

    private static void doTest(ProvisiondConfiguration config) {
        ProvisiondConfigurationValidationCallback callback = new ProvisiondConfigurationValidationCallback();
        JSONObject json;
        if (config == null) {
            json = null;
        } else {
            json = new JSONObject(ConfigConvertUtil.objectToJson(config));
        }
        ConfigUpdateInfo info = new ConfigUpdateInfo(CONFIG_NAME, DEFAULT_CONFIG_ID, json);
        callback.accept(info);
    }

    private static ProvisiondConfiguration createConfig() {

        ProvisiondConfiguration config = new ProvisiondConfiguration();
        List<RequisitionDef> requisitionDefs = new ArrayList<>();

        RequisitionDef def1 = new RequisitionDef();
        def1.setCronSchedule("0 0 * * * ?");
        def1.setImportName("name1");
        requisitionDefs.add(def1);

        RequisitionDef def2 = new RequisitionDef();
        def2.setCronSchedule("1 1 * * * ?");
        def2.setImportName("name2");
        requisitionDefs.add(def2);

        config.setRequisitionDefs(requisitionDefs);

        return config;
    }

}
