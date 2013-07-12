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

import com.vaadin.data.Item;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.TextField;

/**
 * A factory for creating Resource Type Field objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public final class ResourceTypeFieldFactory implements FormFieldFactory {

    /* (non-Javadoc)
     * @see com.vaadin.ui.FormFieldFactory#createField(com.vaadin.data.Item, java.lang.Object, com.vaadin.ui.Component)
     */
    @Override
    public Field<?> createField(Item item, Object propertyId, Component uiContext) {
        if ("name".equals(propertyId)) {
            final TextField f = new TextField("Resource Type Name");
            f.setRequired(true);
            f.setWidth("100%");
            return f;
        }
        if ("label".equals(propertyId)) {
            final TextField f = new TextField("Resource Type Label");
            f.setRequired(true);
            f.setWidth("100%");
            return f;
        }
        if ("resourceLabel".equals(propertyId)) {
            final TextField f = new TextField("Resource Label");
            f.setRequired(false);
            f.setWidth("100%");
            return f;
        }
        if ("storageStrategy".equals(propertyId)) {
            StorageStrategyField f = new StorageStrategyField();
            f.setCaption("Storage Strategy");
            return f;
        }
        if ("persistenceSelectorStrategy".equals(propertyId)) {
            final PersistSelectorStrategyField f = new PersistSelectorStrategyField();
            f.setCaption("Persist Selector Strategy");
            return f;
        }
        return null;
    }
}
