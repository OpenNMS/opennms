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
package org.opennms.web.outage.filter;

import java.util.Objects;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.web.filter.NotEqualsFilterNullAware;
import org.opennms.web.filter.SQLType;
import org.opennms.web.outage.OutageUtil;

public class NegativePerspectiveLocationFilter extends NotEqualsFilterNullAware {
    public static final String TYPE = "notperspective";

    public NegativePerspectiveLocationFilter(String perspective) {
        super(TYPE, SQLType.STRING, "PERSPECTIVE", "perspective.locationName", perspective);
    }

    @Override
    public String getTextDescription() {
        if (getValue() != null) {
            return String.format("polling perspective is not %s", getValue());
        } else {
            return ("from non-perspective polling");
        }
    }

    @Override
    public String getTextDescriptionAsSanitizedHtml() {
        if (getValue() != null) {
            return String.format("polling perspective is not %s", WebSecurityUtils.sanitizeString(getValue()));
        } else {
            return ("from perspective polling");
        }
    }

    @Override
    public String toString() {
        return ("<NegativePerspectiveLocationFilter: " + this.getDescription() + ">");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NegativePerspectiveLocationFilter that = (NegativePerspectiveLocationFilter) o;
        return Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
