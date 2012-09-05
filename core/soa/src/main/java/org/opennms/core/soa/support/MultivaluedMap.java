/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.soa.support;

import java.util.Map;
import java.util.Set;

/**
 * MultivaluedMap
 *
 * @author brozow
 * @version $Id: $
 */
public interface MultivaluedMap<K, V> extends Map<K, Set<V>> {
    
    /**
     * <p>add</p>
     *
     * @param key a K object.
     * @param value a V object.
     * @param <K> a K object.
     * @param <V> a V object.
     */
    public void add(K key, V value);
    
    /**
     * <p>remove</p>
     *
     * @param key a K object.
     * @param value a V object.
     * @return a boolean.
     */
    public boolean remove(K key, V value);
    
    /**
     * <p>getCopy</p>
     *
     * @param key a K object.
     * @return a {@link java.util.Set} object.
     */
    public Set<V> getCopy(K key);
    
}
