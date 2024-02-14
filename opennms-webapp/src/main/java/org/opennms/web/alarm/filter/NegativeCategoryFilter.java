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
package org.opennms.web.alarm.filter;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.opennms.web.filter.OneArgFilter;
import org.opennms.web.filter.SQLType;

public class NegativeCategoryFilter extends OneArgFilter<String> {
    /** Constant <code>TYPE="categoryNot"</code> */
    public static final String TYPE = "categoryNot";
    /** Constant <code>NESTED_TYPE="nestedCategoryNot"</code> */
    public static final String NESTED_TYPE = "nestedCategoryNot";

    public NegativeCategoryFilter(final String category) {
        super(TYPE, SQLType.STRING, "NODEID", "nodeid", category);
    }

    public String getSQLTemplate() {
        return " " + getSQLFieldName() + " NOT IN (SELECT CN.NODEID FROM CATEGORY_NODE CN, CATEGORIES C WHERE CN.CATEGORYID = C.CATEGORYID AND C.CATEGORYNAME = %s) ";
    }

    @Override
    public Criterion getCriterion() {
        return Restrictions.sqlRestriction(" {alias}." + this.getSQLFieldName() + " NOT IN (SELECT CN.NODEID FROM CATEGORY_NODE CN, CATEGORIES C WHERE CN.CATEGORYID = C.CATEGORYID AND C.CATEGORYNAME = ?)",
                new Object[]{this.getValue()},
                new Type[]{StringType.INSTANCE});
    }

    @Override
    public String getTextDescription() {
        return ("category is not \"" + getValue() + "\"");
    }

    @Override
    public String toString() {
        return ("<AlarmFactory.NegativeCategoryFilter: " + this.getDescription() + ">");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof NegativeCategoryFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
