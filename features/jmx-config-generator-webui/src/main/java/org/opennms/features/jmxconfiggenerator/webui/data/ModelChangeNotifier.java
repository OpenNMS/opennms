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

/**
 * One part of the Observer pattern.
 *
 * @author Markus von Rüden
 */
public interface ModelChangeNotifier {

	/**
	 * Registers a ModelChangeListener.
	 *
	 * @param clazz The type of the Model. 
	 * @param listener The listener to get invoked if the model of type <code>clazz</code> changed.
	 */
	void registerListener(Class<?> clazz, ModelChangeListener<?> listener);

	/**
	 * Notify all observers, which has been registered to type
	 * <code>clazz</codE> models.
	 *
	 * @param clazz the type of the model.
	 * @param newModel the model which has been changed.
	 */
	void notifyObservers(Class<?> clazz, Object newModel);
}
