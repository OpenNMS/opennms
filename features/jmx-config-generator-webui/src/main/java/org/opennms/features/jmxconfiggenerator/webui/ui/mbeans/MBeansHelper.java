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

package org.opennms.features.jmxconfiggenerator.webui.ui.mbeans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.opennms.features.jmxconfiggenerator.webui.data.Reflections;
import org.opennms.features.jmxconfiggenerator.webui.data.SimpleEntry;
import org.opennms.xmlns.xsd.config.jmx_datacollection.Mbean;

/**
 * This class is a helper to build a tree representation or the MBean's
 * objectname for the MBeantree.
 * 
 * @author Markus von Rüden
 */
public class MBeansHelper {

	public static List getMBeansTreeElements(Mbean input) {
		return getMBeansTreeElements(input, true);
	}

	/**
	 * @param input
	 * @return the string to display in the MBeanTree for the Mbean leaf.
	 */
	public static String getLeafLabel(Mbean input) {
		List<String> labels = getMBeansTreeElements(input, false);
		if (labels.isEmpty()) return input.getName();
		Object label = labels.get(labels.size() - 1);
		if (label instanceof Entry) return ((Entry) label).getValue().toString();
		return label.toString();
	}

	/**
	 * 
	 * @param input
	 *            any Mbean
	 * @param removeLastElement
	 *            should last element be removed?
	 * @return a List of Elements to build the MBeanTree.
	 */
	private static List getMBeansTreeElements(Mbean input, boolean removeLastElement) {
		List names = new ArrayList();
		try {
			/**
			 * By default, the MBeans are displayed in the tree based on their
			 * object names. The order of key properties specified when the
			 * object names are created is preserved by the MBeans tab when it
			 * adds MBeans to the MBean tree. The exact key property list that
			 * the MBeans tab will use to build the MBean tree will be the one
			 * returned by the method ObjectName.getKeyPropertyListString(),
			 * with type as the first key, and j2eeType, if present, as the
			 * second key.(http://visualvm.java.net/mbeans_tab.html).
			 * 
			 * Below is the implementation of the above definition.
			 */
			ObjectName obj = ObjectName.getInstance(input.getObjectname());
			names.add(obj.getDomain());
			Map<String, String> keyProperty = obj.getKeyPropertyList();
			addIfNotNull(names, keyProperty, "type");
			addIfNotNull(names, keyProperty, "j2eeType");
			names.addAll(keyProperty.entrySet());
			if (removeLastElement) names.remove(names.size() - 1); // remove
																	// last
																	// element
																	// if needed
		} catch (MalformedObjectNameException ex) {
		}
		return names;
	}

	private static void addIfNotNull(List<Map.Entry<String, String>> names, Map<String, String> keyProperty, String key) {
		if (keyProperty.get(key) == null) return;
		names.add(new SimpleEntry(key, keyProperty.get(key)));
		keyProperty.remove(key);
	}

	/**
	 * Builds the class hierarchy of the given <code>clazz</code> and returns
	 * the value of the given map if any class in the hierarchy of
	 * <code>clazz</code> is registered as a key to the map.
	 * 
	 * @param <T>
	 *            type of the value in the map
	 * @param map
	 *            a map to lookup for any class in <code>clazz</code> hierarchy.
	 * @param clazz
	 *            the class to look up any value in <code>map</code>
	 * @return T if a key is found in <code>map</code>, otherwise null.
	 */
	public static <T> T getValueForClass(Map<Class<?>, T> map, Class<?> clazz) {
		List<Class> classes = Reflections.buildClassHierarchy(clazz);
		for (int i = classes.size() - 1; i >= 0; i--) {
			if (map.get(classes.get(i)) != null) return map.get(classes.get(i));
		}
		return null;
	}
}
