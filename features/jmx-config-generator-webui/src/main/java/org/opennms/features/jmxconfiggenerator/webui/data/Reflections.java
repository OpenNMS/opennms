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

package org.opennms.features.jmxconfiggenerator.webui.data;

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
public class Reflections {

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
	public static List<Class> buildClassHierarchy(Class clazz) {
		Set<Class> classes = new HashSet<Class>();
		buildClassHierarchy(clazz, classes);
		return new ArrayList<Class>(classes);
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
	private static void buildClassHierarchy(Class clazz, Set<Class> classes) {
		if (clazz == null) return;
		classes.add(clazz);
		classes.addAll(Arrays.asList(clazz.getInterfaces()));
		buildClassHierarchy(clazz.getSuperclass(), classes);
	}
}
