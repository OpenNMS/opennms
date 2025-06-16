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
package org.opennms.netmgt.flows.classification.csv;

import java.io.InputStream;
import java.util.List;

import org.opennms.netmgt.flows.classification.exception.CSVImportException;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

/**
 * Reads and creates Classification Rules defined as CSV.
 */
public interface CsvService {
    /**
     * Parses the given CSV stream into rules.
     *
     * @param inputStream The inputstream to read the CSV from.
     * @param hasHeader If defined, the csv defined by the inputStream has an header, and this is skipped (not considered as a Rule).
     * @return The list of rules. Only returns, if ALL rules are valid, otherwise the CSVImportException is thrown.
     * @throws CSVImportException is thrown when parsing the CSV fails.
     */
    CsvImportResult parseCSV(Group group, InputStream inputStream, boolean hasHeader) throws CSVImportException;

    /**
     * Creates a CSV string from the given rules.
     * @param rules
     * @return CSV string representation of given rules.
     */
    String createCSV(List<Rule> rules);
}
