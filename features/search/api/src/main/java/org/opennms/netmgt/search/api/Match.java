/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.search.api;

import java.util.Objects;

/**
 * Represents a search match, allowing some additional feedback to the user.
 *
 * A node for example can be found either by label, category, ip address or asset field.
 * The {@link SearchResultItem} however may represent the same node. For the user however it would be useful which
 * criteria of the node actually matched the search input query. A {@link Match} allows to do that.
 *
 * @author mvrueden
 */
public class Match {
    /** Unique ID */
    private String id;

    /** User friendly label */
    private String label;

    /** The value which actually matched. */
    private String value;

    public Match() {

    }

    public Match(String id, String label, String value) {
        this.id = Objects.requireNonNull(id);
        this.label = Objects.requireNonNull(label);
        this.value = Objects.requireNonNull(value);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("label", label)
                .add("value", value)
                .toString();
    }
}
