/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.datacollection;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.netmgt.config.datacollection.Collect;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TwinColSelect;

/**
 * The Collect Field.
 * 
 * TODO: when a new group is added, the groupField must be updated.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class CollectField extends CustomField<Collect> {

    /** The selection field. */
    private final TwinColSelect selectField = new TwinColSelect();

    /**
     * Instantiates a new collect field.
     *
     * @param caption the caption
     * @param groups the available groups
     */
    public CollectField(String caption, List<String> groups) {
        setCaption(caption);
        selectField.setRows(10);
        selectField.setLeftColumnCaption("Available");
        selectField.setRightColumnCaption("Selected");

        for (String group : groups) {
            selectField.addItem(group);
        }

    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.CustomField#initContent()
     */
    @Override
    public Component initContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(selectField);
        return layout;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getType()
     */
    @Override
    public Class<Collect> getType() {
        return Collect.class;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getInternalValue()
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Collect getInternalValue() {
        Collect collect = new Collect();
        if (selectField.getValue() instanceof Set) {
            Set<String> selected = (Set<String>) selectField.getValue();
            for (String value : selected) {
                collect.addIncludeGroup(value);
            }
        } else {
            collect.addIncludeGroup((String)selectField.getValue());
        }
        return collect;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#setInternalValue(java.lang.Object)
     */
    @Override
    protected void setInternalValue(Collect value) {
        selectField.setValue(new TreeSet<String>(value.getIncludeGroups()));
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#setReadOnly(boolean)
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        selectField.setReadOnly(readOnly);
        super.setReadOnly(readOnly);
    }

}
