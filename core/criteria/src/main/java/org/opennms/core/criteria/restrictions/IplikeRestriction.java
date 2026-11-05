/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.criteria.restrictions;

import java.util.regex.Pattern;

public class IplikeRestriction extends AttributeValueRestriction {

    /**
     * The attribute of an iplike restriction is a column name that goes into the SQL text.
     * It cannot be a bind parameter, thus it must be a plain identifier.
     */
    private static final Pattern COLUMN_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    public IplikeRestriction(final String attribute, final Object value) {
        super(RestrictionType.IPLIKE, attribute, value);
    }

    /**
     * Returns true if the attribute is a plain column name that is safe to put into SQL text.
     */
    public static boolean isValidAttribute(final String attribute) {
        return attribute != null && COLUMN_NAME.matcher(attribute).matches();
    }

    @Override
    public void visit(final RestrictionVisitor visitor) {
        visitor.visitIplike(this);
    }

    @Override
    public String toString() {
        return "IplikeRestriction [attribute=" + getAttribute() + ", value=" + getValue() + "]";
    }
}
