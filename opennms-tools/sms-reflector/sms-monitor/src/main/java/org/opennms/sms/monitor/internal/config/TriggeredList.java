/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.sms.monitor.internal.config;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>TriggeredList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class TriggeredList<T> extends AbstractList<T> {
    List<T> m_backingList = new ArrayList<T>();

    /** {@inheritDoc} */
    @Override
    public T get(int index) {
        return m_backingList.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        int size = m_backingList.size();
        return size;
    }

    /** {@inheritDoc} */
    @Override
    public void add(int index, T element) {
        m_backingList.add(index, element);
        if (element != null) {
            onAdd(index, element);
        }
    }

    /** {@inheritDoc} */
    @Override
    public T remove(int index) {
        T element = m_backingList.remove(index);
        if (element != null) {
            onRemove(index, element);
        }
        return element;
    }

    /** {@inheritDoc} */
    @Override
    public T set(int index, T element) {
        T old = m_backingList.set(index, element);
        onSet(index, old, element);
        return old;
    }
    
    /**
     * <p>onSet</p>
     *
     * @param index a int.
     * @param oldElem a T object.
     * @param newElem a T object.
     */
    protected void onSet(int index, T oldElem, T newElem) {
        if (oldElem != null) {
            onRemove(index, oldElem);
        }
        if (newElem != null) {
            onAdd(index, newElem);
        }
    }

    /**
     * <p>onAdd</p>
     *
     * @param index a int.
     * @param element a T object.
     */
    protected void onAdd(int index, T element) {
    }

    /**
     * <p>onRemove</p>
     *
     * @param index a int.
     * @param element a T object.
     */
    protected void onRemove(int index, T element) {
    }


}
