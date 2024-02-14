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
package org.opennms.netmgt.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.type.CharacterType;
import org.hibernate.type.EnumType;
import org.hibernate.type.StringType;
import org.opennms.netmgt.model.OnmsNode.NodeType;

public class NodeTypeUserType extends EnumType {

    private static final long serialVersionUID = -7390506282856507291L;

    private static final int[] SQL_TYPES = new int[] { java.sql.Types.CHAR };

	/**
     * A public default constructor is required by Hibernate.
     */
    public NodeTypeUserType() {}

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner) throws HibernateException, SQLException {
        Character c = CharacterType.INSTANCE.nullSafeGet(rs, names[0]);
        if (c == null) {
            return null;
        }
        for (NodeType type : NodeType.values()) {
            if (type.toString().equals(c.toString())) {
                return type;
            }
        }
        throw new HibernateException("Invalid value for NodeUserType: " + c);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index) throws HibernateException, SQLException {
        if (value == null) {
            StringType.INSTANCE.nullSafeSet(st, null, index);
        } else if (value instanceof NodeType){
            CharacterType.INSTANCE.nullSafeSet(st, ((NodeType)value).toString().charAt(0), index);
        } else if (value instanceof String){
            for (NodeType type : NodeType.values()) {
                if (type.toString().equals(value)) {
                    CharacterType.INSTANCE.nullSafeSet(st, type.toString().charAt(0), index);
                }
            }
        }
    }

    @Override
    public Class<NodeType> returnedClass() {
        return NodeType.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public void setParameterValues(Properties parameters) {
    }
}
