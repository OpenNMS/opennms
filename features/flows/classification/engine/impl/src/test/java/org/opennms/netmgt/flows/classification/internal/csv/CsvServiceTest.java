/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.flows.classification.internal.csv;

import static org.easymock.EasyMock.createNiceMock;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.flows.classification.csv.CsvImportResult;
import org.opennms.netmgt.flows.classification.csv.CsvService;
import org.opennms.netmgt.flows.classification.internal.validation.RuleValidator;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;

import com.google.common.collect.Lists;

public class CsvServiceTest {

    @Test
    public void verifyExportForEmptyRule() {
        // create a csv for a completely empty rule
        final CsvService csvService = new CsvServiceImpl(createNiceMock(RuleValidator.class));
        final Rule rule = new RuleBuilder().withName("dummy").build();
        final String response = csvService.createCSV(Lists.newArrayList(rule));
        final String[] rows = response.split("\n");

        // Verify output
        assertThat(rows.length, is(2));
        assertThat(rows[1], is("dummy;;;"));
    }

    @Test
    public void verifyExportForFullyDefinedRule() {
        // create a csv for a completely empty rule
        final CsvService csvService = new CsvServiceImpl(createNiceMock(RuleValidator.class));
        final Rule rule = new RuleBuilder().withName("dummy").withProtocol("tcp,udp,icmp").withPort("80,1234").withIpAddress("8.8.8.8").build();
        final String response = csvService.createCSV(Lists.newArrayList(rule));
        final String[] rows = response.split("\n");

        // Verify output
        assertThat(rows.length, is(2));
        assertThat(rows[1], is("dummy;8.8.8.8;80,1234;tcp,udp,icmp"));
    }

    @Test
    public void verifyParsingFullyDefined() {
        final CsvService csvService = new CsvServiceImpl(createNiceMock(RuleValidator.class));
        final List<Rule> rules = csvService.parseCSV(new ByteArrayInputStream("dummy;8.8.8.8;80,1234;tcp,udp,icmp".getBytes()), false).getRules();
        assertThat(rules, hasSize(1));
        final Rule rule = rules.get(0);
        assertThat(rule.getId(), is(nullValue()));
        assertThat(rule.getPort(), is("80,1234"));
        assertThat(rule.getName(), is("dummy"));
        assertThat(rule.getProtocol(), is("tcp,udp,icmp"));
        assertThat(rule.getIpAddress(), is("8.8.8.8"));
    }

    @Test
    public void verifyParsingEmpty() {
        final CsvService csvService = new CsvServiceImpl(createNiceMock(RuleValidator.class));

        final List<Rule> rules = csvService.parseCSV(new ByteArrayInputStream(";;;".getBytes()), false).getRules();
        assertThat(rules, hasSize(1));
        final Rule rule = rules.get(0);
        assertThat(rule.getId(), is(nullValue()));
        assertThat(rule.getPort(), is(nullValue()));
        assertThat(rule.getName(), is(nullValue()));
        assertThat(rule.getProtocol(), is(nullValue()));
        assertThat(rule.getIpAddress(), is(nullValue()));
    }

    @Test
    public void verifyParsingWithErrors() {
        final CsvService csvService = new CsvServiceImpl(createNiceMock(RuleValidator.class));
        final CsvImportResult csvImportResult = csvService.parseCSV(new ByteArrayInputStream("\n\n".getBytes()), false);
        assertThat(csvImportResult.getErrorMap().size(), is(2));
    }
}

