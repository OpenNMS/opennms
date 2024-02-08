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
package org.opennms.netmgt.config.wmi.agent;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class WmiConfigTest extends XmlTestNoCastor<WmiConfig> {

    public WmiConfigTest(WmiConfig sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/wmi-config.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getWmiConfig(),
                /** Example from Wiki **/
                "<wmi-config retry=\"2\" timeout=\"1500\"\n" + 
                "        username=\"monitor\" domain=\"enter-domain-name\" password=\"enter-account-password\">\n" + 
                "    \n" + 
                "    <!-- This definition shows how to specify a user for a range of IP addresses. -->\n" + 
                "    <definition username=\"DomainUserA\" domain=\"MYDOMAIN\" password=\"unsecurepwd\">\n" + 
                "        <range \n" + 
                "            begin=\"192.168.1.1\" end=\"192.168.1.10\"/>\n" + 
                "    </definition>\n" + 
                "    \n" + 
                "    <!-- This definition shows how to specify a user for a specific IP address -->\n" + 
                "    <definition  username=\"BobVilla\" domain=\"MYMACHINENAME\" password=\"buildahouse\">\n" + 
                "        <specific xmlns=\"\">192.168.1.12</specific>\n" + 
                "    </definition>        \n" + 
                "\n" + 
                "</wmi-config>"
            },
            {
                new WmiConfig(),
                "<wmi-config/>"
            }
        });
    }

    private static WmiConfig getWmiConfig() {
        WmiConfig config = new WmiConfig();
        config.setRetry(2);
        config.setTimeout(1500);
        config.setUsername("monitor");
        config.setDomain("enter-domain-name");
        config.setPassword("enter-account-password");

        Definition def = new Definition();
        def.setUsername("DomainUserA");
        def.setDomain("MYDOMAIN");
        def.setPassword("unsecurepwd");
        config.addDefinition(def);

        Range range = new Range();
        range.setBegin("192.168.1.1");
        range.setEnd("192.168.1.10");
        def.addRange(range);

        def = new Definition();
        def.setUsername("BobVilla");
        def.setDomain("MYMACHINENAME");
        def.setPassword("buildahouse");
        def.addSpecific("192.168.1.12");
        config.addDefinition(def);

        return config;
    }
}
