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
package org.opennms.netmgt.config.siteStatusViews;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class SiteStatusViewConfigurationTest extends XmlTestNoCastor<SiteStatusViewConfiguration> {

    public SiteStatusViewConfigurationTest(SiteStatusViewConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/site-status-views.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "<site-status-view-configuration \n" + 
                "    xmlns:this=\"http://www.opennms.org/xsd/config/site-status-views\" \n" + 
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" + 
                "    xsi:schemaLocation=\"http://www.opennms.org/xsd/config/site-status-views http://www.opennms.org/xsd/config/site-status-views.xsd \" \n" + 
                "    default-view=\"default\">\n" + 
                "  <views>\n" + 
                "    <view name=\"default\" >\n" + 
                "      <rows>\n" + 
                "        <row-def label=\"Routers\">\n" + 
                "          <category name=\"Routers\"/>\n" + 
                "        </row-def>\n" + 
                "        <row-def label=\"Switches\">\n" + 
                "          <category name=\"Switches\"/>\n" + 
                "        </row-def>\n" + 
                "        <row-def label=\"Servers\">\n" + 
                "          <category name=\"Servers\"/>\n" + 
                "        </row-def>\n" + 
                "      </rows>\n" + 
                "    </view>\n" + 
                "  </views>\n" + 
                "</site-status-view-configuration>"
            }
        });
    }

    private static SiteStatusViewConfiguration getConfig() {
        SiteStatusViewConfiguration config = new SiteStatusViewConfiguration();
        config.setDefaultView("default");
        
        final List<View> views = new ArrayList<>();
        final View view = new View();
        view.setName("default");
        views.add(view);
        config.setViews(views);

        view.addRow("Routers", "Routers");
        view.addRow("Switches", "Switches");
        view.addRow("Servers", "Servers");

        return config;
    }
}
