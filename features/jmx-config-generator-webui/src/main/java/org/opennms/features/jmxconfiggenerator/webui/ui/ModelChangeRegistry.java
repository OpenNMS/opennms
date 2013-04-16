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

package org.opennms.features.jmxconfiggenerator.webui.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeNotifier;

/**
 * Straight forward implementation of ModelChangeNotifier.
 *
 * @author Markus von Rüden
 */
public class ModelChangeRegistry implements ModelChangeNotifier {

	private Map<Class, Set<ModelChangeListener>> listeners = new HashMap<Class, Set<ModelChangeListener>>();

	/**
	 * Registers a new listener as described in {@link ModelChangeNotifier#registerListener(java.lang.Class, org.opennms.tools.gui.data.ModelChangeListener)
	 * }. A listener is only registered once!
	 *
	 * @param clazz the class of the model. Cannot be null!
	 * @param listener
	 * @see ModelChangeNotifier
	 */
	@Override
	public void registerListener(Class clazz, ModelChangeListener listener) {
		if (clazz == null) return;
		if (this.listeners.get(clazz) == null) this.listeners.put(clazz, new HashSet<ModelChangeListener>());
		this.listeners.get(clazz).add(listener);
	}

	/**
	 * @see ModelChangeNotifier#notifyObservers(java.lang.Class, java.lang.Object)
	 */
	@Override
	public void notifyObservers(Class clazz, Object model) {
		if (!listeners.containsKey(clazz)) return;
		if (listeners.get(clazz) == null) return;
		for (ModelChangeListener listener : listeners.get(clazz)) {
			listener.modelChanged(model);
		}
	}
}