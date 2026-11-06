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
