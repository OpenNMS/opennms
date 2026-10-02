/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
     * still legitimate filter targets. Kept in lower case because the allow-list check is
     * case-insensitive.
     */
    private static final Set<String> ENTITY_ONLY_COLUMNS = Set.of(
            "nodeid", "lastmodifieddate", "managedobjecttype", "managedobjectinstance");

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
        assetField = field.substring(TYPE.length());
        if (!isValidAssetColumn(assetField)) {
            throw new IllegalArgumentException("Unknown asset field: " + assetField);
        }
    }

    /**
     * The asset field is a SQL identifier spliced into the query and cannot be bound as a
     * parameter, so it must be validated against the known asset schema to prevent SQL
     * injection (NMS-20383). The schema is the {@link AssetModel} column list plus the few
     * assets-table columns that list does not expose in the UI.
     */
    static boolean isValidAssetColumn(final String column) {
        return AssetModel.isColumnValid(column)
                || ENTITY_ONLY_COLUMNS.contains(column.toLowerCase(Locale.ROOT));
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
