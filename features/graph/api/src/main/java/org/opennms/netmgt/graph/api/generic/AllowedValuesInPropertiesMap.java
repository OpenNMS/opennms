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

package org.opennms.netmgt.graph.api.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.graph.api.info.NodeInfo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/** 
 * This class holds all allowed types that can be used inside the properties of a GenericElement. They need to be immutable and persistable.
 * See also org.opennms.netmgt.graph.persistence.converter.ConverterService
 * TODO: Patrick: discuss with mvr if and how we want to synchronize this list with the converters in converter service. 
 */
public class AllowedValuesInPropertiesMap {

	private final static List<AllowedValue> ALLOWED_VALUES = new ArrayList<>();
	static {
		ALLOWED_VALUES.add((o)->o instanceof Boolean);
		ALLOWED_VALUES.add((o)->o instanceof Float);
		ALLOWED_VALUES.add((o)->o instanceof Integer);
		ALLOWED_VALUES.add((o)->o instanceof Long);
		ALLOWED_VALUES.add((o)->o instanceof Double);
		ALLOWED_VALUES.add((o)->o instanceof String);
		ALLOWED_VALUES.add((o)->o instanceof Short);
		ALLOWED_VALUES.add((o)->o instanceof Byte);
		ALLOWED_VALUES.add((o)->o instanceof Enum);
		ALLOWED_VALUES.add(new AllowedValueCollection());
		// TODO: Patrick make sure NodeInfo can also be persisted, discuss with mvr the strategy
	    ALLOWED_VALUES.add((o)->o instanceof NodeInfo);	
	}
	
	public static void validate(Map<String, Object> properties) {
		properties.values().forEach(item -> validate(item));	
	}
	
	public static void validate(final Object value) {
		if(!isAllowed(value)) {
			throw new IllegalArgumentException(String.format("Values of type %s are not supported. Offending value=%s", value.getClass(),value.toString()));
		}
	}
	
	public static boolean isAllowed(final Object value) {
		Objects.requireNonNull(value);
		return ALLOWED_VALUES.parallelStream().anyMatch(allowedValue -> allowedValue.isAllowed(value));	
	}

	private interface AllowedValue {
		boolean isAllowed(Object o);
	}
	
	private final static class AllowedValueCollection implements AllowedValue {
		
		private final static List<Class<?>> KNOWN_IMMUTABLE_COLLECTIONS = new ArrayList<>();
		static {
			KNOWN_IMMUTABLE_COLLECTIONS.add(ImmutableList.class);
			KNOWN_IMMUTABLE_COLLECTIONS.add(ImmutableSet.class);
			// Please note, the following Collections are not immutable and thus not allowed:
			// Collections.unmodifiableList(java.util.List<? extends T>) => the underlying List can still be modified
			// Arrays.asList() => allows changing an element set(index, element) 
		}
		
		public boolean isAllowed(Object o) {
			Objects.nonNull(o);
			
			// make sure we have a collection
			if (!(o instanceof Collection<?>)) {
				return false;
			}
			
			// make sure the collection itself is immutable
			// this is a bit tricky since there is no interface such as ImmutableCollection, thus we have to test
			// against a white list of immutable collections.
			boolean isImmutable = KNOWN_IMMUTABLE_COLLECTIONS.parallelStream().anyMatch(clazz -> clazz.isInstance(o));
			if(!isImmutable) {
				return false;
			}
			
			// make sure the collection has only allowed children 
			Collection<?> collection = (Collection<?>) o;
			return collection.stream().allMatch(element -> AllowedValuesInPropertiesMap.isAllowed(element));
		}
	}
	
}
