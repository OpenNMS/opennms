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
import java.util.Optional;

import liquibase.parser.core.ParsedNode;

import org.opennms.features.config.dao.api.ConfigItem;

/** A data type parser to get from liquibase to ConfigItem. */
public abstract class AbstractPropertyType {

    public interface Attribute {
        String NAME = "name";
        String DEFAULT = "default";
        String PATTERN = "pattern";
        String MIN = "min";
        String MAX = "max";
        String TYPE = "type";
    }

    final protected List<ParsedNode> listOfAttributes;
    final protected ConfigItem configItem;
    final protected Optional<String> defaultValueOpt;

    protected AbstractPropertyType(final List<ParsedNode> listOfAttributes) {
        this.listOfAttributes = listOfAttributes;
        this.configItem = new ConfigItem();
        this.configItem.setName(getAttributeValueNotBlankOrThrowException(Attribute.NAME));
        this.defaultValueOpt = getAttributeValue(Attribute.DEFAULT);
    }

    public ConfigItem toItem() {
        return configItem;
    }

    public String getAttributeValueNotBlankOrThrowException(final String name) {
        return getAttributeValue(name)
                .filter(s -> !s.isBlank())
                .orElseThrow(() -> new IllegalArgumentException(String.format("Attribute %s must not be blank.", name)));
    }

    public Optional<String> getAttributeValue(final String name) {
        return listOfAttributes
                .stream()
                .filter(n -> name.equals(n.getName()))
                .findAny()
                .map(ParsedNode::getValue)
                .map(Object::toString);
    }
}
