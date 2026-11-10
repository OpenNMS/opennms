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
package org.opennms.netmgt.config.eventd;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class EventdConfigurationTest extends XmlTestNoCastor<EventdConfiguration> {

    public EventdConfigurationTest(final EventdConfiguration sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/eventd-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        final EventdConfiguration config = new EventdConfiguration();
        config.setTCPAddress("127.0.0.1");
        config.setTCPPort(5817);
        config.setUDPAddress("127.0.0.1");
        config.setUDPPort(5817);
        config.setReceivers(5);
        config.setGetNextEventID("SELECT nextval('eventsNxtId')");
        config.setSocketSoTimeoutRequired("yes");
        config.setSocketSoTimeoutPeriod(3000);
        config.setNumThreads(0);
        config.setBatchInterval(500);
        config.setBatchSize(1000);
        config.setQueueSize(10000);
        
        return Arrays.asList(new Object[][] {
            {
                config,
                "<EventdConfiguration\n" + 
                        "        TCPAddress=\"127.0.0.1\"\n" + 
                        "        TCPPort=\"5817\"\n" + 
                        "        UDPAddress=\"127.0.0.1\"\n" + 
                        "        UDPPort=\"5817\"\n" + 
                        "        receivers=\"5\"\n" + 
                        "        getNextEventID=\"SELECT nextval('eventsNxtId')\"\n" + 
                        "        socketSoTimeoutRequired=\"yes\"\n" + 
                        "        socketSoTimeoutPeriod=\"3000\"\n"+
                        "        sink-threads=\"0\"\n"+ 
                        "        sink-queue-size=\"10000\"\n"+
                        "        sink-batch-size=\"1000\"\n"+
                        "        sink-batch-interval=\"500\"\n>"+
                        "</EventdConfiguration>"
            }
        });
    }
}
