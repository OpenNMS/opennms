/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.jmxconfiggenerator.webui.ui.validators;

import java.util.Map;
import java.util.Map.Entry;

import org.opennms.features.jmxconfiggenerator.webui.ui.mbeans.NameProvider;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.ui.Field;

/**
 *
 * @author Markus von Rüden
 */
public class UniqueAttributeNameValidator extends AbstractValidator {

	private final NameProvider provider;
	private final Map<Object, Field> textFieldItemMap;

	public UniqueAttributeNameValidator(NameProvider provider, Map<Object, Field> fieldsForIsValid) {
		super("The attribute name must be unique in whole collection!");
		this.provider = provider;
		this.textFieldItemMap = fieldsForIsValid;
	}

	@Override
	public boolean isValid(Object value) {
		if (value == null || !(value instanceof String)) return false; //validation not possible
		String alias = (String) value;
		//count name occurance
		Multiset<String> nameMultiSet = HashMultiset.create();
		for (Entry<Object, String> entry : provider.getNames().entrySet()) {
			Object itemId = entry.getKey();
			String name = entry.getValue();
			//use name from textFieldItemMap if an entry for itemId exists, otherwise use name from provider
			nameMultiSet.add( textFieldItemMap.get(itemId) == null ? name : (String)textFieldItemMap.get(itemId).getValue());
		}
		return nameMultiSet.count(alias) <= 1; //is only valid if name exists 0 or 1 times 
	}
}
