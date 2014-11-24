/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.gwt.web.ui.asset.client.tools.fieldsets;

import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusHandler;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a> A
 *         FieldSet is a pair of a describing label/name and a value/input. It's
 *         intended to make data-input and data-maintenance pages easier. It's
 *         string based.
 */
public interface FieldSet extends FocusHandler, ChangeHandler {

	/**
	 * Clears the status changed from a {@link FieldSet}.
	 */
	public abstract void clearChanged();

	/**
	 * clears all error strings from a {@link FieldSet}.
	 */
	public abstract void clearErrors();

	/**
	 * clears all warning strings from a {@link FieldSet}.
	 */
	public abstract void clearWarnings();

	/**
	 * @return boolean enabled if writing/changes are allowed/active.
	 */
	public abstract Boolean getEnabled();

	/**
	 * Get the complete error string for the {@link FieldSet}.
	 * 
	 * @return String error
	 */
	public abstract String getError();

	/**
	 * Get the description/label text of the {@link FieldSet}.
	 * 
	 * @return String label
	 */
	public abstract String getLabel();

	/**
	 * @return actual value of {@link FieldSet}.
	 */
	public abstract String getValue();

	/**
	 * Get the complete warning string for the {@link FieldSet}.
	 * 
	 * @return String warning
	 */
	public abstract String getWarning();

	/**
	 * Set the {@link FieldSet} into write/write-protected mode.
	 * 
	 * @param enabled
	 *            to get write-mode disable to get write-protected mode
	 */
	public abstract void setEnabled(Boolean enabled);

	/**
	 * Set a error string to the {@link FieldSet}.
	 * 
	 * @param error
	 */
	public abstract void setError(String error);

	/**
	 * Sets a text into the description/label of the {@link FieldSet}.
	 * 
	 * @param label
	 */
	public abstract void setLabel(String label);

	/**
	 * Sets a value into the value/input of the {@link FieldSet}.
	 * 
	 * @param value
	 */
	public abstract void setValue(String value);

	/**
	 * Set a warning string to the {@link FieldSet}.
	 * 
	 * @param warning
	 */
	public abstract void setWarning(String warning);
}
