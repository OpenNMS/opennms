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
package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.opennms.netmgt.config.collectd.jmx.Mbean;

/**
 * This class is a helper to build a tree representation or the MBean's
 * objectname for the MBeantree.
 * 
 * @author Markus von Rüden
 */
abstract class MBeansHelper {

	public static List<?> getMBeansTreeElements(Mbean input) {
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
		if (label instanceof Entry<?,?>) return ((Entry<?,?>) label).getValue().toString();
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
	private static List<String> getMBeansTreeElements(Mbean input, boolean removeLastElement) {
		List names = new ArrayList<>();
		try {
			/*
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
			final ObjectName objectname = ObjectName.getInstance(input.getObjectname());
			final Map<String, String> keyProperty = buildKeyPropertyList(objectname);
			names.add(objectname.getDomain());
			names.addAll(keyProperty.entrySet());
			if (removeLastElement) names.remove(names.size() - 1); // remove
																	// last
																	// element
																	// if needed
		} catch (MalformedObjectNameException ex) {
		}
		return names;
	}

	/**
	 * Generates the Key-Value-PropertyMap manually.
	 * This is necessary because <code>objectName.getKeyPropertyList()</code> returns a Map, wherein the order of the
	 * elements of the keySet may not be deterministic. The order is different than the order in <code>object.getKeyPropertyListString()</code>.
	 *
	 * Anyways, this method uses the <code>object.getKeyPropertyListString()</code>-method to build the KeyPropertyList-Map.
	 * @param objectName The Objectname to build the KeyPropertyList-Map from.
	 * @return The KeyPropertyList-Map.
	 */
	private static Map<String, String> buildKeyPropertyList(ObjectName objectName) {
		Map<String, String> keyValueMap = new HashMap<>();

		String keyPropertyListString = objectName.getKeyPropertyListString();
		if (keyPropertyListString != null && !keyPropertyListString.isEmpty()) {
			String[] keyValuePairs = keyPropertyListString.split(",");
			for (String eachKeyValue : keyValuePairs) {
				String key = eachKeyValue.substring(0, eachKeyValue.indexOf("="));
				String value =  eachKeyValue.length() > key.length() ? eachKeyValue.substring(key.length() + 1) : "undefined";
				if (!key.isEmpty() && !value.isEmpty()) {
					keyValueMap.put(key, value);
				}
			}
		}
		return keyValueMap;
	}
}
