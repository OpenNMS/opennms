/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.dao.support;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.map.MultiValueMap;
import org.opennms.netmgt.dao.ExtensionManager;

/**
 * @author brozow
 *
 */
public class DefaultExtensionManager implements ExtensionManager {
    
    MultiValueMap m_extensions = new MultiValueMap();
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.ExtensionManager#findExtensions(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <T> Collection<T> findExtensions(Class<T> extensionPoint) {
        Collection collection = m_extensions.getCollection(extensionPoint);
        return collection == null? Collections.emptyList() : Collections.unmodifiableCollection(collection);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.ExtensionManager#registerExtension(java.lang.Object, java.lang.Class<? super T>[])
     */
    public void registerExtension(Object extension, Class<?>... extensionPoints) {
        for(Class<?> extensionPoint : extensionPoints) {
            m_extensions.put(extensionPoint, extension);
        }
    }

}
