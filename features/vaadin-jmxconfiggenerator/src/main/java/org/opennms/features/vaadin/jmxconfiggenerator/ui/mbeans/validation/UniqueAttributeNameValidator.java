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
package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans.NameProvider;

import com.vaadin.v7.data.validator.AbstractStringValidator;
import com.vaadin.v7.ui.Field;

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
