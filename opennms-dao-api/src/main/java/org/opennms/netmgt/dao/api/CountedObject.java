/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class CountedObject<T> implements Comparable<CountedObject<T>> {
    private T m_object;
    private long m_count;

    public CountedObject() {
    }

    public CountedObject(final T object, final Long count) {
        m_object = object;
        m_count = count;
    }

    public void setObject(final T object) {
        m_object = object;
    }

    public T getObject() {
        return m_object;
    }
    
    public void setCount(final int count) {
        m_count = count;
    }
    
    public Long getCount() {
        return m_count;
    }

    @Override
    public int compareTo(final CountedObject<T> o) {
        return new CompareToBuilder()
            .append(this.getCount(), (o == null? null:o.getCount()))
            .append(this.getObject(), (o == null? null:o.getObject()))
            .toComparison();
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append(this.getObject())
            .append(this.getCount())
            .toString();
    }
}
