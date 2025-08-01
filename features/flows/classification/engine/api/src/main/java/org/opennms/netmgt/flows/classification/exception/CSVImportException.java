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
package org.opennms.netmgt.flows.classification.exception;

import java.util.Objects;

import org.opennms.netmgt.flows.classification.csv.CsvImportResult;

// Exception which is thrown when a given CSV could not be imported
public class CSVImportException extends RuntimeException {

    private final CsvImportResult result;

    public CSVImportException(CsvImportResult result) {
        this.result = Objects.requireNonNull(result);
    }

    public CsvImportResult getResult() {
        return result;
    }
}
