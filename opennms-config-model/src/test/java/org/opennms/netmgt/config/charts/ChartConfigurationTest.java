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
