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
package org.opennms.features.config.convert;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.netmgt.config.provisiond.ProvisiondConfiguration;
import org.opennms.netmgt.config.provisiond.RequisitionDef;

public class ProvisiondConfigurationTest extends CmConfigTest<ProvisiondConfiguration> {

    public ProvisiondConfigurationTest(ProvisiondConfiguration sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, "provisiond-configuration.xsd", "provisiond-configuration");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<provisiond-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/provisiond-configuration\" importThreads=\"99\">\n" + 
                "   <requisition-def import-url-resource=\"dns://localhost/localhost\" import-name=\"localhost\" rescan-existing=\"true\">\n" + 
                "      <cron-schedule>0 0 0 * * ? *</cron-schedule>\n" + 
                "   </requisition-def>\n" + 
                "</provisiond-configuration>"
            },
            {
                new ProvisiondConfiguration(),
                "<provisiond-configuration/>"
            }
        });
    }

    private static ProvisiondConfiguration getConfig() {
        ProvisiondConfiguration config = new ProvisiondConfiguration();
        config.setImportThreads(99L);
        
        RequisitionDef def = new RequisitionDef();
        def.setImportName("localhost");
        def.setImportUrlResource("dns://localhost/localhost");
        def.setCronSchedule("0 0 0 * * ? *");
        def.setRescanExisting("true");
        config.addRequisitionDef(def);

        return config;
    }
}
