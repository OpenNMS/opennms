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

/**
 * Is used by the {@link org.opennms.web.tags.SelectTag} to determine how to render its options elements.
 * @param <T> The type of the elements which will be rendered by the SelectTag (e.g. String)
 */
public interface SelectTagHandler<T> {
    /**
     * Returns the String which should be put in the value attribute of the option.
     *
     * @param input The object to get the value from.
     * @return the value for the value attribute of the option-tag.
     */
    String getValue(T input);

    /**
     * Returns the String which should be put in the element section of the option tag.
     * @param input
     * @return
     */
    String getDescription(T input);

    /**
     * Determines if the currentElement is the selectedElement
     * @return if the currentElement is the selectedElement.
     */
    boolean isSelected(T currentElement, T selectedElement);

}
