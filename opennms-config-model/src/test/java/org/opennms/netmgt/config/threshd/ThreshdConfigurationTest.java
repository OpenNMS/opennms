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
package org.opennms.netmgt.config.threshd;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class ThreshdConfigurationTest extends XmlTestNoCastor<ThreshdConfiguration> {

    public ThreshdConfigurationTest(ThreshdConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/thresholding.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        ThreshdConfiguration threshdConfiguration = new ThreshdConfiguration();
        threshdConfiguration.setThreads(5);
        
        Package pkg = new Package();
        pkg.setName("mib2");
        threshdConfiguration.addPackage(pkg);

        Filter filter = new Filter();
        filter.setContent("IPADDR != '0.0.0.0'");
        pkg.setFilter(filter);

        return Arrays.asList(new Object[][] {
            {
                threshdConfiguration,
                "<threshd-configuration threads=\"5\">\n" + 
                "   <package name=\"mib2\">\n" + 
                "      <filter>IPADDR != '0.0.0.0'</filter>\n" + 
                "   </package>\n" + 
                "</threshd-configuration>"
            }
        });
    }
}
