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
package org.opennms.netmgt.config.tl1d;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class Tl1dConfigurationTest extends XmlTestNoCastor<Tl1dConfiguration> {

    public Tl1dConfigurationTest(Tl1dConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/tl1d-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<tl1d-configuration>\n" +
                "  <tl1-element host=\"127.0.0.1\" \n" +
                "               port=\"15001\" \n" +
                "               password=\"opennms\" \n" +
                "               reconnect-delay=\"30000\" \n" +
                "               tl1-client-api=\"org.opennms.netmgt.tl1d.Tl1ClientImpl\"\n" +
                "               tl1-message-parser=\"org.opennms.netmgt.tl1d.Tl1AutonomousMessageProcessor\" \n" +
                "               userid=\"opennms\"/>\n" +
                "</tl1d-configuration>"
            },
            {
                new Tl1dConfiguration(),
                "<tl1d-configuration/>"
            }
        });
    }

    private static Tl1dConfiguration getConfig() {
        Tl1dConfiguration config = new Tl1dConfiguration();
        
        Tl1Element el = new Tl1Element();
        el.setHost("127.0.0.1");
        el.setPort(15001);
        el.setPassword("opennms");
        el.setReconnectDelay(30000L);
        el.setTl1ClientApi("org.opennms.netmgt.tl1d.Tl1ClientImpl");
        el.setTl1MessageParser("org.opennms.netmgt.tl1d.Tl1AutonomousMessageProcessor");
        el.setUserid("opennms");
        config.addTl1Element(el);

        return config;
    }
}

