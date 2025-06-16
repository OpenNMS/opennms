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
package org.opennms.netmgt.config.tester;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.netmgt.xml.eventconf.Events;

public class ConfigTesterTest_testEventConf {

    @Test
    public void testEventConfWithValidVbNumber() throws IOException {
        assertNotNull(testEventConf("<vbnumber>1</vbnumber><vbvalue>0</vbvalue>"));
    }

    @Test(expected = MarshallingResourceFailureException.class)
    public void testEventConfWithMissingVbNumberButExistingVbValue() throws IOException {
        testEventConf("<!-- vbnumber missing --><vbvalue>0</vbvalue>");
    }

    @Test
    public void testEventConfWithMissingVbNumberAndExistingVbValue() throws IOException {
        testEventConf("<!-- vbnumber and vbvalue missing -->");
    }

    private Events testEventConf(String varbindContent) throws IOException {
        // Tests NMS-9821
        String xml = String.format("<events xmlns=\"http://xmlns.opennms.org/xsd/eventconf\">%n" +
                "   <event>%n" +
                "      <mask>%n" +
                "         <maskelement>%n" +
                "            <mename>id</mename>%n" +
                "            <mevalue>.1.3.6.1.4.1.4874.2.2.30</mevalue>%n" +
                "         </maskelement>%n" +
                "         <varbind>%n" +
                "            %s%n" +
                "         </varbind>%n" +
                "      </mask>%n" +
                "      <uei>uei.opennms.org/vendor/juniper/traps/juniCliSecurityAlertPriority0</uei>%n" +
                "      <event-label>Juniper-CLI-MIB defined trap event: juniCliSecurityAlert</event-label>%n" +
                "      <descr>blah</descr>%n" +
                "      <logmsg dest=\"logndisplay\">Juniper CLI Security Alert.</logmsg>%n" +
                "      <severity>Major</severity>%n" +
                "   </event>%n" +
                "</events>", varbindContent);
        return JaxbUtils.unmarshal(Events.class, new StringReader(xml));
    }
}
