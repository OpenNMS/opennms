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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.opennms.netmgt.flows.classification.csv.CsvImportResult;
import org.opennms.netmgt.flows.classification.csv.CsvService;
import org.opennms.netmgt.flows.classification.exception.CSVImportException;
import org.opennms.netmgt.flows.classification.error.Error;
import org.opennms.netmgt.flows.classification.error.Errors;
import org.opennms.netmgt.flows.classification.exception.InvalidRuleException;
import org.opennms.netmgt.flows.classification.internal.validation.RuleValidator;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

public class CsvServiceImpl implements CsvService {

    private static final int EXPECTED_COLUMNS = 4;

    private final RuleValidator ruleValidator;

    public CsvServiceImpl(RuleValidator ruleValidator) {
        this.ruleValidator = Objects.requireNonNull(ruleValidator);
    }

    @Override
    public CsvImportResult parseCSV(InputStream inputStream, boolean hasHeader) throws CSVImportException {
        Objects.requireNonNull(inputStream);
        final CsvImportResult result = new CsvImportResult();
        try {
            CSVFormat csvFormat = CSVFormat.RFC4180.withDelimiter(';');
            if (hasHeader) csvFormat = csvFormat.withHeader();
            final CSVParser parser = csvFormat.parse(new InputStreamReader(inputStream));
            for (CSVRecord record : parser.getRecords()) {
                if (record.size() < EXPECTED_COLUMNS) {
                    result.markError(record.getRecordNumber(), createError(Errors.CSV_TOO_FEW_COLUMNS, record.getRecordNumber(), record.toString(), EXPECTED_COLUMNS, record.size()));
                    continue;
                }
                // Read Values
                final String name = record.get(0);
                final String ipAddress = record.get(1);
                final String port = record.get(2);
                final String protocol = record.get(3);

                // Set values
                final Rule rule = new Rule();
                rule.setName("".equals(name) ? null : name);
                rule.setPort("".equals(port) ? null : port);
                rule.setIpAddress("".equals(ipAddress) ? null : ipAddress);
                rule.setProtocol("".equals(protocol) ? null : protocol);

                // Ensure it is a valid rule
                try {
                    ruleValidator.validate(rule);
                } catch (InvalidRuleException ex) {
                    result.markError(record.getRecordNumber(), ex.getError());
                    continue;
                }
                if (!result.hasError(record.getRecordNumber())) {
                    result.markSuccess(rule);
                }
            }
        } catch (IOException ex) {
            result.setError(createError(Errors.CSV_IMPORT_FAILED, ex.getMessage()));
        }
        return result;
    }

    @Override
    public String createCSV(List<Rule> rules) {
        StringBuffer body = new StringBuffer();
        body.append("name;ipAddress;port;protocol\n");
        body.append(rules.stream()
                        .map(rule ->
                                String.format("%s;%s;%s;%s",
                                        rule.getName() == null ? "" : rule.getName(),
                                        rule.getIpAddress() == null ? "" : rule.getIpAddress(),
                                        rule.getPort() == null ? "" : rule.getPort(),
                                        rule.getProtocol() == null ? "" : rule.getProtocol()))
                        .collect(Collectors.joining("\n")));
        return body.toString();
    }

    private static Error createError(Error error, Object... arguments) {
        error.setArguments(arguments);
        return error;
    }
}
