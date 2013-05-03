/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.browsers;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.vaadin.data.Property;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.ColumnGenerator;

public class CheckboxGenerator implements ColumnGenerator {

	private static final long serialVersionUID = -1072007643387089006L;

	private final String m_valueProperty;

	protected Set<Integer> m_selectedCheckboxes = new TreeSet<Integer>();

	public CheckboxGenerator(String valueProperty) {
		m_valueProperty = valueProperty;
	}

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) {
		final Property property = source.getContainerProperty(itemId, m_valueProperty);
		if (property.getValue() == null) {
			return null;
		} else {
			CheckBox button = new CheckBox();
			button.setData(property.getValue());
			button.addListener(new ClickListener() {

				private static final long serialVersionUID = 2991986878904005830L;

				@Override
				public void buttonClick(ClickEvent event) {
					if (event.getButton().booleanValue()) {
						m_selectedCheckboxes .add((Integer)event.getButton().getData());
					} else {
						m_selectedCheckboxes.remove(event.getButton().getData());
					}
				}
			});
			return button;
		}
	}

	public Set<Integer> getSelectedIds() {
		return Collections.unmodifiableSet(m_selectedCheckboxes);
	}
}
