/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import java.util.List;

/**
 * Interface to collect all Alias from CompMembers and Attribs
 * to identify ambiguous aliases.
 *
 * @author Markus von Rüden
 */
public interface NameProvider {

	/** List of alias/names to validate */
	List<String> getNames();

	/**
	 * Maps a Attrib/CompMember to a certain Vaadin field value. We need this, because we need the possibility to validate
	 * the uniqueness of the Atrrib's/CompMember's aliases. Wile editing the final alias has not yet been written to the underlying
	 * bean and therefore need to be considered by the name provider. This field is optional and may be empty, but not null.
	 * For example at the initial validation phase the map may be empty, but while validating the input of the alias
	 * of an Attribute in the Attributes tables the map shoult not be empty.
	 */
	interface FieldValueProvider {

		String getFieldValue(Object input);
	}
}
