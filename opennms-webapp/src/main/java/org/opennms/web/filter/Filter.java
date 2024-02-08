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
package org.opennms.web.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.criterion.Criterion;
import org.opennms.core.utils.WebSecurityUtils;

/**
 * <p>Filter interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface Filter {
    
    /**
     * Returns an expression for a SQL where clause. Remember to include a
     * trailing space, but no leading AND or OR.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSql();
    
    /**
     * Returns a parameterized SQL where clause.  Remember to include a
     * trailing space, but no leading AND or OR.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParamSql();
    
    /**
     * Binds the parameter values corresponding to the ? tokens in the string
     * returned from getParamSql() to a prepared statement.  Returns the number
     * of parameters that were bound.
     *
     * @param ps a {@link java.sql.PreparedStatement} object.
     * @param parameterIndex a int.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException;

    /**
     * Returns a terse string (including a "=") that describes this filter in
     * such a way to easily be included in an HTTP GET parameter.
     *
     * <p>
     * Some examples:
     * <ul>
     * <li>"node=1"</li>
     * <li>"interface=192.168.0.1"</li>
     * <li>"severity=3"</li>
     * <li>"nodenamelike=opennms"</li>
     * </ul>
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription();

    /**
     * Returns a terse but human-readable string describing this filter in such
     * a way to easily be included in a search results list.
     *
     * <p>
     * Some examples (corresponding to the examples in
     * {@link #getDescription getDescription}):
     * <ul>
     * <li>"node=nodelabel_of_node_1"</li>
     * <li>"interface=192.168.0.1"</li>
     * <li>"severity=Normal"</li>
     * <li>"node name containing \"opennms\""</li>
     * </ul>
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTextDescription();

    /** Returns a similar text as getTextDescription() but can contain html elements.
     *  The implementor is responsible to make sure no cross side scripting can occur. */
    default String getTextDescriptionAsSanitizedHtml() {
        return WebSecurityUtils.sanitizeString(getTextDescription());
    };

    /**
     * Criterion used to construction an OnmsCritieria
     *
     * @return A Criterion that represents a criteria restriction
     * imposed by this filter
     */
    public Criterion getCriterion();
        
}
