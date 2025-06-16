/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.plugins.browsers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.web.api.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.Container.ItemSetChangeEvent;
import com.vaadin.v7.data.Container.ItemSetChangeListener;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Table.ColumnGenerator;

public class CheckboxGenerator implements ColumnGenerator, ItemSetChangeListener {

	private static final long serialVersionUID = 2L;

	private final String m_valueProperty;
	protected final Set<CheckBox> m_checkboxes = new HashSet<>();
	protected Set<Integer> m_selectedCheckboxes = new TreeSet<>();
	protected Set<Integer> m_notSelectedCheckboxes = new TreeSet<Integer>(); //Explizit not set
	private boolean m_selectAll = false;

	public CheckboxGenerator(String valueProperty) {
		m_valueProperty = valueProperty;
	}

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) {
		final Property<Integer> property = source.getContainerProperty(itemId, m_valueProperty);
		if (property.getValue() == null) {
			return null;
		} else {
			if (SecurityContextHolder.getContext().toString().contains(Authentication.ROLE_READONLY)) {
				// Do not render the checkboxes for read-only users
				return null;
			} else {
				final CheckBox button = new CheckBox();
				button.setData(property.getValue());
				button.setValue(isSelected((Integer) property.getValue()));
				button.addValueChangeListener(new ValueChangeListener() {
	
					private static final long serialVersionUID = 2991986878904005830L;
	
					@Override
					public void valueChange(ValueChangeEvent event) {
						if (Boolean.TRUE.equals(event.getProperty().getValue())) {
							m_selectedCheckboxes.add(property.getValue());
							m_notSelectedCheckboxes.remove(property.getValue());
						} else {
							m_selectedCheckboxes.remove(property.getValue());
							m_notSelectedCheckboxes.add(property.getValue());
						}
					}
				});
				m_checkboxes.add(button);
				return button;
			}
		}
	}

	private boolean isSelected(Integer id) {
	    return (m_selectAll || m_selectedCheckboxes.contains(id)) && !m_notSelectedCheckboxes.contains(id);
	}
	
	public Set<Integer> getSelectedIds(Table source) {
	    if (m_selectAll) {
	        Set<Integer> selected = new TreeSet<Integer>(); 
	        for (Object eachItemId : source.getItemIds()) {
	           Property<Integer> property = source.getContainerProperty(eachItemId,  m_valueProperty);
	           if (property == null) continue;
	           selected.add(property.getValue());
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
