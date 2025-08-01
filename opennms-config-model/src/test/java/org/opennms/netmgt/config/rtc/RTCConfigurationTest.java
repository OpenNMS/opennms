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
package org.opennms.netmgt.config.rtc;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class RTCConfigurationTest extends XmlTestNoCastor<RTCConfiguration> {

    public RTCConfigurationTest(RTCConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/rtc-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        RTCConfiguration config = new RTCConfiguration();
        config.setUpdaters(10);
        config.setSenders(5);
        config.setRollingWindow("24h");
        config.setMaxEventsBeforeResend(100);
        config.setLowThresholdInterval("20s");
        config.setHighThresholdInterval("45s");
        config.setUserRefreshInterval("2m");
        config.setErrorsBeforeUrlUnsubscribe(5);

        return Arrays.asList(new Object[][] {
            {
                config,
                "<RTCConfiguration\n" + 
                "        updaters=\"10\"\n" + 
                "        senders=\"5\"\n" + 
                "        rollingWindow=\"24h\"\n" + 
                "        maxEventsBeforeResend=\"100\"\n" + 
                "        lowThresholdInterval=\"20s\"\n" + 
                "        highThresholdInterval=\"45s\"\n" + 
                "        userRefreshInterval=\"2m\"\n" + 
                "        errorsBeforeUrlUnsubscribe=\"5\"/>"
            }
        });
    }
}
