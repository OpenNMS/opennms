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
