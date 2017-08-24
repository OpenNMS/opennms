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

package org.opennms.netmgt.config.charts;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class ChartConfigurationTest extends XmlTestNoCastor<ChartConfiguration> {

    public ChartConfigurationTest(final ChartConfiguration sampleObject,
            final String sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/chart-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getChartConfig(),
                "<chart-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/charts\">\n" + 
                "   <bar-chart name=\"some-bar\" domain-axis-label=\"some-domain-label\" range-axis-label=\"some-range-label\">\n" + 
                "      <title value=\"some-title\"/>\n" + 
                "      <image-size>\n" + 
                "         <hz-size>\n" + 
                "            <pixels>100</pixels>\n" + 
                "         </hz-size>\n" + 
                "         <vt-size>\n" + 
                "            <pixels>200</pixels>\n" + 
                "         </vt-size>\n" + 
                "      </image-size>\n" + 
                "   </bar-chart>" + 
                "</chart-configuration>"
            },
            {
                new ChartConfiguration(),
                "<chart-configuration/>"
            }
        });
    }

    public static ChartConfiguration getChartConfig() {
        final ChartConfiguration chartConfig = new ChartConfiguration();

        BarChart barChart = new BarChart();
        barChart.setName("some-bar");
        barChart.setDomainAxisLabel("some-domain-label");
        barChart.setRangeAxisLabel("some-range-label");
        chartConfig.addBarChart(barChart);

        Title title = new Title();
        title.setValue("some-title");
        barChart.setTitle(title);
        
        ImageSize size = new ImageSize();
        HzSize hzSize = new HzSize();
        hzSize.setPixels(100);
        size.setHzSize(hzSize);
        VtSize vtSize = new VtSize();
        vtSize.setPixels(200);
        size.setVtSize(vtSize);
        barChart.setImageSize(size);

        return chartConfig;
    }
}
