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
package org.opennms.minion.heartbeat.common;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.utils.StringUtils;

public class MinionIdentityDTOTest extends XmlTestNoCastor<MinionIdentityDTO> {

    public MinionIdentityDTOTest(MinionIdentityDTO sampleObject, Object sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        Date timestamp = new Date(0);
        MinionIdentityDTO identity = new MinionIdentityDTO();
        identity.setId("idx");
        identity.setLocation("locationx");
        identity.setTimestamp(timestamp);
        return Arrays.asList(new Object[][] {
            {
                identity,
                "<minion>\n" +
                   "<id>idx</id>\n" +
                   "<location>locationx</location>\n" +
                   "<timestamp>" + StringUtils.iso8601LocalOffsetString(timestamp) + "</timestamp>\n" +
                "</minion>",
                null, }
        });
    }
}
