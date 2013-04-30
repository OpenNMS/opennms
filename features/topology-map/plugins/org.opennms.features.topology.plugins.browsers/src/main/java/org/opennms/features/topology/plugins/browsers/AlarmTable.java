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

package org.opennms.features.topology.plugins.browsers;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.opennms.features.topology.api.HasExtraComponents;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;

public class AlarmTable extends SelectionAwareTable implements HasExtraComponents {

	private static final long serialVersionUID = -1384405693333129773L;

	private static class CheckboxButton extends Button {

		private static final long serialVersionUID = -3595363303361200441L;

		private CheckboxGenerator m_generator;
		private ComboBox m_ackCombo;

		public CheckboxButton(String string) {
			super(string);
			addListener(new ClickListener() {

				private static final long serialVersionUID = 4351558084135658129L;

				@Override
				public void buttonClick(final ClickEvent event) {
					Set<Integer> selected = m_generator.getSelectedIds();
					String action = (String)m_ackCombo.getValue();
					LoggerFactory.getLogger(getClass()).warn("WOULD HAVE DONE OPERATION {} to [{}]", action, StringUtils.join(selected.toArray(new Integer[0]), ","));
				}
			});
		}

		public void setCombo(final ComboBox combo) {
			m_ackCombo = combo;
		}

		public void setCheckboxGenerator(final CheckboxGenerator generator) {
			m_generator = generator;
		}
	}

	private final CheckboxButton m_submitButton;
	private final ComboBox m_ackCombo;

	/**
	 *  Leave OnmsDaoContainer without generics; the Aries blueprint code cannot match up
	 *  the arguments if you put the generic types in.
	 */
	@SuppressWarnings("unchecked")
	public AlarmTable(final String caption, final OnmsDaoContainer container) {
		super(caption, container);
		m_ackCombo = new ComboBox();
		m_ackCombo.setNullSelectionAllowed(false);
		Item heloItem = m_ackCombo.addItem("Hello");
		m_ackCombo.addItem("World");
		m_ackCombo.addItem("Awesome");
		m_ackCombo.setValue(heloItem);
		
		m_submitButton = new CheckboxButton("Submit");
		m_submitButton.setCombo(m_ackCombo);

	}

	@Override
	public void setColumnGenerators(final Map generators) {
		for (final Object key : generators.keySet()) {
			super.addGeneratedColumn(key, (ColumnGenerator)generators.get(key));
			// If any of the column generators are {@link CheckboxGenerator} instances,
			// then connect it to the ack buttons.
			try {
				m_submitButton.setCheckboxGenerator((CheckboxGenerator)generators.get(key));
			} catch (final ClassCastException e) {}
		}
	}

	@Override
	public void setCellStyleGenerator(final CellStyleGenerator generator) {
		try {
			((TableAware)generator).setTable(this);
		} catch (final ClassCastException e) {}
		super.setCellStyleGenerator(generator);
	}

	@Override
	public Component[] getExtraComponents() {
		/*
		return new Component[] {
				m_ackCombo,
				m_submitButton
		};
		*/
		return new Component[0];
	}
}
