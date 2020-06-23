/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
