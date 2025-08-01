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
