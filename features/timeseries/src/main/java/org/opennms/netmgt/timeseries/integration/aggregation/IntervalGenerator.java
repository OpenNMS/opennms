/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.integration.aggregation;


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Timestamp;


/**
 * Aligned, fixed-interval iterator of {@link Timestamp}s.
 * 
 * Copied from Newts project.
 */
public class IntervalGenerator implements Iterator<Timestamp>, Iterable<Timestamp> {

    private final Duration m_interval;
    private final Timestamp m_final;
    private final boolean m_reversed;
    private Timestamp m_current;

    public IntervalGenerator(Timestamp start, Timestamp finish, Duration interval) {
        this(start, finish, interval, false);
    }
    
    public IntervalGenerator(Timestamp start, Timestamp finish, Duration interval, boolean reversed) {
        m_interval = checkNotNull(interval, "interval argument");
        checkNotNull(start, "start argument");
        checkNotNull(finish, "finish argument");

        m_current = reversed ? finish : start;
        m_final = reversed ? start : finish;
        m_reversed = reversed;

    }

    @Override
    public boolean hasNext() {
        return  m_reversed ? m_current.gte(m_final) : m_current.lte(m_final);
    }

    @Override
    public Timestamp next() {

        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        try {
            return m_current;
        }
        finally {
            m_current = m_reversed ? m_current.minus(m_interval) : m_current.plus(m_interval);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Timestamp> iterator() {
        return this;
    }

}
