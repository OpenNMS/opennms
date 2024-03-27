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
package org.opennms.netmgt.config.ami;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class AmiConfigTest extends XmlTestNoCastor<AmiConfig> {

    public AmiConfigTest(final AmiConfig sampleObject,
            final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {

        Range range = new Range("10.0.0.0", "10.255.255.255");
        List<Range> ranges = new ArrayList<>();
        ranges.add(range);

        List<String> specifics = new ArrayList<>();
        specifics.add("192.168.1.1");

        List<String> ipMatches = new ArrayList<>();
        ipMatches.add("172.23.*.*");

        Definition definition = new Definition(9999, true, 1000, 3, "user",
                                               "pass", ranges, specifics,
                                               ipMatches);
        List<Definition> definitions = new ArrayList<>();
        definitions.add(definition);

        AmiConfig amiConfig = new AmiConfig(9998, false, 3000, 1, "admin",
                                            "admin", definitions);

        return Arrays.asList(new Object[][] { {
                amiConfig,
                "<ami-config port=\"9998\" use-ssl=\"false\" timeout=\"3000\" "
                        + "retry=\"1\" username=\"admin\" password=\"admin\">"
                        + "<definition port=\"9999\" use-ssl=\"true\" timeout=\"1000\" "
                        + "retry=\"3\" username=\"user\" password=\"pass\">"
                        + "<range begin=\"10.0.0.0\" end=\"10.255.255.255\"/>"
                        + "<specific>192.168.1.1</specific>"
                        + "<ip-match>172.23.*.*</ip-match>"
                        + "</definition>"
                        + "</ami-config>",
                "target/classes/xsds/ami-config.xsd", }, });
    }
}
