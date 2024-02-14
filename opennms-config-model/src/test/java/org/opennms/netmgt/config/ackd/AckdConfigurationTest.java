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
package org.opennms.netmgt.config.ackd;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class AckdConfigurationTest extends XmlTestNoCastor<AckdConfiguration> {

    public AckdConfigurationTest(final AckdConfiguration sampleObject,
            final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {

        ReaderSchedule schedule = new ReaderSchedule(60L, "s");
        List<Parameter> parameters = new ArrayList<Parameter>(1);
        parameters.add(new Parameter("readmail-config", "localhost"));
        Reader reader = new Reader("JavaMailReader", false, schedule, parameters);
        final List<Reader> readers = new ArrayList<>();
        readers.add(reader);
        AckdConfiguration ackdConfig = new AckdConfiguration(
                                                             true,
                                                             "~^ack$",
                                                             "~^unack$",
                                                             "~^(resolve|clear)$",
                                                             "~^esc$",
                                                             "~.*Re:.*Notice #([0-9]+).*",
                                                             "~.*alarmid:([0-9]+).*",
                                                             readers);
        return Arrays.asList(new Object[][] {
                {
                        ackdConfig,
                        "<ackd-configuration alarm-sync=\"true\" ack-expression=\"~^ack$\""
                                + " unack-expression=\"~^unack$\" escalate-expression=\"~^esc$\""
                                + " clear-expression=\"~^(resolve|clear)$\""
                                + " notifyid-match-expression=\"~.*Re:.*Notice #([0-9]+).*\""
                                + " alarmid-match-expression=\"~.*alarmid:([0-9]+).*\">"
                                + "<readers>"
                                + "<reader enabled=\"false\" reader-name=\"JavaMailReader\">"
                                + "<reader-schedule interval=\"60\" unit=\"s\"/>"
                                + "<parameter key=\"readmail-config\" value=\"localhost\" />"
                                + "</reader>" + "</readers>"
                                + "</ackd-configuration>",
                        "target/classes/xsds/ackd-configuration.xsd", },
                { new AckdConfiguration(), "<ackd-configuration/>",
                        "target/classes/xsds/ackd-configuration.xsd" } });
    }
}
