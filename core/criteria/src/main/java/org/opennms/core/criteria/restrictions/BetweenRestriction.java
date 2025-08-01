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

import java.util.Arrays;
import java.util.List;

public class BetweenRestriction extends AttributeValueRestriction {

    public BetweenRestriction(final String attribute, final Object begin, final Object end) {
        super(RestrictionType.BETWEEN, attribute, Arrays.asList(new Object[] { begin, end }));
    }

    public Object getBegin() {
        @SuppressWarnings("unchecked")
        final List<Object> value = (List<Object>) getValue();
        return value.get(0);
    }

    public Object getEnd() {
        @SuppressWarnings("unchecked")
        final List<Object> value = (List<Object>) getValue();
        return value.get(1);
    }

    @Override
    public void visit(final RestrictionVisitor visitor) {
        visitor.visitBetween(this);
    }

    @Override
    public String toString() {
        return "BetweenRestriction [attribute=" + getAttribute() + ", begin=" + getBegin() + ", end=" + getEnd() + "]";
    }
}
