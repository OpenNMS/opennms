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

import java.util.Locale;
import java.util.Set;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.opennms.web.asset.AssetModel;
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
     * Columns of the assets table that are not part of the {@link AssetModel} UI column list but are
     * still legitimate filter targets. Only text columns qualify, because the filter compares the
     * value as a string. Kept in lower case because the allow-list check is
     * case-insensitive.
     */
    private static final Set<String> ENTITY_ONLY_COLUMNS = Set.of(
            "managedobjecttype", "managedobjectinstance");

    /**
     * Numeric columns in the {@link AssetModel} UI column list. They cannot be compared to the
     * string value this filter binds, so they are not filter targets.
     */
    private static final Set<String> NON_TEXT_COLUMNS = Set.of("latitude", "longitude");

    /**
     * Instantiates a new asset filter.
     *
     * @param field the name of the field field
     * @param value the value
     */
    public AssetFilter(String field, String value) {
        super(field, SQLType.STRING, "OUTAGES.IFSERVICEID", field, value);
        if (field == null || !field.startsWith(TYPE)) {
            throw new IllegalArgumentException("Asset filter field must start with '" + TYPE + "'");
        }
        final String column = field.substring(TYPE.length());
        if (!isValidAssetColumn(column)) {
            throw new IllegalArgumentException("Unknown asset field");
        }
        // The SQL text gets the allow-list form of the name, not the request text. PostgreSQL folds
        // unquoted identifiers to lower case, so the lower-case name refers to the same column.
        assetField = column.toLowerCase(Locale.ROOT);
    }

    /**
     * The asset field is a SQL identifier spliced into the query and cannot be bound as a
     * parameter, so it must be validated against the known asset schema to prevent SQL
     * injection (NMS-20383). The schema is the {@link AssetModel} column list plus the few
     * assets-table columns that list does not expose in the UI, minus its numeric columns.
     */
    static boolean isValidAssetColumn(final String column) {
        final String normalized = column.toLowerCase(Locale.ROOT);
        if (NON_TEXT_COLUMNS.contains(normalized)) {
            return false;
        }
        return AssetModel.isColumnValid(column) || ENTITY_ONLY_COLUMNS.contains(normalized);
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
