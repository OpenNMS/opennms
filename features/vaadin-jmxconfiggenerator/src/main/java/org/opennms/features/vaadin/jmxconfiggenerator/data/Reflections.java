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
package org.opennms.features.vaadin.jmxconfiggenerator.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is a helper class to do some reflection stuff.
 * 
 * @author Markus von Rüden
 */
public abstract class Reflections {

	/**
	 * Gets a list of all parent interfaces and classes implemented/inherit by
	 * <code>clazz</code>.<br/>
	 * <br/>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 *   class A implements Serializable, Clonable {
	 *   ....
	 *   }
	 * 
	 *   class B extends A implements Comparable {
	 *    ....
	 *   }
	 * 
	 *   class C extends B {
	 * 
	 *    ....
	 * 
	 *   }
	 * 
	 *   buildClassHierarchy(c.class) returns [C.class, Comparable.class, B.class, Serializable.class, Cloneable.class, A.class]
	 * </pre>
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Class<?>> buildClassHierarchy(Class<?> clazz) {
		Set<Class<?>> classes = new HashSet<>();
		buildClassHierarchy(clazz, classes);
		return new ArrayList<>(classes);
	}

	/**
	 * Builds the class hierarchy as described in
	 * {@link #buildClassHierarchy(java.lang.Class) }
	 * 
	 * @param clazz
	 *            the class to build class hierarchy for
	 * @param classes
	 *            a set to store all classes and prevent doubles (is needed to
	 *            avoid double occurance of interrfaces)
	 * @see #buildClassHierarchy(java.lang.Class)
	 */
	private static void buildClassHierarchy(Class<?> clazz, Set<Class<?>> classes) {
		if (clazz == null) return;
		classes.add(clazz);
		classes.addAll(Arrays.asList(clazz.getInterfaces()));
		buildClassHierarchy(clazz.getSuperclass(), classes);
	}
}
