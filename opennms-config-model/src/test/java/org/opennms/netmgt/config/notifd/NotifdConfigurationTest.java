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
package org.opennms.netmgt.config.notifd;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class NotifdConfigurationTest extends XmlTestNoCastor<NotifdConfiguration> {

    public NotifdConfigurationTest(NotifdConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/notifd-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getNotifdConfiguration(),
                "<notifd-configuration \n" + 
                "        status=\"off\"\n" + 
                "        match-all=\"true\">\n" +
                "        <auto-acknowledge-alarm resolution-prefix=\"BLAH!\" notify=\"false\">\n" +
                "                <uei>yo</uei>\n" +
                "        </auto-acknowledge-alarm>\n" +
                "        <auto-acknowledge resolution-prefix=\"RESOLVED: \" \n" +
                "                          uei=\"uei.opennms.org/nodes/nodeRegainedService\" \n" +
                "                          acknowledge=\"uei.opennms.org/nodes/nodeLostService\">\n" +
                "                          <match>nodeid</match>\n" +
                "                          <match>interfaceid</match>\n" +
                "                          <match>serviceid</match>\n" +
                "        </auto-acknowledge>\n" +
                "        <queue>\n" + 
                "                <queue-id>default</queue-id>\n" + 
                "                <interval>20s</interval>\n" + 
                "                <handler-class>\n" + 
                "                        <name>org.opennms.netmgt.notifd.DefaultQueueHandler</name>\n" + 
                "                </handler-class>\n" + 
                "        </queue>\n" +
                "</notifd-configuration>"
            }
        });
    }

    private static NotifdConfiguration getNotifdConfiguration() {
        NotifdConfiguration config = new NotifdConfiguration();
        config.setStatus("off");
        config.setMatchAll(true);

        final AutoAcknowledgeAlarm aaa = new AutoAcknowledgeAlarm();
        aaa.setNotify(false);
        aaa.setResolutionPrefix("BLAH!");
        aaa.addUei("yo");
        config.setAutoAcknowledgeAlarm(aaa);

        AutoAcknowledge autoAck = new AutoAcknowledge();
        autoAck.setResolutionPrefix("RESOLVED: ");
        autoAck.setUei("uei.opennms.org/nodes/nodeRegainedService");
        autoAck.setAcknowledge("uei.opennms.org/nodes/nodeLostService");
        autoAck.addMatch("nodeid");
        autoAck.addMatch("interfaceid");
        autoAck.addMatch("serviceid");
        config.addAutoAcknowledge(autoAck);

        Queue queue = new Queue();
        queue.setQueueId("default");
        queue.setInterval("20s");
        HandlerClass handlerClass = new HandlerClass();
        handlerClass.setName("org.opennms.netmgt.notifd.DefaultQueueHandler");
        queue.setHandlerClass(handlerClass);
        config.addQueue(queue);

        return config;
    }
}
