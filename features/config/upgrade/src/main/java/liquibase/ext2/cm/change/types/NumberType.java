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
package liquibase.ext2.cm.change.types;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import liquibase.parser.core.ParsedNode;

import org.opennms.features.config.dao.api.ConfigItem.Type;

/**
 * Represents a number. There might be a need to distinguish Double and Integer.
 */
public class NumberType extends AbstractPropertyType {

    public NumberType(final List<ParsedNode> listOfAttributes) {
        super(listOfAttributes);
        this.configItem.setType(Type.NUMBER);
        Double defaultValue = defaultValueOpt
                .map(this::validateNumber)
                .map(Double::valueOf)
                .orElse(null);
        this.configItem.setDefaultValue(defaultValue);
        final Optional<String> min = getAttributeValue(Attribute.MIN)
                .map(this::validateNumber);
        final Optional<String> max = getAttributeValue(Attribute.MAX)
                .map(this::validateNumber);

        double minP = min.map(Double::parseDouble).orElse(Double.MIN_VALUE);
        double maxP = max.map(Double::parseDouble).orElse(Double.MAX_VALUE);
        if (maxP < minP) {
            throw new IllegalArgumentException(String.format("min=%s must not be bigger than max=%s", minP, maxP));
        }
        if (defaultValue != null) {
            if (min.isPresent() && defaultValue < minP) {
                throw new IllegalArgumentException(String.format("defaultValue=%s must not be smaller than min=%s", defaultValue, min.get()));
            }
            if (max.isPresent() && defaultValue > maxP) {
                throw new IllegalArgumentException(String.format("defaultValue=%s must not be bigger than max=%s", defaultValue, max.get()));
            }
        }
    }

    private String validateNumber(String number) {
        Objects.requireNonNull(number);
        try {
            Double.parseDouble(number);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Cannot parse value as 'number': %s. %s", number, e.getMessage()));
        }
        return number;
    }

}
