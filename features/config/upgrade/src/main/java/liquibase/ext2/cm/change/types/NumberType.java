/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
        if (min.isPresent() && defaultValue < minP) {
            throw new IllegalArgumentException(String.format("defaultValue=%s must not be smaller than min=%s", defaultValue, min.get()));
        }
        if (max.isPresent() && defaultValue > maxP) {
            throw new IllegalArgumentException(String.format("defaultValue=%s must not be bigger than max=%s", defaultValue, max.get()));
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
