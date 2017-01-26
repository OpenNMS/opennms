/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.browsers;

import com.vaadin.data.Container;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.themes.BaseTheme;
import org.apache.commons.lang.ArrayUtils;
import org.opennms.features.topology.api.HasExtraComponents;
import org.opennms.features.topology.api.browsers.OnmsVaadinContainer;
import org.opennms.features.topology.api.browsers.SelectionAwareTable;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.osgi.EventProxy;
import org.opennms.osgi.VaadinApplicationContext;
import org.opennms.osgi.VaadinApplicationContextAware;
import org.opennms.web.api.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("serial")
public class AlarmTable extends SelectionAwareTable implements HasExtraComponents, VaadinApplicationContextAware {

	private static final String ACTION_CLEAR = "Clear";
	private static final String ACTION_ESCALATE = "Escalate";
	private static final String ACTION_UNACKNOWLEDGE = "Unacknowledge";
	private static final String ACTION_ACKNOWLEDGE = "Acknowledge";

	private static final long serialVersionUID = -1384405693333129773L;

    private EventProxy m_eventProxy;
    private VaadinApplicationContext vaadinApplicationContext;

    private class CheckboxButton extends Button {

		private static final long serialVersionUID = -3595363303361200441L;

		private CheckboxGenerator m_generator;
		private AbstractSelect m_ackCombo;

		public CheckboxButton(String string) {
			super(string);
			setColumnCollapsingAllowed(false);
			addClickListener(new ClickListener() {

				private static final long serialVersionUID = 4351558084135658129L;

				@Override
				public void buttonClick(final ClickEvent event) {
					Set<Integer> selected = m_generator.getSelectedIds(AlarmTable.this);
					if (selected.size() > 0) {
						String action = (String)m_ackCombo.getValue();
						if (ACTION_ACKNOWLEDGE.equals(action)) {
							m_alarmRepo.acknowledgeAlarms(
									ArrayUtils.toPrimitive(selected.toArray(new Integer[0])), 
									getUser(),
									new Date()
							);
						} else if (ACTION_UNACKNOWLEDGE.equals(action)) {
							m_alarmRepo.unacknowledgeAlarms(
									ArrayUtils.toPrimitive(selected.toArray(new Integer[0])), 
									getUser()
							);
						} else if (ACTION_ESCALATE.equals(action)) {
							m_alarmRepo.escalateAlarms(
									ArrayUtils.toPrimitive(selected.toArray(new Integer[0])), 
									getUser(),
									new Date()
							);
						} else if (ACTION_CLEAR.equals(action)) {
							m_alarmRepo.clearAlarms(
									ArrayUtils.toPrimitive(selected.toArray(new Integer[0])), 
									getUser(),
									new Date()
							);
						}

						// Clear the checkboxes
						m_generator.clearSelectedIds(AlarmTable.this);

						AlarmTable.this.containerItemSetChange(new Container.ItemSetChangeEvent() {
							private static final long serialVersionUID = 7086486972418241175L;
							@Override
							public Container getContainer() {
								return AlarmTable.this.getContainerDataSource();
							}
						});
					}
				}
			});
		}

		public void setCombo(final AbstractSelect combo) {
			m_ackCombo = combo;
		}

		public void setCheckboxGenerator(final CheckboxGenerator generator) {
			m_generator = generator;
		}
	}

	private class SelectAllButton extends Button {

		private CheckboxGenerator m_generator;

		public SelectAllButton(String string) {
			super(string);
			setStyleName(BaseTheme.BUTTON_LINK);
			addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					m_generator.selectAll(AlarmTable.this);
				}
			});
		}

		public void setCheckboxGenerator(final CheckboxGenerator generator) {
			m_generator = generator;
		}
	}

	private class ResetSelectionButton extends Button {

		private CheckboxGenerator m_generator;

		public ResetSelectionButton(String string) {
			super(string);
			setStyleName(BaseTheme.BUTTON_LINK);
			addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					m_generator.clearSelectedIds(AlarmTable.this);
				}
			});
		}

		public void setCheckboxGenerator(final CheckboxGenerator generator) {
			m_generator = generator;
		}
	}

	private final CheckboxButton m_submitButton;
	private final NativeSelect m_ackCombo;
	private final SelectAllButton m_selectAllButton = new SelectAllButton("Select All");
	private final ResetSelectionButton m_resetButton = new ResetSelectionButton("Deselect All");
	private final AlarmRepository m_alarmRepo;
	private Set<ItemSetChangeListener> m_itemSetChangeListeners = new HashSet<ItemSetChangeListener>();

	/**
	 *  Leave OnmsVaadinContainer without generics; the Aries blueprint code cannot match up
	 *  the arguments if you put the generic types in.
	 */
	public AlarmTable(final String caption, final OnmsVaadinContainer container, final AlarmRepository alarmRepo) {
		super(caption, container);
		m_alarmRepo = alarmRepo;

		m_ackCombo = new NativeSelect();
		m_ackCombo.setNullSelectionAllowed(false);
		m_ackCombo.addItem(ACTION_ACKNOWLEDGE);
		m_ackCombo.addItem(ACTION_UNACKNOWLEDGE);
		m_ackCombo.addItem(ACTION_ESCALATE);
		m_ackCombo.addItem(ACTION_CLEAR);
		m_ackCombo.setValue(ACTION_ACKNOWLEDGE); // Make "Acknowledge" the default value

		m_submitButton = new CheckboxButton("Submit");
		m_submitButton.setCombo(m_ackCombo);
	}
	
	@Override
	public void containerItemSetChange(Container.ItemSetChangeEvent event) {
		for (ItemSetChangeListener listener : m_itemSetChangeListeners ) {
			listener.containerItemSetChange(event);
		}
		super.containerItemSetChange(event);
	}

	@Override
	@SuppressWarnings("rawtypes") // Because Aries Blueprint cannot handle generics
	public void setColumnGenerators(final Map generators) {
		super.setColumnGenerators(generators);
		for (final Object key : generators.keySet()) {
            // If any of the column generators are {@link CheckboxGenerator} instances,
            // then connect it to the buttons.
			try {
                Object generatorObj = generators.get(key);
                CheckboxGenerator generator = (CheckboxGenerator) generatorObj;
				m_submitButton.setCheckboxGenerator(generator);
				m_selectAllButton.setCheckboxGenerator(generator);
				m_resetButton.setCheckboxGenerator(generator);

				m_itemSetChangeListeners.add(generator);
			} catch (final ClassCastException e) {}
		}
	}

	@Override
	public Component[] getExtraComponents() {
		if (SecurityContextHolder.getContext().toString().contains(Authentication.ROLE_READONLY)) {
			return new Component[0];
		} else {
			return new Component[] {
				m_selectAllButton,
				m_resetButton,
				m_ackCombo,
				m_submitButton
			};
		}
	}

    @Override
    public void setVaadinApplicationContext(VaadinApplicationContext vaadinApplicationContext) {
        this.vaadinApplicationContext = vaadinApplicationContext;
    }

	private String getUser() {
        return vaadinApplicationContext.getUsername();
	}
}
