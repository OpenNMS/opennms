/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.datacollection;

import java.util.List;

import com.vaadin.data.Item;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.AbstractSelect.NewItemHandler;

/**
 * A factory for creating MIB Group Field objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public final class GroupFieldFactory implements FormFieldFactory {
    
    /** The resource types. */
    private final List<String> resourceTypes;
    
    /**
     * Instantiates a new group field factory.
     *
     * @param resourceTypes the available resource types
     */
    public GroupFieldFactory(final List<String> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.FormFieldFactory#createField(com.vaadin.data.Item, java.lang.Object, com.vaadin.ui.Component)
     */
    @Override
    public Field<?> createField(Item item, Object propertyId, Component uiContext) {
        if ("name".equals(propertyId)) {
            final TextField f = new TextField("Group Name");
            f.setRequired(true);
            f.setWidth("100%");
            return f;
        }
        if ("ifType".equals(propertyId)) {
            final ComboBox f = new ComboBox("ifType Filter");
            f.addItem("ignore");
            f.addItem("all");
            f.setNullSelectionAllowed(false);
            f.setRequired(true);
            f.setImmediate(true);
            f.setNewItemsAllowed(true);
            f.setNewItemHandler(new NewItemHandler() {
                @Override
                public void addNewItem(String newItemCaption) {
                    if (!f.containsId(newItemCaption)) {
                        f.addItem(newItemCaption);
                        f.setValue(newItemCaption);
                    }
                }
            });
            return f;
        }
        if ("mibObjCollection".equals(propertyId)) {
            final MibObjField f = new MibObjField(resourceTypes);
            f.setCaption("MIB Objects");
            f.setRequired(true);
            f.setImmediate(true);
            f.setWidth("100%");
            return f;
        }
        return null;
    }
}
