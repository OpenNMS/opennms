/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.tags.select;

import org.opennms.netmgt.model.OnmsFilterFavorite;

public class FilterFavoriteSelectTagHandler implements SelectTagHandler<OnmsFilterFavorite> {
    private String m_nullDescription = "";

    public FilterFavoriteSelectTagHandler(String nullDescription){
        m_nullDescription = nullDescription;
    }

    @Override
    public String getValue(OnmsFilterFavorite input) {
        if (input == null) return "";
        return input.getId() + ";" + input.getFilter();
    }

    @Override
    public String getDescription(OnmsFilterFavorite input) {
        if (input == null) return m_nullDescription;
        return input.getName();
    }

    @Override
    public boolean isSelected(OnmsFilterFavorite currentElement, OnmsFilterFavorite selectedElement) {
        if (currentElement == selectedElement) return true;
        if (currentElement != null) return currentElement.equals(selectedElement);
        return false;
    }
}
