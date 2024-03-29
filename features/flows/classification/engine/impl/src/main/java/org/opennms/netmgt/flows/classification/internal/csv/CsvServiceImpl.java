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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.BooleanUtils;
import org.opennms.netmgt.flows.classification.csv.CsvImportResult;
import org.opennms.netmgt.flows.classification.csv.CsvService;
import org.opennms.netmgt.flows.classification.error.Error;
import org.opennms.netmgt.flows.classification.error.ErrorContext;
import org.opennms.netmgt.flows.classification.error.Errors;
import org.opennms.netmgt.flows.classification.exception.CSVImportException;
import org.opennms.netmgt.flows.classification.exception.InvalidRuleException;
import org.opennms.netmgt.flows.classification.internal.validation.RuleValidator;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

public class CsvServiceImpl implements CsvService {

    public static final String[] HEADERS = {"name","protocol","srcAddress","srcPort", "dstAddress", "dstPort", "exporterFilter", "omnidirectional"};

    public static final String HEADERS_STRING = String.join(";", HEADERS) + "\n";

    private static final int EXPECTED_COLUMNS = HEADERS.length;

    private final RuleValidator ruleValidator;

    public CsvServiceImpl(RuleValidator ruleValidator) {
        this.ruleValidator = Objects.requireNonNull(ruleValidator);
    }

    @Override
    public CsvImportResult parseCSV(Group group, InputStream inputStream, boolean hasHeader) throws CSVImportException {
        Objects.requireNonNull(inputStream);
        final CsvImportResult result = new CsvImportResult();
        try {
            CSVFormat csvFormat = CSVFormat.RFC4180.withDelimiter(';');
            if (hasHeader) csvFormat = csvFormat.withHeader();
            final CSVParser parser = csvFormat.parse(new InputStreamReader(inputStream));
            for (CSVRecord record : parser.getRecords()) {
                if (record.size() < EXPECTED_COLUMNS) {
                    result.markError(record.getRecordNumber(), new Error(ErrorContext.Entity, Errors.CSV_TOO_FEW_COLUMNS, record.getRecordNumber(), record.toString(), EXPECTED_COLUMNS, record.size()));
                    continue;
                }
                // Read Values
                final String name = record.get(0);
                final String protocol = record.get(1);
                final String srcAddress = record.get(2);
                final String srcPort = record.get(3);
                final String dstAddress = record.get(4);
                final String dstPort = record.get(5);
                final String exportFilter = record.get(6);
                final String omnidirectional = record.get(7);

                // Set values
                final Rule rule = new Rule();
                rule.setGroup(group);
                rule.setName("".equals(name) ? null : name);
                rule.setDstPort("".equals(dstPort) ? null : dstPort);
                rule.setDstAddress("".equals(dstAddress) ? null : dstAddress);
                rule.setSrcPort("".equals(srcPort) ? null : srcPort);
                rule.setSrcAddress("".equals(srcAddress) ? null : srcAddress);
                rule.setProtocol("".equals(protocol) ? null : protocol);
                rule.setExporterFilter("".equals(exportFilter) ? null : exportFilter);
                rule.setOmnidirectional(BooleanUtils.toBoolean(omnidirectional));

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
            result.setError(new Error(ErrorContext.Entity, Errors.CSV_IMPORT_FAILED, ex.getMessage()));
        }
        return result;
    }

    @Override
    public String createCSV(List<Rule> rules) {
        final String csv = new CsvBuilder().withHeader(true).withRules(rules).build();
        return csv;
    }

}
