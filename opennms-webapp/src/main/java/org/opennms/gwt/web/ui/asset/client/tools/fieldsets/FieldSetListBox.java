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

import java.util.List;

import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus
 *         Neumann</a></br> {@link FieldSet} for displaying and selection values
 *         from a list.
 */
public class FieldSetListBox extends AbstractFieldSet implements FieldSet {

	private ListBox listBox = new ListBox(false);
	private List<String> options;

	@UiConstructor
	public FieldSetListBox(String name, String value, String helpText) {
		super(name, helpText);
		init(value, null);
	}

	public FieldSetListBox(String name, String value, String helpText, List<String> options) {
		super(name, helpText);
		init(value, options);
	}

	@Override
	public String getValue() {
		return listBox.getItemText(listBox.getSelectedIndex());
	}

	private void init(String value, List<String> options) {
		inititalValue = value;

		this.options = options;

		if (options != null) {
			for (String string : options) {
				listBox.addItem(string);
			}
			if (options.contains(value)) {
				listBox.setSelectedIndex(options.indexOf(value));
			} else {
				listBox.addItem(value);
				listBox.setSelectedIndex(options.size());
			}
		}

		if (options == null) {
			listBox.addItem(value);
			listBox.setSelectedIndex(0);
		}

		listBox.setVisibleItemCount(1);
		listBox.addChangeHandler(this);
		listBox.addStyleName("form-control");

		panel.add(listBox);
	}

	@Override
	public void setEnabled(Boolean enabled) {
		listBox.setEnabled(enabled);
	}

	/**
	 * Takes a ArraList of Strings as options. Options will be shown at the
	 * list.
	 * 
	 * @param ArrayList
	 *            <String> options
	 */
	public void setOptions(List<String> options) {
		this.options = options;
		listBox.clear();
		for (String string : options) {
			listBox.addItem(string);
		}
	}

	@Override
	public void setValue(String value) {
		if (options.contains(value)) {
			listBox.setSelectedIndex(options.indexOf(value));
		} else {
			listBox.addItem(value);
			listBox.getItemCount();
			listBox.setSelectedIndex(listBox.getItemCount() - 1);
		}
		inititalValue = value;
		validate(this.getValue());
	}
}
