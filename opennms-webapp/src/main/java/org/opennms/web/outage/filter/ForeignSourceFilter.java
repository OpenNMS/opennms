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

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.opennms.web.filter.EqualsFilter;
import org.opennms.web.filter.SQLType;

/**
 * Encapsulates all node filtering functionality.
 *
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 * @since 1.12.2
 */
public class ForeignSourceFilter extends EqualsFilter<String> {
    /** Constant <code>TYPE="foreignsource"</code> */
    public static final String TYPE = "foreignsource";

    /**
     * Constructor for ForeignSourceFilter.
     *
     * @param foreignSource a {@link java.lang.String} object.
     */
    public ForeignSourceFilter(String foreignSource) {
        super(TYPE, SQLType.STRING, "OUTAGES.IFSERVICEID", "NODE.foreignSource", foreignSource);
    }

    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        return " " + getSQLFieldName() + " IN (SELECT DISTINCT ifservices.id FROM ifservices, ipinterface, node WHERE ifservices.ipinterfaceid = ipinterface.id AND ipinterface.nodeid = node.nodeid AND node.foreignSource=%s)";
    }

    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        return Restrictions.sqlRestriction(" {alias}.ifserviceid IN (SELECT DISTINCT ifservices.id FROM ifservices, ipinterface, node WHERE ifservices.ipinterfaceid = ipinterface.id AND ipinterface.nodeid = node.nodeid AND node.foreignSource=?)", getValue(), StringType.INSTANCE);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return ("<ForeignSourceFilter: " + this.getDescription() + ">");
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof ForeignSourceFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
