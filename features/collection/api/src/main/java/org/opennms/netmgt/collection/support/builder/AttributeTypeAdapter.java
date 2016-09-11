/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.support.builder;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class AttributeTypeAdapter extends XmlAdapter<String,AttributeType> {

    @Override
    public AttributeType unmarshal(String typeAsString) throws Exception {
        if (typeAsString == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }
        final AttributeType type = AttributeType.parse(typeAsString);
        if (type == null) {
            throw new IllegalArgumentException("Unsupported attribute type '" + typeAsString + "'");
        }
        return type;
    }

    @Override
    public String marshal(AttributeType type) throws Exception {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }
        return type.getName();
    }

}
