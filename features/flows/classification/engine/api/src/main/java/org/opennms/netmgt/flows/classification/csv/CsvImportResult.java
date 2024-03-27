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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.flows.classification.error.Error;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

public class CsvImportResult {

    final Map<Long, Error> errorMap = new HashMap<>();
    final List<Rule> rules = new ArrayList<>();
    private Error error;

    public void markError(long rowNumber, Error error) {
        errorMap.put(rowNumber, error);
    }

    public boolean hasError(long rowNumber) {
        return errorMap.containsKey(rowNumber);
    }

    public void setError(Error error) {
        this.error = error;
    }

    public void markSuccess(Rule rule) {
        rules.add(rule);
    }

    public List<Rule> getRules() {
        return rules;
    }

    public boolean isSuccess() {
        return error == null && errorMap.isEmpty();
    }

    public Error getError() {
        return error;
    }

    public Map<Long, Error> getErrorMap() {
        return errorMap;
    }
}
