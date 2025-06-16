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
package org.opennms.netmgt.flows.classification.internal.csv;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.GroupBuilder;
import org.opennms.netmgt.flows.classification.persistence.api.Groups;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;

public class CsvBuilderTest {

    @Test
    public void verifyBuilder() {
        final Group group = new GroupBuilder().withName(Groups.USER_DEFINED).build();
        final String actualCsv = new CsvBuilder()
                .withRule(new RuleBuilder().withGroup(group).withName("http2").withProtocol("TCP,UDP")
                        .withDstAddress("127.0.0.1"))
                .withRule(new RuleBuilder().withGroup(group).withName("google").withDstAddress("8.8.8.8"))
                .withRule(new RuleBuilder().withGroup(group).withName("opennms").withDstPort(8980))
                .withRule(new RuleBuilder()
                        .withGroup(group)
                        .withName("opennms-monitor")
                        .withSrcAddress("10.0.0.1").withSrcPort(10000)
                        .withDstAddress("10.0.0.2").withDstPort(8980))
                .withRule(new RuleBuilder().withGroup(group).withName("http").withProtocol("TCP")
                        .withOmnidirectional(true))
                .withRule(new RuleBuilder()
                        .withGroup(group)
                        .withName("xxx")
                        .withProtocol("tcp,udp")
                        .withSrcAddress("10.0.0.1").withSrcPort(10000)
                        .withDstAddress("10.0.0.2").withDstPort(8980)
                        .withExporterFilter("some-filter-value")
                )
                .build();

        final StringBuilder builder = new StringBuilder();
        builder.append(CsvServiceImpl.HEADERS_STRING);
        builder.append("http2;TCP,UDP;;;127.0.0.1;;;false\n");
        builder.append("google;;;;8.8.8.8;;;false\n");
        builder.append("opennms;;;;;8980;;false\n");
        builder.append("opennms-monitor;;10.0.0.1;10000;10.0.0.2;8980;;false\n");
        builder.append("http;TCP;;;;;;true\n");
        builder.append("xxx;tcp,udp;10.0.0.1;10000;10.0.0.2;8980;some-filter-value;false");

        final String expectedCsv = builder.toString();
        assertEquals(expectedCsv, actualCsv);
    }
}
