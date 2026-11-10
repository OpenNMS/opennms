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
package org.opennms.netmgt.config.viewsdisplay;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class ViewinfoTest extends XmlTestNoCastor<Viewinfo> {

    public ViewinfoTest(Viewinfo sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/viewsdisplay.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getViewinfo(),
                "<viewinfo>\n" + 
                "        <view>\n" + 
                "                <view-name>WebConsoleView</view-name>\n" + 
                "                <section>\n" + 
                "                        <section-name><![CDATA[Categories]]></section-name>\n" + 
                "                        <category><![CDATA[Network Interfaces]]></category>\n" + 
                "                        <category><![CDATA[Web Servers]]></category>\n" +
                "                </section>\n" + 
                "                <section>\n" + 
                "                        <section-name><![CDATA[Total]]></section-name>\n" + 
                "                        <category><![CDATA[Overall Service Availability]]></category>\n" + 
                "                </section>\n" + 
                "        </view>\n" + 
                "</viewinfo>"
            },
            {
                new Viewinfo(),
                "<viewinfo/>"
            }
        });
    }

    private static Viewinfo getViewinfo() {
        Viewinfo viewInfo = new Viewinfo();
        View view = new View();
        view.setViewName("WebConsoleView");
        viewInfo.addView(view);

        Section cats = new Section();
        cats.setSectionName("Categories");
        cats.addCategory("Network Interfaces");
        cats.addCategory("Web Servers");
        view.addSection(cats);

        Section total = new Section();
        total.setSectionName("Total");
        total.addCategory("Overall Service Availability");
        view.addSection(total);

        return viewInfo;
    }
}
