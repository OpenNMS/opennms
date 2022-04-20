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

import liquibase.parser.core.ParsedNode;

import org.opennms.features.config.dao.api.ConfigItem;

public class BooleanType extends AbstractPropertyType {

    public BooleanType(final List<ParsedNode> listOfAttributes) {
        super(listOfAttributes);
        if (defaultValueOpt.isPresent() && !"true".equals(defaultValueOpt.get()) && !"false".equals(defaultValueOpt.get())) {
            throw new IllegalArgumentException(String.format("value='%s' is not true or false", defaultValueOpt.get()));
        }
        this.configItem.setType(ConfigItem.Type.BOOLEAN);
        Boolean defaultValue = defaultValueOpt
                .map(Boolean::valueOf)
                .orElse(null);
        this.configItem.setDefaultValue(defaultValue);
    }
}
