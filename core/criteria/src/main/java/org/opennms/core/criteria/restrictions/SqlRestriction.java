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


public class SqlRestriction extends AttributeRestriction {

    public enum Type {
        FLOAT,
        INTEGER,
        LONG,
        STRING,
        TIMESTAMP
    }

    final Object[] parameters;
    final Type[] types;

    public SqlRestriction(final String attribute) {
        this(attribute, new Object[0], new Type[0]);
    }

    public SqlRestriction(final String attribute, Object parameter, Type type) {
        this(attribute, new Object[] { parameter }, new Type[] { type });
    }

    public SqlRestriction(final String attribute, Object[] parameters, Type[] types) {
        super(RestrictionType.SQL, attribute);
        if (parameters == null) {
            if (types == null) {
                this.parameters = new Object[0];
                this.types = new Type[0];
            } else {
                throw new IllegalArgumentException("Cannot have non-null types with null parameters");
            }
        } else {
            if (types == null) {
                throw new IllegalArgumentException("Cannot have null types with non-null parameters");
            } else {
                if (parameters.length == types.length) {
                    this.parameters = parameters;
                    this.types = types;
                } else {
                    throw new IllegalArgumentException("Parameter and type lists are different lengths");
                }
            }
        }
    }

    @Override
    public void visit(final RestrictionVisitor visitor) {
        visitor.visitSql(this);
    }

    @Override
    public String toString() {
        return "SqlRestriction [attribute=" + getAttribute() + "]";
    }

    public Object[] getParameters() {
        return parameters;
    }

    public Type[] getTypes() {
        return types;
    }
}
