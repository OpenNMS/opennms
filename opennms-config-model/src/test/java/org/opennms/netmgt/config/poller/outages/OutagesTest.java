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
package org.opennms.netmgt.config.poller.outages;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class OutagesTest extends XmlTestNoCastor<Outages> {

    public OutagesTest(final Outages sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Outage outage = new Outage();
        outage.setName("junit test");
        outage.setType("weekly");
        final Interface intf = new Interface();
        intf.setAddress("match-any");
        outage.addInterface(intf);
        final Time time = new Time();
        time.setDay("monday");
        time.setBegins("13:30:00");
        time.setEnds("14:45:00");
        outage.addTime(time);
        Outages outages = new Outages();
        outages.addOutage(outage);
        return Arrays.asList(new Object[][] {
            {
                outages,
                "<outages>\n" +
                "  <outage name='junit test' type='weekly'>\n" +
                "    <time day='monday' begins='13:30:00' ends='14:45:00'/>\n" +
                "    <interface address='match-any'/>\n" +
                "  </outage>\n" +
                "</outages>\n",
                "target/classes/xsds/poll-outages.xsd"
            }
        });
    }

}
