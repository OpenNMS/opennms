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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.vaadin.data.Property;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table.ColumnGenerator;

public class CheckboxGenerator implements ColumnGenerator, ItemSetChangeListener {

	private static final long serialVersionUID = 2L;

	private final String m_valueProperty;
	protected final Set<CheckBox> m_checkboxes = new HashSet<CheckBox>();
	protected Set<Integer> m_selectedCheckboxes = new TreeSet<Integer>();
	protected Set<Integer> m_notSelectedCheckboxes = new TreeSet<Integer>(); //Explizit not set
	private boolean m_selectAll = false;

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
			button.setValue(isSelected((Integer) property.getValue()));
			button.addListener(new ClickListener() {

				private static final long serialVersionUID = 2991986878904005830L;

				@Override
				public void buttonClick(ClickEvent event) {
					if (event.getButton().booleanValue()) {
						m_selectedCheckboxes .add((Integer)event.getButton().getData());
						m_notSelectedCheckboxes.remove(event.getButton().getData());
					} else {
						m_selectedCheckboxes.remove(event.getButton().getData());
						m_notSelectedCheckboxes.add((Integer)event.getButton().getData());
					}
				}
			});
			m_checkboxes.add(button);
			return button;
		}
	}

	private boolean isSelected(Integer id) {
	    return (m_selectAll || m_selectedCheckboxes.contains(id)) && !m_notSelectedCheckboxes.contains(id);
	}
	
	public Set<Integer> getSelectedIds(Table source) {
	    if (m_selectAll) {
	        Set<Integer> selected = new TreeSet<Integer>(); 
	        for (Object eachItemId : source.getItemIds()) {
	           Property property = source.getContainerProperty(eachItemId,  m_valueProperty);
	           if (property == null) continue;
	           selected.add((Integer)property.getValue());
	        }
	        
	        //remove unselected
	        selected.removeAll(m_notSelectedCheckboxes);
	        return selected;
	    } 
		return Collections.unmodifiableSet(m_selectedCheckboxes);
	}

	public void clearSelectedIds(Table source) {
	    m_selectAll = false;
	    
		// Uncheck all of the checkboxes
		for (CheckBox button : m_checkboxes) {
			button.setValue(false);
		}
		m_selectedCheckboxes.clear();
		m_notSelectedCheckboxes.clear();
	}

	public void selectAll(Table source) {
	    m_selectAll = true;
	    
		m_selectedCheckboxes.clear();
		m_notSelectedCheckboxes.clear();
		// Check all of the checkboxes
		for (CheckBox button : m_checkboxes) {
			button.setValue(true);
			m_selectedCheckboxes.add((Integer)button.getData());
		}
	}

	@Override
	public void containerItemSetChange(ItemSetChangeEvent event) {
		// Delete all of the checkboxes, they will be regenerated during Table.containerItemSetChange()
		m_checkboxes.clear();

		// Remove any selected item IDs that are no longer present in the container
		Iterator<Integer> itr = m_selectedCheckboxes.iterator();
		while(itr.hasNext()) {
			if (!event.getContainer().getItemIds().contains(itr.next())) {
				itr.remove();
			}
		}
		
		// Remove any not selected item IDs that are no longer present in the container
        itr = m_notSelectedCheckboxes.iterator();
        while(itr.hasNext()) {
            if (!event.getContainer().getItemIds().contains(itr.next())) {
                itr.remove();
            }
        }
	}
}
