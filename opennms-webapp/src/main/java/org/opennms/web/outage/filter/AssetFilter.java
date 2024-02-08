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
 * The Class AssetFilter.
 * <p>See NMS-8702 for more details.</p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AssetFilter extends EqualsFilter<String> {

    /** The Constant TYPE. */
    public static final String TYPE = "asset.";

    /** The asset field. */
    private String assetField;

    /**
     * Instantiates a new asset filter.
     *
     * @param field the name of the field field
     * @param value the value
     */
    public AssetFilter(String field, String value) {
        super(field, SQLType.STRING, "OUTAGES.IFSERVICEID", field, value);
        assetField = field.replaceFirst("asset.","");
    }

    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        return " " + getSQLFieldName() + " IN (SELECT DISTINCT ifservices.id FROM ifservices, ipinterface, assets WHERE ifservices.ipinterfaceid = ipinterface.id AND ipinterface.nodeid = assets.nodeid AND assets." + assetField + "=%s)";
    }

    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        return Restrictions.sqlRestriction(" {alias}.ifserviceid IN (SELECT DISTINCT ifservices.id FROM ifservices, ipinterface, assets WHERE ifservices.ipinterfaceid = ipinterface.id AND ipinterface.nodeid = assets.nodeid AND assets." + assetField + "=?)", getValue(), StringType.INSTANCE);
    }

    /* (non-Javadoc)
     * @see org.opennms.web.filter.BaseFilter#toString()
     */
    public String toString() {
        return ("<AssetFilter: " + this.getDescription() + ">");
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof AssetFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
