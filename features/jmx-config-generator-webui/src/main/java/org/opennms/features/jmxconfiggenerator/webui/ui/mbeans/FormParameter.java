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

/**
 * Is used to configure the {@link NameEditForm}.
 * 
 * @author Markus von Rüden
 * @see NameEditForm
 */
public interface FormParameter {
	/**
	 * Defines the caption for the {@link NameEditForm}.
	 * 
	 * @return the caption of the {@link NameEditForm}.
	 */
	String getCaption();

	/**
	 * Returns the name of the property which is editable.
	 * 
	 * @return the property name which is editable.
	 */
	String getEditablePropertyName();

	/**
	 * Returns the property name which is not editable.
	 * 
	 * @return the property name which is not editable.
	 */
	String getNonEditablePropertyName();

	/**
	 * Returns the property names of all properties, which are visible.
	 * 
	 * @return the property names of all properties, which are visible.
	 */
	Object[] getVisiblePropertieNames();

	/**
	 * Returns an optional callback which is invoked after the usual hooks from
	 * {@link EditControls} have been invoked and executed. May return null.
	 * 
	 * @return an optional callback which is invoked after the usual hooks from
	 *         {@link EditControls} have been invoked and executed. May return
	 *         null.
	 */
	@SuppressWarnings("rawtypes")
	EditControls.Callback getAdditionalCallback();

	/**
	 * Defines wether the {@link NameEditForm} has a footer or not.
	 * 
	 * @return true if there is any footer, false otherwise.
	 */
	public boolean hasFooter();
}