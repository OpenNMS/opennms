/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.csv;

import java.io.InputStream;
import java.util.List;

import org.opennms.netmgt.flows.classification.exception.CSVImportException;
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
    CsvImportResult parseCSV(InputStream inputStream, boolean hasHeader) throws CSVImportException;

    /**
     * Creates a CSV string from the given rules.
     * @param rules
     * @return CSV string representation of given rules.
     */
    String createCSV(List<Rule> rules);
}
