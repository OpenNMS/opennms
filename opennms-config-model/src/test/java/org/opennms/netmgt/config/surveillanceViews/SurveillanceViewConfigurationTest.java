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
package org.opennms.netmgt.config.surveillanceViews;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class SurveillanceViewConfigurationTest extends XmlTestNoCastor<SurveillanceViewConfiguration> {

    public SurveillanceViewConfigurationTest(SurveillanceViewConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/surveillance-views.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "<surveillance-view-configuration \n" + 
                "  xmlns:this=\"http://www.opennms.org/xsd/config/surveillance-views\" \n" + 
                "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" + 
                "  xsi:schemaLocation=\"http://www.opennms.org/xsd/config/surveillance-views http://www.opennms.org/xsd/config/surveillance-views.xsd\"\n" + 
                "  default-view=\"default\" >\n" + 
                "  <views >\n" + 
                "    <view name=\"default\" refresh-seconds=\"300\" >\n" + 
                "      <rows>\n" + 
                "        <row-def label=\"Routers\" >\n" + 
                "          <category name=\"Routers\"/>\n" + 
                "        </row-def>\n" + 
                "        <row-def label=\"Switches\" >\n" + 
                "          <category name=\"Switches\" />\n" + 
                "        </row-def>\n" + 
                "        <row-def label=\"Servers\" >\n" + 
                "          <category name=\"Servers\" />\n" + 
                "        </row-def>\n" + 
                "      </rows>\n" + 
                "      <columns>\n" + 
                "        <column-def label=\"PROD\" >\n" + 
                "          <category name=\"Production\" />\n" + 
                "        </column-def>\n" + 
                "        <column-def label=\"TEST\" >\n" + 
                "          <category name=\"Test\" />\n" + 
                "        </column-def>\n" + 
                "        <column-def label=\"DEV\" >\n" + 
                "          <category name=\"Development\" />\n" + 
                "        </column-def>\n" + 
                "      </columns>\n" + 
                "    </view>\n" + 
                "  </views>\n" + 
                "</surveillance-view-configuration>"
            },
            {
                getConfig(),
                "<surveillance-view-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/surveillance-views\" default-view=\"default\">\n" + 
                "   <views>\n" + 
                "      <view name=\"default\" refresh-seconds=\"300\">\n" + 
                "         <rows>\n" + 
                "            <row-def label=\"Routers\">\n" + 
                "               <category name=\"Routers\"/>\n" + 
                "            </row-def>\n" + 
                "            <row-def label=\"Switches\">\n" + 
                "               <category name=\"Switches\"/>\n" + 
                "            </row-def>\n" + 
                "            <row-def label=\"Servers\">\n" + 
                "               <category name=\"Servers\"/>\n" + 
                "            </row-def>\n" + 
                "         </rows>\n" + 
                "         <columns>\n" + 
                "            <column-def label=\"PROD\">\n" + 
                "               <category name=\"Production\"/>\n" + 
                "            </column-def>\n" + 
                "            <column-def label=\"TEST\">\n" + 
                "               <category name=\"Test\"/>\n" + 
                "            </column-def>\n" + 
                "            <column-def label=\"DEV\">\n" + 
                "               <category name=\"Development\"/>\n" + 
                "            </column-def>\n" + 
                "         </columns>\n" + 
                "      </view>\n" + 
                "   </views>\n" + 
                "</surveillance-view-configuration>"
            }
        });
    }

    private static SurveillanceViewConfiguration getConfig() {
        SurveillanceViewConfiguration config = new SurveillanceViewConfiguration();
        config.setDefaultView("default");

        View view = new View();
        view.setName("default");
        view.setRefreshSeconds(300);
        config.addView(view);

        view.addRow("Routers", "Routers");
        view.addRow("Switches", "Switches");
        view.addRow("Servers", "Servers");

        view.addColumn("PROD", "Production");
        view.addColumn("TEST", "Test");
        view.addColumn("DEV", "Development");

        return config;
    }
}

