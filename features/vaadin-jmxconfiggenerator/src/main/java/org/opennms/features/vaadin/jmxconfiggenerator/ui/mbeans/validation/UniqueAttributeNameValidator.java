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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.Field;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.NameProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Markus von Rüden
 */
public class UniqueAttributeNameValidator extends AbstractStringValidator {

	private final NameProvider provider;

	public UniqueAttributeNameValidator(NameProvider provider) {
		super("The attribute name must be unique in whole collection!");
		this.provider = provider;
	}

	@Override
	protected boolean isValidValue(String value) {
		if (value == null) return false; //validation not possible
		//count name occurrence
		Multiset<String> nameMultiSet = HashMultiset.create();
		List<String> names = provider.getNames();
		nameMultiSet.addAll(names);
		return nameMultiSet.count(value /* alias */) <= 1; //is only valid if name exists 0 or 1 times
	}
}
