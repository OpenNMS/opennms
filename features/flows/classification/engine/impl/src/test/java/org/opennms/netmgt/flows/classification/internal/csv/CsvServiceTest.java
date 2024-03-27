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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.flows.classification.csv.CsvImportResult;
import org.opennms.netmgt.flows.classification.csv.CsvService;
import org.opennms.netmgt.flows.classification.internal.validation.RuleValidator;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.GroupBuilder;
import org.opennms.netmgt.flows.classification.persistence.api.Groups;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;

import com.google.common.collect.Lists;

public class CsvServiceTest {

    private Group group;

    @Before
    public void setUp(){
        group = new GroupBuilder().withName(Groups.USER_DEFINED).build();
    }

    @Test
    public void verifyExportForEmptyRule() {
        // create a csv for a completely empty rule
        final CsvService csvService = new CsvServiceImpl(org.mockito.Mockito.mock(RuleValidator.class));
        final Rule rule = new RuleBuilder().withGroup(group).withName("dummy").build();
        final String response = csvService.createCSV(Lists.newArrayList(rule));
        final String[] rows = response.split("\n");

        // Verify output
        assertThat(rows.length, is(2));
        assertThat(rows[1], is("dummy;;;;;;;false"));
    }

    @Test
    public void verifyExportForFullyDefinedRule() {
        // create a csv for a completely empty rule
        final CsvService csvService = new CsvServiceImpl(org.mockito.Mockito.mock(RuleValidator.class));
        final Rule rule = new RuleBuilder()
                .withGroup(group)
                .withName("dummy")
                .withProtocol("tcp,udp,icmp")
                .withDstPort("80,1234").withDstAddress("8.8.8.8")
                .withSrcPort("55555").withSrcAddress("10.0.0.1")
                .withExporterFilter("categoryName = 'Databases'")
                .withOmnidirectional(true)
                .build();
        final String response = csvService.createCSV(Lists.newArrayList(rule));
        final String[] rows = response.split("\n");

        // Verify output
        assertThat(rows.length, is(2));
        assertThat(rows[1], is("dummy;tcp,udp,icmp;10.0.0.1;55555;8.8.8.8;80,1234;categoryName = 'Databases';true"));
    }

    @Test
    public void verifyParsingFullyDefined() {
        final CsvService csvService = new CsvServiceImpl(org.mockito.Mockito.mock(RuleValidator.class));
        final List<Rule> rules = csvService.parseCSV(group,
                new ByteArrayInputStream(
                        ("dummy;tcp,udp,icmp;10.0.0.1;55555;8.8.8.8;80,1234;categoryName = 'Databases';true").getBytes()),
                false
                ).getRules();
        assertThat(rules, hasSize(1));
        final Rule rule = rules.get(0);
        assertThat(rule.getId(), is(nullValue()));
        assertThat(rule.getName(), is("dummy"));
        assertThat(rule.getProtocol(), is("tcp,udp,icmp"));
        assertThat(rule.getDstPort(), is("80,1234"));
        assertThat(rule.getDstAddress(), is("8.8.8.8"));
        assertThat(rule.getSrcPort(), is("55555"));
        assertThat(rule.getSrcAddress(), is("10.0.0.1"));
        assertThat(rule.getExporterFilter(), is("categoryName = 'Databases'"));
        assertThat(rule.isOmnidirectional(), is(true));
    }

    @Test
    public void verifyParsingEmpty() {
        final CsvService csvService = new CsvServiceImpl(org.mockito.Mockito.mock(RuleValidator.class));

        final List<Rule> rules = csvService.parseCSV(group, new ByteArrayInputStream(";;;;;;;".getBytes()), false).getRules();
        assertThat(rules, hasSize(1));
        final Rule rule = rules.get(0);
        assertThat(rule.getId(), is(nullValue()));
        assertThat(rule.getDstPort(), is(nullValue()));
        assertThat(rule.getName(), is(nullValue()));
        assertThat(rule.getProtocol(), is(nullValue()));
        assertThat(rule.getDstAddress(), is(nullValue()));
        assertThat(rule.isOmnidirectional(), is(false));
    }

    @Test
    public void verifyParsingWithErrors() {
        final CsvService csvService = new CsvServiceImpl(org.mockito.Mockito.mock(RuleValidator.class));
        final CsvImportResult csvImportResult = csvService.parseCSV(group, new ByteArrayInputStream("\n\n".getBytes()), false);
        assertThat(csvImportResult.getErrorMap().size(), is(2));
    }
}

