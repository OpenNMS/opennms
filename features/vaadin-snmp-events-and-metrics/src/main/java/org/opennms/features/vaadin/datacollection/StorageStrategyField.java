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

import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;
import org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy;

/**
 * The Storage Strategy Field.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class StorageStrategyField extends AbstractStrategyField<StorageStrategy> {

    /**
     * Instantiates a new storage strategy field.
     *
     * @param caption the caption
     */
    public StorageStrategyField(String caption) {
        // If the strategy from the XML is different, it will be added automatically to the combo-box
        super(caption, new String[] {
                IndexStorageStrategy.class.getName(),
                SiblingColumnStorageStrategy.class.getName()
        });
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getType()
     */
    @Override
    public Class<StorageStrategy> getType() {
        return StorageStrategy.class;
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#setInternalValue(java.lang.Object)
     */
    @Override
    protected void setInternalValue(StorageStrategy strategy) {
        setComboValue(strategy.getClazz());
        container.removeAllItems();
        container.addAll(strategy.getParameters());
    }

    /* (non-Javadoc)
     * @see com.vaadin.ui.AbstractField#getInternalValue()
     */
    @Override
    protected StorageStrategy getInternalValue() {
        StorageStrategy strategy = new StorageStrategy();
        if (combo.getValue() != null) {
            strategy.setClazz((String) combo.getValue());
        }
        for (Object itemId: container.getItemIds()) {
            strategy.addParameter(container.getItem(itemId).getBean());
        }
        return strategy;
    }

}
