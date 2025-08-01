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
package org.opennms.netmgt.flows.classification.internal.value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StringValue {
    private final String input;

    public StringValue(String input) {
        this.input = input;
    }

    public boolean hasWildcard() {
        return input != null && input.contains("*");
    }

    public boolean isWildcard() {
        return "*".equals(input);
    }

    public boolean isNull() {
        return input == null;
    }

    public boolean isEmpty() {
        return input != null && input.isEmpty();
    }

    public boolean isNullOrEmpty() {
        return isNull() || isEmpty();
    }

    public boolean isRanged() {
        if (!isNullOrEmpty()) {
            return input.contains("-");
        }
        return false;
    }

    public String getValue() {
        return input;
    }

    public List<StringValue> splitBy(String separator) {
        return isNullOrEmpty() ? Collections.emptyList() : Arrays.stream(input.split(separator))
                .map(String::trim)
                .filter(segment -> segment.length() > 0)
                .map(StringValue::new)
                .collect(Collectors.toList());
    }

    public boolean contains(final CharSequence charSequence) {
        Objects.requireNonNull(charSequence);
        if (input == null) {
            return false;
        }
        return input.contains(charSequence);
    }
}
