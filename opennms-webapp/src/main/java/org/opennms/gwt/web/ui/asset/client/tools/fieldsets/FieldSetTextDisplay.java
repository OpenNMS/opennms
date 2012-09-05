/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.gwt.web.ui.asset.client.tools.fieldsets;

import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.Label;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 *         </br> {@link FieldSet} just for displaying text. No input possible.
 */
public class FieldSetTextDisplay extends AbstractFieldSet implements FieldSet {

	protected Label textLabel = new Label();

	@UiConstructor
	public FieldSetTextDisplay(String name, String value, String helpText) {
		super(name, helpText);
		init(value);
	}

	@Override
	public String getValue() {
		return textLabel.getText();
	}

	private void init(String value) {
		textLabel.setText(value);
		textLabel.setStyleName("textLabel");
		panel.add(textLabel);
	}

	@Override
	public void setEnabled(Boolean enabled) {
	}

	@Override
	public void setValue(String value) {
		textLabel.setText(value);
	}
}
