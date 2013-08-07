/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.api;

import com.vaadin.data.util.BeanContainer;

/**
 * The Class OnmsBeanContainer.
 *
 * @param <T> the OpenNMS entity
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
@SuppressWarnings("serial")
public class OnmsBeanContainer<T> extends BeanContainer<Long, T> {

    /**
     * Instantiates a new OpenNMS bean container.
     *
     * @param type the type
     */
    public OnmsBeanContainer(Class<? super T> type) {
        super(type);
        setBeanIdResolver(new BeanIdResolver<Long,T>() {
            @Override
            public Long getIdForBean(T bean) {
                return generateItemId();
            }
        });
    }

    /**
     * Generates an itemId.
     *
     * @return the object
     */
    public Long generateItemId() {
        return System.nanoTime();
    }

    /**
     * Adds an OpenNMS bean.
     *
     * @param bean the new OpenNMS bean
     * @return the itemId
     */
    public Object addOnmsBean(T bean) {
        Long itemId = (Long) generateItemId();
        addItem(itemId, bean);
        return itemId;
    }

    /* (non-Javadoc)
     * @see com.vaadin.data.util.BeanContainer#setBeanIdProperty(java.lang.Object)
     */
    @Override
    public void setBeanIdProperty(Object propertyId) {}

}
