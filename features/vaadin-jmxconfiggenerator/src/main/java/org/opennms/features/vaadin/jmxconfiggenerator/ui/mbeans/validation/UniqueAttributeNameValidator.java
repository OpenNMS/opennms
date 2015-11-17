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

package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation;

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.Field;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.NameProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ensures that a <code>value</code> only occurs once in a List of names.
 * @author Markus von Rüden
 */
public class UniqueAttributeNameValidator extends AbstractStringValidator {

	/**
	 * Maps a Attrib/CompMember to a certain Vaadin field value. We need this, because we need the possibility to validate
	 * the uniqueness of the Atrrib's/CompMember's aliases. While editing the final alias has not yet been written to the underlying
	 * bean and therefore need to be considered by the name provider. This field is optional and may be empty, but not null.
	 * For example at the initial validation phase the map may be empty, but while validating the input of the alias
	 * of an Attribute in the Attributes tables the map should not be empty.
	 */
	public interface FieldProvider {

		Map<Object, Field<String>> getObjectFieldMap();
	}

	private final NameProvider provider;

	private final FieldProvider fieldProvider;

	public UniqueAttributeNameValidator(NameProvider nameProvider, FieldProvider fieldProvider) {
		super("The attribute name must be unique in whole collection!");
		this.provider = nameProvider;
		this.fieldProvider = fieldProvider;
	}

	public UniqueAttributeNameValidator(NameProvider provider) {
		this(provider, null);
	}

	protected List<String> getNames() {
		Map<Object, String> objectToNameMap = provider.getNamesMap();
		if (fieldProvider == null) {
			return new ArrayList<>(objectToNameMap.values());
		}
		final Map<Object, String> clonedMap = new HashMap<>(objectToNameMap);
		final Map<Object, Field<String>> fieldValuesToMerge = fieldProvider.getObjectFieldMap();
		for (Map.Entry<Object, Field<String>> eachEntry : fieldValuesToMerge.entrySet()) {
			clonedMap.put(eachEntry.getKey(), eachEntry.getValue().getValue());
		}
		return new ArrayList<>(clonedMap.values());
	}

	@Override
	protected boolean isValidValue(String value) {
		//is only valid if name exists 0 or 1 times
		if (value != null) {
			List<String> names = getNames();
			if (!names.contains(value)) { // value exists 0 times => OK
				return true;
			}
			names.remove(value);

			// if value exists => more than 1 times in general,
			// if not => only 1 times
			return !names.contains(value);
		}
		return false; //validation not, value is null
	}
}
