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

package liquibase.ext2.cm.change;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import liquibase.ext2.cm.change.types.AbstractPropertyType.Attribute;
import liquibase.ext2.cm.change.types.BooleanType;
import liquibase.ext2.cm.change.types.NumberType;
import liquibase.ext2.cm.change.types.PropertyType;
import liquibase.ext2.cm.change.types.StringType;
import liquibase.parser.core.ParsedNode;

import org.opennms.features.config.dao.api.ConfigItem;

public class Liqui2ConfigItemUtil {

    public static ConfigItem createConfigItemForProperty(final List<ParsedNode> listOfAttributes) {

        final String type = getAttributeValueOrThrowException(listOfAttributes, Attribute.TYPE);
        if (PropertyType.BOOLEAN.equals(type)) {
            return new BooleanType(listOfAttributes).toItem();
        } else if (PropertyType.STRING.equals(type)) {
            return new StringType(listOfAttributes).toItem();
        } else if (PropertyType.NUMBER.equals(type)) {
            return new NumberType(listOfAttributes).toItem();
        } else {
            throw new IllegalArgumentException(String.format("Unknown type='%s'", type));
        }
    }

    public static Optional<ConfigItem> findPropertyDefinition(ConfigItem schema, String propertyName) {
        Objects.requireNonNull(propertyName);
        return schema
                .getChildren()
                .stream().filter(i -> propertyName.equals(i.getName()))
                .findAny();
    }

    public static String getAttributeValueOrThrowException(final List<ParsedNode> listOfAttributes, final String name) {
        return getAttributeValue(listOfAttributes, name)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Attribute %s must not be null.", name)));
    }

    public static Optional<String> getAttributeValue(final List<ParsedNode> listOfAttributes, final String name) {
        return listOfAttributes
                .stream()
                .filter(n -> name.equals(n.getName()))
                .findAny()
                .map(ParsedNode::getValue)
                .map(Object::toString);
    }
}
